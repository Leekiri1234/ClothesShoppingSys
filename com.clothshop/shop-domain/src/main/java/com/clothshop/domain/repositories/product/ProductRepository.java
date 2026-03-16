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

    Optional<Product> findByProductSlug(String slug);

    boolean existsByProductSlug(String productSlug);

    // Chỉ lấy sản phẩm thuộc danh mục đang hoạt động
    @Query("SELECT p FROM Product p WHERE p.category.id = :categoryId AND p.isActive = true")
    List<Product> findByCategoryId(@Param("categoryId") Long categoryId);

    // Lấy tất cả sản phẩm active và có danh mục active
    @Query("SELECT p FROM Product p WHERE p.isActive = true AND (p.category IS NULL OR p.category.id IS NOT NULL)")
    Page<Product> findAllByIsActiveTrue(Pageable pageable);

    @Query("SELECT p FROM Product p LEFT JOIN FETCH p.variants WHERE p.id = :id")
    Optional<Product> findProductWithVariantsById(@Param("id") Long id);

    @Query("SELECT DISTINCT p FROM Product p LEFT JOIN FETCH p.variants")
    List<Product> findAllProductsWithVariants();

    /**
     * TÌM KIẾM SẢN PHẨM TRANG CHỦ
     * Thêm điều kiện p.category.id IS NOT NULL để đảm bảo không lấy sản phẩm có danh mục bị ẩn
     */
    @Query("SELECT DISTINCT p FROM Product p " +
            "LEFT JOIN FETCH p.category " +
            "WHERE p.isActive = true " +
            "AND (p.category IS NULL OR p.category.id IS NOT NULL) " +
            "ORDER BY p.createdAt DESC")
    List<Product> findTop100ActiveProductsWithDetails(Pageable pageable);

    // Lọc theo tên + danh mục active
    @Query("SELECT p FROM Product p WHERE LOWER(p.productName) LIKE LOWER(CONCAT('%', :name, '%')) " +
            "AND p.isActive = true AND (p.category IS NULL OR p.category.id IS NOT NULL)")
    Page<Product> findByProductNameContainingIgnoreCaseAndIsActiveTrue(@Param("name") String name, Pageable pageable);

    // Lọc theo ID danh mục (chỉ lấy nếu danh mục đó tồn tại/active)
    @Query("SELECT p FROM Product p WHERE p.category.id = :categoryId AND p.isActive = true")
    Page<Product> findByCategory_IdAndIsActiveTrue(@Param("categoryId") Long categoryId, Pageable pageable);

    @Query("SELECT ci.product FROM CollectionItem ci " +
            "WHERE ci.collection.slug = :slug " +
            "AND ci.product.isActive = true " +
            "AND (ci.product.category IS NULL OR ci.product.category.id IS NOT NULL)")
    Page<Product> findByCollectionSlug(@Param("slug") String slug, Pageable pageable);
}