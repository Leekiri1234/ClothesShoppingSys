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
    List<Product> findByCategoryId(Long categoryId);
    Page<Product> findAllByIsActiveTrue(Pageable pageable);
    @Query("SELECT p FROM Product p LEFT JOIN FETCH p.variants WHERE p.id = :id")
    Optional<Product> findProductWithVariantsById(@Param("id") Long id);
    // Thêm từ khóa DISTINCT để tránh bị duplicate data khi JOIN OneToMany
    @Query("SELECT DISTINCT p FROM Product p LEFT JOIN FETCH p.variants")
    List<Product> findAllProductsWithVariants();

    // Query với JOIN FETCH category (tránh N+1)
    // Note: Không fetch images và variants ở đây để tránh MultipleBagFetchException
    // Controller sẽ initialize lazy collections khi cần
    @Query("SELECT DISTINCT p FROM Product p " +
           "LEFT JOIN FETCH p.category " +
           "WHERE p.isActive = true " +
           "ORDER BY p.createdAt DESC")
    List<Product> findTop100ActiveProductsWithDetails(Pageable pageable);
}