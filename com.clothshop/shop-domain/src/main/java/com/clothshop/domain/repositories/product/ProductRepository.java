package com.clothshop.domain.repositories.product;

import com.clothshop.domain.entities.product.Product;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface ProductRepository extends JpaRepository<Product, Long> {

    @Query("SELECT p FROM Product p WHERE p.productSlug = :slug AND p.isActive = true")
    Optional<Product> findByProductSlug(@Param("slug") String slug);

    boolean existsByProductSlug(String productSlug);

    // Chỉ lấy sản phẩm thuộc danh mục đang hoạt động
    @Query("SELECT p FROM Product p WHERE p.category.id = :categoryId AND p.isActive = true")
    List<Product> findByCategoryId(@Param("categoryId") Long categoryId);

    /**
     * TỐI ƯU: Lấy tất cả sản phẩm active, không quan tâm danh mục active hay không.
     * Khi Category bị ẩn (is_active=false), p.category sẽ tự động null nhờ @SQLRestriction
     */
    @Query("SELECT p FROM Product p WHERE p.isActive = true")
    Page<Product> findAllByIsActiveTrue(Pageable pageable);

    @Query("SELECT p FROM Product p LEFT JOIN FETCH p.variants WHERE p.id = :id")
    Optional<Product> findProductWithVariantsById(@Param("id") Long id);

//    @Query("SELECT DISTINCT p FROM Product p LEFT JOIN FETCH p.variants")
//    List<Product> findAllProductsWithVariants();

    /**
     * TRANG CHỦ: Fetch luôn category bằng LEFT JOIN.
     * Nếu Category inactive -> p.category = null -> Mapper sẽ hiện "Chưa phân loại"
     */
    @Query("SELECT DISTINCT p FROM Product p " +
            "LEFT JOIN FETCH p.category " +
            "WHERE p.isActive = true " +
            "ORDER BY p.createdAt DESC")
    List<Product> findTop100ActiveProductsWithDetails(Pageable pageable);

    // Lọc theo tên + danh mục active
    // Lọc theo tên: Bỏ điều kiện check category ID
    @Query("SELECT p FROM Product p WHERE LOWER(p.productName) LIKE LOWER(CONCAT('%', :name, '%')) " + "AND p.isActive = true")
    Page<Product> findByProductNameContainingIgnoreCaseAndIsActiveTrue(@Param("name") String name, Pageable pageable);

    /**
     * Hàm lọc theo danh mục thì VẪN GIỮ NGUYÊN hoặc tùy biến.
     * Nếu user vào đúng link danh mục đã ẩn, ta có thể trả về trống hoặc báo lỗi 404 ở Service.
     */
    @Query("SELECT p FROM Product p WHERE p.category.id = :categoryId AND p.isActive = true")
    Page<Product> findByCategory_IdAndIsActiveTrue(@Param("categoryId") Long categoryId, Pageable pageable);

    @Query("SELECT ci.product FROM CollectionItem ci " +
            "WHERE ci.collection.slug = :slug " +
            "AND ci.product.isActive = true")
    Page<Product> findByCollectionSlug(@Param("slug") String slug, Pageable pageable);

    @Query("SELECT DISTINCT p FROM Product p " +
            "LEFT JOIN p.category cat " +
            "LEFT JOIN p.collectionItems ci " +
            "LEFT JOIN ci.collection col " +
            "WHERE p.isActive = true " +
            "AND (" +
            "   LOWER(p.productName) LIKE LOWER(CONCAT('%', :keyword, '%')) " +
            "   OR (cat IS NOT NULL AND LOWER(cat.categoryName) LIKE LOWER(CONCAT('%', :keyword, '%'))) " +
            "   OR (col IS NOT NULL AND LOWER(col.name) LIKE LOWER(CONCAT('%', :keyword, '%')))" +
            ")")
    Page<Product> searchFullText(@Param("keyword") String keyword, Pageable pageable);

    @Query("SELECT p FROM Product p WHERE p.isActive = true")
    Page<Product> findAllActive(Pageable pageable);
}