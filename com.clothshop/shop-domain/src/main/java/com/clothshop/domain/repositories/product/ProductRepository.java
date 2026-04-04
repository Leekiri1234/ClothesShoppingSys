package com.clothshop.domain.repositories.product;

import com.clothshop.domain.entities.product.Product;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface ProductRepository extends JpaRepository<Product, Long>, JpaSpecificationExecutor<Product> {

    // --- NHÓM 1: LẤY CHI TIẾT (FETCH TẤT CẢ) ---
    // Dùng cho detail.html. Lấy 1 bản ghi nên fetch thoải mái, không lo tốn RAM hay MultipleBag.
    @EntityGraph(attributePaths = {"variants", "images", "category"})
    @Query("SELECT p FROM Product p WHERE p.productSlug = :slug AND p.isActive = true")
    Optional<Product> findByProductSlug(@Param("slug") String slug);

    @EntityGraph(attributePaths = {"variants", "images", "category"})
    @Query("SELECT p FROM Product p WHERE p.id = :id")
    Optional<Product> findProductWithDetailsById(@Param("id") Long id);

    // --- NHÓM 2: TRANG DANH SÁCH & PHÂN TRANG (CHỈ FETCH CATEGORY) ---
    // variants và images sẽ được nạp tự động theo lô nhờ @BatchSize và @Transactional.

    @EntityGraph(attributePaths = {"category"})
    @Query("SELECT p FROM Product p WHERE p.isActive = true")
    Page<Product> findAllByIsActiveTrue(Pageable pageable);

    @EntityGraph(attributePaths = {"category"})
    @Query("SELECT p FROM Product p WHERE p.category.id = :categoryId AND p.isActive = true")
    Page<Product> findByCategory_IdAndIsActiveTrue(@Param("categoryId") Long categoryId, Pageable pageable);

    @EntityGraph(attributePaths = {"category"})
    @Query("SELECT ci.product FROM CollectionItem ci WHERE ci.collection.slug = :slug AND ci.product.isActive = true")
    Page<Product> findByCollectionSlug(@Param("slug") String slug, Pageable pageable);

    @EntityGraph(attributePaths = {"category"})
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

    // --- NHÓM 3: TIỆN ÍCH KHÁC ---
    boolean existsByProductSlug(String productSlug);

    @Query("SELECT p.id, p.productName FROM Product p WHERE p.id IN :ids")
    List<Object[]> findIdAndProductNameByIdIn(@Param("ids") List<Long> ids);

    @EntityGraph(attributePaths = {"variants", "images", "category"})
    @Query("SELECT p FROM Product p WHERE p.isActive = true")
    Page<Product> findAllActive(Pageable pageable);

    @Query("SELECT DISTINCT p FROM Product p " +
            "LEFT JOIN FETCH p.variants v " +
            "WHERE p.isActive = true " +
            "ORDER BY p.createdAt DESC")
    List<Product> findAllActiveWithVariants();
    List<Product> findTop100ActiveProductsWithDetails(Pageable pageable);
}