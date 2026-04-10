package com.clothshop.domain.repositories.marketing;

import com.clothshop.domain.models.marketing.FeaturedProduct;
import com.clothshop.domain.models.product.Product;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface FeaturedProductRepository extends JpaRepository<FeaturedProduct, Long> {

    @Query("SELECT fp FROM FeaturedProduct fp JOIN FETCH fp.product p WHERE fp.isActive = true AND p.isActive = true ORDER BY fp.displayOrder ASC")
    List<FeaturedProduct> findAllActiveFeaturedProducts();

    @Modifying
    @Query("UPDATE FeaturedProduct fp SET fp.isActive = false WHERE fp.product.id = :productId")
    void deactivateByProductId(@Param("productId") Long productId);

    // THÊM HÀM NÀY ĐỂ GIẢI QUYẾT UNIQUE CONSTRAINT:
    // Native query sẽ bỏ qua @SQLRestriction("is_active = true") để tìm được cả record đã bị xóa mềm
    @Query(value = "SELECT * FROM featured_products WHERE product_id = :productId LIMIT 1", nativeQuery = true)
    Optional<FeaturedProduct> findByProductIdIncludingInactive(@Param("productId") Long productId);

    @Modifying
    @Query("UPDATE FeaturedProduct f SET f.isActive = false, f.updatedBy = :username WHERE f.isActive = true")
    void deactivateAllFeaturedProducts(@Param("username") String username);

    @Query("SELECT fp.product FROM FeaturedProduct fp " +
            "JOIN fp.product p " +
            "LEFT JOIN FETCH p.category " +
            "WHERE fp.isActive = true AND p.isActive = true " +
            "ORDER BY fp.displayOrder ASC")
    List<Product> findTopFeaturedProducts(Pageable pageable);
}