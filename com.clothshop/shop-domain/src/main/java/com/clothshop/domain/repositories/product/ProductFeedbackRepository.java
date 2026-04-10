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

    List<ProductFeedback> findByProductIdOrderByCreatedAtDesc(Long productId);

    boolean existsByCustomerAndProduct(Customer customer, Product product);

    Page<ProductFeedback> findByFeedbackStatusOrderByCreatedAtDesc(String feedbackStatus, Pageable pageable);

    Page<ProductFeedback> findAllByOrderByCreatedAtDesc(Pageable pageable);

    Optional<ProductFeedback> findByIdAndIsActiveTrue(Long id);

    Optional<ProductFeedback> findByIdAndProductIdAndIsActiveTrue(Long id, Long productId);
}
