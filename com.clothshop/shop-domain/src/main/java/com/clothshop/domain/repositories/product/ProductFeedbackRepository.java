package com.clothshop.domain.repositories.product;

import com.clothshop.domain.models.auth.Customer;
import com.clothshop.domain.models.product.ProductFeedback;
import com.clothshop.domain.models.product.Product;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ProductFeedbackRepository extends JpaRepository<ProductFeedback, Long> {

    List<ProductFeedback> findByProductAndFeedbackStatusOrderByCreatedAtDesc(Product product, String feedbackStatus);

    List<ProductFeedback> findByProductOrderByCreatedAtDesc(Product product);

    Page<ProductFeedback> findByProductIdAndFeedbackStatusOrderByCreatedAtDesc(Long productId, String feedbackStatus, Pageable pageable);

    boolean existsByCustomerAndProduct(Customer customer, Product product);
}
