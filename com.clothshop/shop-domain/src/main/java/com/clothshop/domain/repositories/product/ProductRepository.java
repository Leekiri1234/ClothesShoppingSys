package com.clothshop.domain.repositories.product;

import com.clothshop.domain.entities.product.Product;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface ProductRepository extends JpaRepository<Product, Long> {
    Optional<Product> findByProductSlug(String slug);
    boolean existsByProductSlug(String productSlug);
    List<Product> findByCategoryId(Long categoryId);
    Page<Product> findAllByIsActiveTrue(Pageable pageable);
}