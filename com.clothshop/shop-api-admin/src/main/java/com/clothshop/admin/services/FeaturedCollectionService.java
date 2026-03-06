package com.clothshop.admin.services;

import com.clothshop.admin.dtos.request.marketing.CollectionSaveRequest;
import com.clothshop.admin.dtos.response.marketing.CollectionResponse;
import com.clothshop.common.exceptions.BusinessException;
import com.clothshop.common.exceptions.ErrorCode;
import com.clothshop.common.utils.SlugUtils;
import com.clothshop.domain.entities.marketing.Collection;
import com.clothshop.domain.entities.marketing.CollectionItem;
import com.clothshop.domain.entities.product.Product;
import com.clothshop.domain.repositories.marketing.CollectionItemRepository;
import com.clothshop.domain.repositories.marketing.CollectionRepository;
import com.clothshop.domain.repositories.product.ProductRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class FeaturedCollectionService {

    private final CollectionRepository collectionRepository;
    private final CollectionItemRepository collectionItemRepository;
    private final ProductRepository productRepository;

    /**
     * Tạo mới hoặc Cập nhật Collection (Unified endpoint)
     * Sử dụng Shopee-style slug: {name}-c.{id}
     */
    @Transactional
    public CollectionResponse saveCollection(CollectionSaveRequest request, String username) {
        Collection collection;

        if (request.getId() == null) {
            log.info("Creating new collection: {}", request.getName());

            // Generate base slug from name (chưa có ID)
            String baseSlug = SlugUtils.makeSlug(request.getName());

            collection = Collection.builder()
                    .name(request.getName())
                    .slug(baseSlug) // Tạm thời dùng baseSlug, sẽ update sau khi có ID
                    .description(request.getDescription())
                    .isActive(request.getIsActive() != null ? request.getIsActive() : true)
                    .createdBy(username)
                    .build();

            // Save để có ID
            collection = collectionRepository.save(collection);

            // Generate slug với ID (Shopee style): bo-suu-tap-mua-he-c.123
            String finalSlug = generateSlugWithId(baseSlug, collection.getId());
            collection.setSlug(finalSlug);
            log.info("Generated Shopee-style slug: {}", finalSlug);

        } else {
            log.info("Updating collection ID: {}", request.getId());
            collection = collectionRepository.findById(request.getId())
                    .orElseThrow(() -> new BusinessException(ErrorCode.RESOURCE_NOT_FOUND, "Không tìm thấy bộ sưu tập"));

            // Regenerate slug if name changed (giữ nguyên ID suffix)
            String baseSlug = SlugUtils.makeSlug(request.getName());
            String newSlugWithId = generateSlugWithId(baseSlug, collection.getId());

            if (!collection.getSlug().equals(newSlugWithId)) {
                collection.setSlug(newSlugWithId);
                log.info("Updated slug from {} to {}", collection.getSlug(), newSlugWithId);
            }

            collection.setName(request.getName());
            collection.setDescription(request.getDescription());

            if (request.getIsActive() != null) {
                collection.setIsActive(request.getIsActive());
            }
            collection.setUpdatedBy(username);
        }

        Collection saved = collectionRepository.save(collection);

        // Nếu là update, cần đếm lại số lượng item đang có. Nếu tạo mới thì là 0.
        Long itemCount = request.getId() != null ? collectionItemRepository.countActiveItemsByCollectionId(saved.getId()) : 0L;

        return CollectionResponse.builder()
                .id(saved.getId())
                .name(saved.getName())
                .description(saved.getDescription())
                .isActive(saved.getIsActive())
                .itemCount(itemCount)
                .build();
    }

    /**
     * Soft Delete: Xóa bộ sưu tập và ẩn tất cả sản phẩm bên trong
     */
    @Transactional
    public void deleteCollection(Long id, String username) {
        log.info("Soft deleting collection ID: {}", id);
        Collection collection = collectionRepository.findById(id)
                .orElseThrow(() -> new BusinessException(ErrorCode.RESOURCE_NOT_FOUND, "Không tìm thấy bộ sưu tập"));

        collection.setIsActive(false);
        collection.setUpdatedBy(username);
        collectionRepository.save(collection);

        // Tắt toàn bộ sản phẩm bên trong để tránh hiển thị rác
        collectionItemRepository.deactivateAllItemsByCollectionId(id);
    }

    /**
     * Tối ưu Bulk Assignment (Many-to-Many)
     */
    @Transactional
    public void addProductsToCollection(Long collectionId, List<Long> productIds, String username) {
        if (productIds == null || productIds.isEmpty()) return;

        log.info("Bulk assigning {} products to collection: {}", productIds.size(), collectionId);

        Collection collection = collectionRepository.findById(collectionId)
                .orElseThrow(() -> new BusinessException(ErrorCode.RESOURCE_NOT_FOUND, "Bộ sưu tập không tồn tại"));

        // Lấy thứ tự lớn nhất
        Integer maxOrder = collectionItemRepository.findMaxDisplayOrderByCollectionId(collectionId).orElse(0);

        // Lấy danh sách ID đã có để lọc trùng
        List<Long> existingProductIds = collectionItemRepository.findProductIdsByCollectionId(collectionId);

        List<Long> newProductIds = productIds.stream()
                .filter(id -> !existingProductIds.contains(id))
                .distinct()
                .collect(Collectors.toList());

        if (newProductIds.isEmpty()) {
            log.warn("All products are already in the collection. Skipped.");
            return;
        }

        List<CollectionItem> itemsToSave = new ArrayList<>();
        int currentOrder = maxOrder + 1;

        for (Long pId : newProductIds) {
            //Tối ưu Proxy (Không query xuống DB)
            Product productProxy = productRepository.getReferenceById(pId);

            itemsToSave.add(CollectionItem.builder()
                    .collection(collection)
                    .product(productProxy)
                    .displayOrder(currentOrder++)
                    .addedBy(username)
                    .isActive(true)
                    .build());
        }

        // Gom một mẻ insert xuống DB
        collectionItemRepository.saveAll(itemsToSave);
        log.info("Successfully assigned {} new products to collection {}", itemsToSave.size(), collectionId);
    }

    /**
     * Thêm 1 sản phẩm vào bộ sưu tập (Wrapper method)
     *
     * Convenience method để thêm 1 sản phẩm đơn lẻ, thường dùng từ trang chi tiết sản phẩm.
     * Internally gọi addProductsToCollection() để tái sử dụng logic (kiểm tra trùng, displayOrder, etc.)
     *
     * Note: KHÔNG trùng với addProductsToCollection() - đây là wrapper cho single product.
     *
     * @param collectionId ID của bộ sưu tập
     * @param productId ID của sản phẩm cần thêm
     * @param username Tên người thực hiện
     * @see #addProductsToCollection(Long, List, String) Main implementation
     */
    @Transactional
    public void addProductToCollection(Long collectionId, Long productId, String username) {
        log.info("Adding single product {} to collection {}", productId, collectionId);
        // Delegate to bulk method for code reuse
        addProductsToCollection(collectionId, List.of(productId), username);
    }

    /**
     * Lấy danh sách tất cả Collection với itemCount (Tối ưu cho màn hình List)
     */
    @Transactional(readOnly = true)
    public Page<CollectionResponse> getAllCollectionsWithCount(Pageable pageable) {
        Page<Collection> collectionPage = collectionRepository.findAll(pageable);
        return collectionPage.map(this::mapToResponse);
    }

    /**
     * Tìm kiếm Collection theo tên với itemCount
     */
    @Transactional(readOnly = true)
    public Page<CollectionResponse> searchCollectionsByName(String keyword, Pageable pageable) {
        Page<Collection> collectionPage = collectionRepository.searchByName(keyword, pageable);
        return collectionPage.map(this::mapToResponse);
    }

    /**
     * Tìm collection theo slug (Optimized với ID parsing)
     *
     * Thay vì query WHERE slug = ?, ta parse ID từ slug và query WHERE id = ?
     * → Nhanh hơn vì query trực tiếp Primary Key (indexed)
     *
     * @param slug Collection slug (VD: "bo-suu-tap-mua-he-c.123")
     * @return Collection entity
     * @throws BusinessException nếu không tìm thấy
     */
    @Transactional(readOnly = true)
    public Collection findBySlug(String slug) {
        // Try parse ID from slug first (fast path)
        Long id = parseIdFromSlug(slug);

        if (id != null) {
            // Query trực tiếp theo ID (Primary Key - siêu nhanh)
            return collectionRepository.findById(id)
                    .orElseThrow(() -> new BusinessException(
                            ErrorCode.RESOURCE_NOT_FOUND,
                            "Không tìm thấy bộ sưu tập với slug: " + slug));
        }

        // Fallback: Query theo slug (cho các slug cũ không có ID)
        return collectionRepository.findBySlug(slug)
                .orElseThrow(() -> new BusinessException(
                        ErrorCode.RESOURCE_NOT_FOUND,
                        "Không tìm thấy bộ sưu tập với slug: " + slug));
    }

    /**
     * Helper method: Map Collection entity sang CollectionResponse với itemCount
     */
    private CollectionResponse mapToResponse(Collection collection) {
        Long itemCount = collectionItemRepository.countActiveItemsByCollectionId(collection.getId());
        return CollectionResponse.builder()
                .id(collection.getId())
                .name(collection.getName())
                .description(collection.getDescription())
                .isActive(collection.getIsActive())
                .itemCount(itemCount)
                .build();
    }

    /**
     * Generate slug với ID suffix (Shopee style)
     *
     * Pattern: {base-slug}-c.{id}
     * Example: "bo-suu-tap-mua-he-c.123"
     *
     * Ưu điểm:
     * - 100% unique (dựa vào Primary Key)
     * - Query nhanh (parse ID từ slug để tìm trực tiếp)
     * - Clean & Professional
     * - SEO friendly
     *
     * @param baseSlug Slug gốc từ tên (VD: "bo-suu-tap-mua-he")
     * @param id Collection ID (VD: 123)
     * @return Slug hoàn chỉnh (VD: "bo-suu-tap-mua-he-c.123")
     */
    private String generateSlugWithId(String baseSlug, Long id) {
        return baseSlug + "-c." + id;
    }

    /**
     * Parse collection ID từ slug (để query nhanh)
     *
     * Example: "bo-suu-tap-mua-he-c.123" → 123
     *
     * @param slug Slug đầy đủ
     * @return Collection ID, hoặc null nếu parse fail
     */
    public static Long parseIdFromSlug(String slug) {
        if (slug == null || !slug.contains("-c.")) {
            return null;
        }

        try {
            String idPart = slug.substring(slug.lastIndexOf("-c.") + 3);
            return Long.parseLong(idPart);
        } catch (Exception e) {
            return null;
        }
    }
}
