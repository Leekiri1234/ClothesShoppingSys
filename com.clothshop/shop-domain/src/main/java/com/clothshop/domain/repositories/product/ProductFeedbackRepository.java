package com.clothshop.domain.repositories.product;

import com.clothshop.domain.models.auth.Customer;
import com.clothshop.domain.models.product.Product;
import com.clothshop.domain.models.product.ProductFeedback;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.Collection;

@Repository
public interface ProductFeedbackRepository extends JpaRepository<ProductFeedback, Long> {

    List<ProductFeedback> findByProductAndFeedbackStatusOrderByCreatedAtDesc(Product product, String feedbackStatus);

    List<ProductFeedback> findByProductOrderByCreatedAtDesc(Product product);

    Page<ProductFeedback> findByProductIdAndFeedbackStatusOrderByCreatedAtDesc(Long productId, String feedbackStatus, Pageable pageable);

    Page<ProductFeedback> findByProductIdOrderByCreatedAtDesc(Long productId, Pageable pageable);

    Page<ProductFeedback> findByProductIdAndFeedbackStatusInOrderByCreatedAtDesc(Long productId, Collection<String> statuses, Pageable pageable);

    Page<ProductFeedback> findByProductIdAndRatingAndFeedbackStatusInOrderByCreatedAtDesc(Long productId, Integer rating, Collection<String> statuses, Pageable pageable);

    List<ProductFeedback> findByProductIdOrderByCreatedAtDesc(Long productId);

    boolean existsByCustomerAndProduct(Customer customer, Product product);

    Page<ProductFeedback> findByFeedbackStatusOrderByCreatedAtDesc(String feedbackStatus, Pageable pageable);

    Page<ProductFeedback> findAllByOrderByCreatedAtDesc(Pageable pageable);

    Optional<ProductFeedback> findByIdAndIsActiveTrue(Long id);

    Optional<ProductFeedback> findByIdAndProductIdAndIsActiveTrue(Long id, Long productId);

    @org.springframework.data.jpa.repository.Query("SELECT pf FROM ProductFeedback pf WHERE " +
           "(:status IS NULL OR pf.feedbackStatus = :status) AND " +
           "(:productId IS NULL OR pf.product.id = :productId) AND " +
           "(:rating IS NULL OR pf.rating = :rating) AND " +
           "(:keyword IS NULL OR LOWER(pf.product.productName) LIKE LOWER(CONCAT('%', :keyword, '%')) OR EXISTS (SELECT 1 FROM ProductVariant v WHERE v.product = pf.product AND LOWER(v.sku) LIKE LOWER(CONCAT('%', :keyword, '%')))) " +
           "ORDER BY pf.createdAt DESC")
    Page<ProductFeedback> searchAdminReviews(@org.springframework.data.repository.query.Param("status") String status,
                                             @org.springframework.data.repository.query.Param("productId") Long productId,
                                             @org.springframework.data.repository.query.Param("rating") Integer rating,
                                             @org.springframework.data.repository.query.Param("keyword") String keyword,
                                             Pageable pageable);
}
