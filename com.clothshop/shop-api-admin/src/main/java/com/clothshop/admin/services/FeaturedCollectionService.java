package com.clothshop.admin.services;

import com.clothshop.admin.dtos.request.marketing.CollectionSaveRequest;
import com.clothshop.admin.dtos.response.marketing.BulkAssignResult;
import com.clothshop.admin.dtos.response.marketing.CollectionResponse;
import com.clothshop.admin.mappers.CollectionMapper;
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
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class FeaturedCollectionService {

    private final CollectionRepository collectionRepository;
    private final CollectionItemRepository collectionItemRepository;
    private final ProductRepository productRepository;
    private final CollectionMapper collectionMapper;

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

            // USAGE 1: Dùng Mapper để map Request sang Entity thay vì dùng Builder thủ công
            collection = collectionMapper.toEntity(request);
            collection.setSlug(baseSlug);
            if (request.getIsActive() == null) collection.setIsActive(true);

            if (request.getIsActive() == null) {
                collection.setIsActive(true);
            }

            // Save lần 1 để DB sinh ra ID
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

            // USAGE 2: Dùng Mapper update các trường (name, description, isActive) từ Request vào Entity
            collectionMapper.updateEntityFromRequest(request, collection);
        }

        // Save lần 2 (áp dụng cho cả Create để lưu cái finalSlug, và Update)
        Collection saved = collectionRepository.save(collection);

        return mapToResponse(saved);
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
        collectionRepository.save(collection);

        // Tắt toàn bộ sản phẩm bên trong để tránh hiển thị rác
        collectionItemRepository.deactivateAllItemsByCollectionId(id);
    }

    /**
     * Bulk Assignment (Chống N+1 và Lỗi Duplicate Key)
     */
    @Transactional
    public BulkAssignResult addProductsToCollection(Long collectionId, List<Long> productIds, String username) {
        if (productIds == null || productIds.isEmpty()) {
            return BulkAssignResult.builder().addedCount(0).duplicateCount(0).duplicateProductNames(List.of()).totalRequested(0).build();
        }

        // 1. Sanitize: Bọc loại bỏ trùng lặp ID phòng trường hợp Frontend gửi lên 1 ID hai lần
        List<Long> distinctProductIds = productIds.stream().distinct().collect(Collectors.toList());

        Collection collection = collectionRepository.findById(collectionId)
                .orElseThrow(() -> new BusinessException(ErrorCode.RESOURCE_NOT_FOUND, "Bộ sưu tập không tồn tại"));

        // Lấy thứ tự lớn nhất
        Integer maxOrder = collectionItemRepository.findMaxDisplayOrderByCollectionId(collectionId).orElse(0);

        // 2. Kéo TOÀN BỘ lịch sử (active + inactive) của các sản phẩm này lên RAM (Chỉ 1 câu SQL duy nhất - xuyên thủng @SQLRestriction)
        List<CollectionItem> historicalItems = collectionItemRepository.findAllHistoryByCollectionIdAndProductIds(collectionId, distinctProductIds);

        // Chuyển sang Map để tra cứu O(1) thay vì O(n) mỗi vòng lặp
        Map<Long, CollectionItem> historicalItemMap = historicalItems.stream()
                .collect(Collectors.toMap(
                        item -> item.getProduct().getId(),
                        item -> item,
                        (existing, replacement) -> existing));

        List<CollectionItem> itemsToSave = new ArrayList<>();
        List<String> duplicateProductNames = new ArrayList<>();
        int currentOrder = maxOrder + 1;
        int reactivatedCount = 0;

        // 3. Xử lý logic trên RAM để chống N+1
        for (Long pId : distinctProductIds) {
            // Tìm xem ID này đã từng tồn tại trong lịch sử chưa
            CollectionItem existingItem = historicalItemMap.get(pId);

            if (existingItem != null) {
                if (existingItem.getIsActive()) {
                    // TH1: Đã tồn tại và ĐANG ACTIVE -> Ghi nhận trùng lặp để báo UI
                    duplicateProductNames.add(productNameMap.getOrDefault(pId, ""));
                } else {
                    // TH2: Đã tồn tại nhưng BỊ XÓA MỀM -> Khôi phục (Re-activate) thay vì tạo mới
                    existingItem.setIsActive(true);
                    existingItem.setDisplayOrder(currentOrder++);
                    // Không cần setUpdatedBy thủ công vì Auditing @LastModifiedBy sẽ tự lo
                    itemsToSave.add(existingItem);
                    reactivatedCount++;
                }
            } else {
                // TH3: Mới hoàn toàn -> Tạo bản ghi mới sử dụng Product Proxy để không query dư thừa
                Product productProxy = productRepository.getReferenceById(pId);
                itemsToSave.add(CollectionItem.builder()
                        .collection(collection)
                        .product(productProxy)
                        .displayOrder(currentOrder++)
                        .isActive(true)
                        .build());
            }
        }

        // 4. Batch Update / Insert
        if (!itemsToSave.isEmpty()) {
            collectionItemRepository.saveAll(itemsToSave);
            log.info("Assigned {} products ({} new, {} reactivated) to collection {}",
                    itemsToSave.size(), itemsToSave.size() - reactivatedCount, reactivatedCount, collectionId);
        }

        return BulkAssignResult.builder()
                .addedCount(itemsToSave.size())
                .duplicateCount(duplicateProductNames.size())
                .duplicateProductNames(duplicateProductNames)
                .totalRequested(distinctProductIds.size())
                .build();
    }

    /**
     * Thêm 1 sản phẩm vào bộ sưu tập (Wrapper method)
     */
    @Transactional
    public void addProductToCollection(Long collectionId, Long productId, String username) {
        log.info("Adding single product {} to collection {}", productId, collectionId);
        addProductsToCollection(collectionId, List.of(productId), username);
    }

    /**
     * Lấy danh sách tất cả Collection với itemCount (Tối ưu cho màn hình List)
     */
    @Transactional(readOnly = true)
    public Page<CollectionResponse> getAllCollectionsWithCount(Pageable pageable) {
        return collectionRepository.findAll(pageable).map(this::mapToResponse);
    }

    /**
     * Tìm kiếm Collection theo tên với itemCount
     */
    @Transactional(readOnly = true)
    public Page<CollectionResponse> searchCollectionsByName(String keyword, Pageable pageable) {
        return collectionRepository.searchByName(keyword, pageable).map(this::mapToResponse);
    }

    /**
     * Helper method: Map Collection entity sang CollectionResponse với itemCount
     */
    private CollectionResponse mapToResponse(Collection collection) {
        // USAGE 3: Dùng Mapper biến Entity thành DTO
        CollectionResponse response = collectionMapper.toResponse(collection);

        // Count số lượng và set vào
        Long itemCount = collection.getId() != null ? collectionItemRepository.countActiveItemsByCollectionId(collection.getId()) : 0L;
        response.setItemCount(itemCount);

        return response;
    }

    /**
     * Generate slug với ID suffix (Shopee style)
     */
    private String generateSlugWithId(String baseSlug, Long id) {
        return baseSlug + "-c." + id;
    }

    /**
     * Parse collection ID từ slug (để query nhanh)
     */
    public static Long parseIdFromSlug(String slug) {
        if (slug == null || !slug.contains("-c.")) return null;
        try {
            String idPart = slug.substring(slug.lastIndexOf("-c.") + 3);
            return Long.parseLong(idPart);
        } catch (Exception e) {
            return null;
        }
    }
}