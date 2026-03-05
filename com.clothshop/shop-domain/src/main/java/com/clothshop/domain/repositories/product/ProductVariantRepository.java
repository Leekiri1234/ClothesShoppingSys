package com.clothshop.domain.repositories.product;

import com.clothshop.domain.entities.product.ProductVariant;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface ProductVariantRepository extends JpaRepository<ProductVariant, Long> {
    boolean existsBySku(String sku);

    Optional<ProductVariant> findBySku(String sku);

    // Tính tổng tồn kho theo từng sản phẩm trong một lần truy vấn (tránh N+1)
    @Query("SELECT pv.product.id, SUM(pv.stockQuantity) FROM ProductVariant pv WHERE pv.product.id IN :productIds GROUP BY pv.product.id")
    List<Object[]> findTotalStockByProductIds(@Param("productIds") List<Long> productIds);
}