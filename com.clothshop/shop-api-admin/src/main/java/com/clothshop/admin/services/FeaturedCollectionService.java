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

    @Transactional
    public CollectionResponse saveCollection(CollectionSaveRequest request, String username) {
        Collection collection;

        if (request.getId() == null) {
            log.info("Creating new collection: {}", request.getName());
            String baseSlug = SlugUtils.makeSlug(request.getName());

            collection = collectionMapper.toEntity(request);
            collection.setSlug(baseSlug);
            if (request.getIsActive() == null) collection.setIsActive(true);

            collection = collectionRepository.save(collection);
            String finalSlug = generateSlugWithId(baseSlug, collection.getId());
            collection.setSlug(finalSlug);
        } else {
            log.info("Updating collection ID: {}", request.getId());
            collection = collectionRepository.findById(request.getId())
                    .orElseThrow(() -> new BusinessException(ErrorCode.RESOURCE_NOT_FOUND, "Không tìm thấy bộ sưu tập"));

            String baseSlug = SlugUtils.makeSlug(request.getName());
            String newSlugWithId = generateSlugWithId(baseSlug, collection.getId());

            if (!collection.getSlug().equals(newSlugWithId)) {
                collection.setSlug(newSlugWithId);
            }
            collectionMapper.updateEntityFromRequest(request, collection);
        }

        Collection saved = collectionRepository.save(collection);
        return mapToResponse(saved);
    }

    @Transactional
    public void deleteCollection(Long id, String username) {
        log.info("Soft deleting collection ID: {}", id);
        Collection collection = collectionRepository.findById(id)
                .orElseThrow(() -> new BusinessException(ErrorCode.RESOURCE_NOT_FOUND, "Không tìm thấy bộ sưu tập"));

        collection.setIsActive(false);
        collectionRepository.save(collection);
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

        Integer maxOrder = collectionItemRepository.findMaxDisplayOrderByCollectionId(collectionId).orElse(0);

        // 2. Kéo TOÀN BỘ lịch sử (active + inactive) của các sản phẩm này lên RAM (Chỉ 1 câu SQL duy nhất - xuyên thủng @SQLRestriction)
        List<CollectionItem> historicalItems = collectionItemRepository.findAllHistoryByCollectionIdAndProductIds(collectionId, distinctProductIds);

        List<CollectionItem> itemsToSave = new ArrayList<>();
        List<String> duplicateProductNames = new ArrayList<>();
        int currentOrder = maxOrder + 1;
        int reactivatedCount = 0;

        // 3. Xử lý logic trên RAM để chống N+1
        for (Long pId : distinctProductIds) {
            // Tìm xem ID này đã từng tồn tại trong lịch sử chưa
            Optional<CollectionItem> existingItemOpt = historicalItems.stream()
                    .filter(item -> item.getProduct().getId().equals(pId))
                    .findFirst();

            if (existingItemOpt.isPresent()) {
                CollectionItem existingItem = existingItemOpt.get();
                if (existingItem.getIsActive()) {
                    // TH1: Đã tồn tại và ĐANG ACTIVE -> Ghi nhận trùng lặp để báo UI
                    duplicateProductNames.add(existingItem.getProduct().getProductName());
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

    @Transactional
    public void addProductToCollection(Long collectionId, Long productId, String username) {
        addProductsToCollection(collectionId, List.of(productId), username);
    }

    @Transactional(readOnly = true)
    public Page<CollectionResponse> getAllCollectionsWithCount(Pageable pageable) {
        return collectionRepository.findAll(pageable).map(this::mapToResponse);
    }

    @Transactional(readOnly = true)
    public Page<CollectionResponse> searchCollectionsByName(String keyword, Pageable pageable) {
        return collectionRepository.searchByName(keyword, pageable).map(this::mapToResponse);
    }

    private CollectionResponse mapToResponse(Collection collection) {
        CollectionResponse response = collectionMapper.toResponse(collection);
        Long itemCount = collection.getId() != null ? collectionItemRepository.countActiveItemsByCollectionId(collection.getId()) : 0L;
        response.setItemCount(itemCount);
        return response;
    }

    private String generateSlugWithId(String baseSlug, Long id) {
        return baseSlug + "-c." + id;
    }

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