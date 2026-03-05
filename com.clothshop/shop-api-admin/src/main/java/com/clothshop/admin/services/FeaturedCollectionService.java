package com.clothshop.admin.services;

import com.clothshop.common.exceptions.BusinessException;
import com.clothshop.common.exceptions.ErrorCode;
import com.clothshop.domain.entities.marketing.Collection;
import com.clothshop.domain.entities.marketing.CollectionItem;
import com.clothshop.domain.entities.product.Product;
import com.clothshop.domain.repositories.marketing.CollectionItemRepository;
import com.clothshop.domain.repositories.marketing.CollectionRepository;
import com.clothshop.domain.repositories.product.ProductRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Slf4j
public class FeaturedCollectionService {

    private final CollectionRepository collectionRepository;
    private final ProductRepository productRepository;
    private final CollectionItemRepository collectionItemRepository;

    /**
     * Thêm sản phẩm vào bộ sưu tập.
     * Kiểm tra trùng lặp để tránh lỗi UniqueConstraint trong database.
     */
    @Transactional
    public void addProductToCollection(Long collectionId, Long productId, String username) {
        log.info("Adding product ID: {} to collection ID: {} by admin: {}", productId, collectionId, username);

        // 1. Validate sự tồn tại của Collection và Product
        Collection collection = collectionRepository.findById(collectionId)
                .orElseThrow(() -> new BusinessException(ErrorCode.RESOURCE_NOT_FOUND, "Không tìm thấy bộ sưu tập"));

        Product product = productRepository.findById(productId)
                .orElseThrow(() -> new BusinessException(ErrorCode.RESOURCE_NOT_FOUND, "Không tìm thấy sản phẩm"));

        // 2. Kiểm tra sản phẩm đã nằm trong bộ sưu tập này chưa
        // Dùng CollectionItemRepository.existsByCollectionIdAndProductId nếu bạn đã thêm method này,
        // hoặc check size của list kết quả.
        boolean isExisted = collectionItemRepository.findByCollectionIdOrderByDisplayOrderAsc(collectionId)
                .stream()
                .anyMatch(item -> item.getProduct().getId().equals(productId));

        if (isExisted) {
            throw new BusinessException(ErrorCode.DUPLICATE_RESOURCE, "Sản phẩm đã tồn tại trong bộ sưu tập này");
        }

        // 3. Tạo bản ghi trung gian CollectionItem
        CollectionItem collectionItem = CollectionItem.builder()
                .collection(collection)
                .product(product)
                .addedBy(username)
                .displayOrder(0) // Mặc định lên đầu hoặc cuối tùy logic UI
                .isActive(true)
                .build();

        collectionItemRepository.save(collectionItem);
        log.info("Product successfully linked to collection");
    }
}