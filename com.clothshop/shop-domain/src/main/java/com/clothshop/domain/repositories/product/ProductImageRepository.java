package com.clothshop.domain.repositories.product;

import com.clothshop.domain.entities.product.ProductImage;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface ProductImageRepository extends JpaRepository<ProductImage, Long> {
    // Lấy album ảnh của một sản phẩm
    List<ProductImage> findByProductId(Long productId);

    // Tìm ảnh chính của sản phẩm
    java.util.Optional<ProductImage> findByProductIdAndIsMainTrue(Long productId);
}