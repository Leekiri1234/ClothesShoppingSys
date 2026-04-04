package com.clothshop.domain.repositories.product;

import com.clothshop.domain.entities.product.ProductImage;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface ProductImageRepository extends JpaRepository<ProductImage, Long> {
    // Lấy album ảnh của một sản phẩm
    List<ProductImage> findByProductId(Long productId);

    // Tìm ảnh chính của sản phẩm
    java.util.Optional<ProductImage> findByProductIdAndIsMainTrue(Long productId);

    @Query("SELECT pi.product.id, pi.imageUrl FROM ProductImage pi " +
            "WHERE pi.product.id IN :productIds AND pi.isMain = true")
    List<Object[]> findMainImageUrlsByProductIds(@Param("productIds") List<Long> productIds);
}