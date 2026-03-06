package com.clothshop.domain.repositories.marketing;

import com.clothshop.domain.entities.marketing.FeaturedProduct;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface FeaturedProductRepository extends JpaRepository<FeaturedProduct, Long> {

    // Tối ưu N+1 Query bằng JOIN FETCH khi lấy dữ liệu ra trang chủ.
    // Lấy danh sách sản phẩm nổi bật đang active, sắp xếp theo thứ tự hiển thị
    @Query("SELECT fp FROM FeaturedProduct fp JOIN FETCH fp.product p WHERE fp.isActive = true AND p.isActive = true ORDER BY fp.displayOrder ASC")
    List<FeaturedProduct> findAllActiveFeaturedProducts();

    // Lấy thứ tự lớn nhất hiện tại để cộng dồn khi thêm sản phẩm nổi bật mới
    @Query("SELECT MAX(fp.displayOrder) FROM FeaturedProduct fp WHERE fp.isActive = true")
    Optional<Integer> findMaxDisplayOrder();

    // Kiểm tra xem sản phẩm đã được đưa lên trang chủ chưa (Tránh trùng lặp dữ liệu)
    boolean existsByProductIdAndIsActiveTrue(Long productId);

    // Xóa mềm một sản phẩm khỏi danh sách nổi bật thông qua Product ID
    @Modifying
    @Query("UPDATE FeaturedProduct fp SET fp.isActive = false WHERE fp.product.id = :productId")
    void deactivateByProductId(@Param("productId") Long productId);
}