package com.clothshop.domain.repositories.customer;

import com.clothshop.domain.models.customer.WishlistItem;
import com.clothshop.domain.projections.WishlistCustomerSummary;
import com.clothshop.domain.projections.WishlistProductSummary;
import com.clothshop.domain.projections.WishlistTrendSummary;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface WishlistItemRepository extends JpaRepository<WishlistItem, Long> {
    boolean existsByWishlistIdAndProductId(Long wishlistId, Long productId);
    void deleteByWishlistIdAndProductId(Long wishlistId, Long productId);
    WishlistItem findByWishlistIdAndProductId(Long wishlistId, Long ProductId);

    @Query("SELECT p.id AS productId, p.productName AS productName, cat.categoryName AS categoryName, COUNT(wi.id) AS wishlistCount " +
            "FROM WishlistItem wi " +
            "JOIN wi.wishlist w " +
            "JOIN wi.product p " +
            "LEFT JOIN p.category cat " +
            "WHERE wi.isActive = true " +
            "AND w.isActive = true " +
            "AND p.isActive = true " +
            "AND wi.createdAt BETWEEN :start AND :end " +
            "AND (:categoryId IS NULL OR cat.id = :categoryId) " +
            "AND (:productId IS NULL OR p.id = :productId) " +
            "GROUP BY p.id, p.productName, cat.categoryName " +
            "ORDER BY COUNT(wi.id) DESC, p.productName ASC")
    List<WishlistProductSummary> findTopWishlistedProducts(@Param("start") LocalDateTime start,
                                                           @Param("end") LocalDateTime end,
                                                           @Param("categoryId") Long categoryId,
                                                           @Param("productId") Long productId,
                                                           Pageable pageable);

    @Query("SELECT COUNT(wi.id) " +
            "FROM WishlistItem wi " +
            "JOIN wi.wishlist w " +
            "JOIN wi.product p " +
            "LEFT JOIN p.category cat " +
            "WHERE wi.isActive = true " +
            "AND w.isActive = true " +
            "AND p.isActive = true " +
            "AND wi.createdAt BETWEEN :start AND :end " +
            "AND (:categoryId IS NULL OR cat.id = :categoryId) " +
            "AND (:productId IS NULL OR p.id = :productId)")
    Long countWishlists(@Param("start") LocalDateTime start,
                        @Param("end") LocalDateTime end,
                        @Param("categoryId") Long categoryId,
                        @Param("productId") Long productId);

    @Query("SELECT COUNT(DISTINCT c.id) " +
            "FROM WishlistItem wi " +
            "JOIN wi.wishlist w " +
            "JOIN w.customer c " +
            "JOIN wi.product p " +
            "LEFT JOIN p.category cat " +
            "WHERE wi.isActive = true " +
            "AND w.isActive = true " +
            "AND c.isActive = true " +
            "AND p.isActive = true " +
            "AND wi.createdAt BETWEEN :start AND :end " +
            "AND (:categoryId IS NULL OR cat.id = :categoryId) " +
            "AND (:productId IS NULL OR p.id = :productId)")
    Long countDistinctCustomers(@Param("start") LocalDateTime start,
                                @Param("end") LocalDateTime end,
                                @Param("categoryId") Long categoryId,
                                @Param("productId") Long productId);

    @Query("SELECT COUNT(DISTINCT p.id) " +
            "FROM WishlistItem wi " +
            "JOIN wi.wishlist w " +
            "JOIN wi.product p " +
            "LEFT JOIN p.category cat " +
            "WHERE wi.isActive = true " +
            "AND w.isActive = true " +
            "AND p.isActive = true " +
            "AND wi.createdAt BETWEEN :start AND :end " +
            "AND (:categoryId IS NULL OR cat.id = :categoryId) " +
            "AND (:productId IS NULL OR p.id = :productId)")
    Long countDistinctProducts(@Param("start") LocalDateTime start,
                               @Param("end") LocalDateTime end,
                               @Param("categoryId") Long categoryId,
                               @Param("productId") Long productId);

    @Query("SELECT FUNCTION('DATE', wi.createdAt) AS date, COUNT(wi.id) AS wishlistCount " +
            "FROM WishlistItem wi " +
            "JOIN wi.wishlist w " +
            "JOIN wi.product p " +
            "LEFT JOIN p.category cat " +
            "WHERE wi.isActive = true " +
            "AND w.isActive = true " +
            "AND p.isActive = true " +
            "AND wi.createdAt BETWEEN :start AND :end " +
            "AND (:categoryId IS NULL OR cat.id = :categoryId) " +
            "AND (:productId IS NULL OR p.id = :productId) " +
            "GROUP BY FUNCTION('DATE', wi.createdAt) " +
            "ORDER BY FUNCTION('DATE', wi.createdAt)")
    List<WishlistTrendSummary> getWishlistTrend(@Param("start") LocalDateTime start,
                                                @Param("end") LocalDateTime end,
                                                @Param("categoryId") Long categoryId,
                                                @Param("productId") Long productId);

    @Query("SELECT c.id AS customerId, c.fullName AS customerName, c.email AS customerEmail, wi.createdAt AS wishlistedAt " +
            "FROM WishlistItem wi " +
            "JOIN wi.wishlist w " +
            "JOIN w.customer c " +
            "WHERE wi.isActive = true " +
            "AND w.isActive = true " +
            "AND c.isActive = true " +
            "AND wi.product.id = :productId " +
            "AND wi.createdAt BETWEEN :start AND :end " +
            "ORDER BY wi.createdAt DESC")
    List<WishlistCustomerSummary> findCustomersByProduct(@Param("productId") Long productId,
                                                         @Param("start") LocalDateTime start,
                                                         @Param("end") LocalDateTime end);
}
