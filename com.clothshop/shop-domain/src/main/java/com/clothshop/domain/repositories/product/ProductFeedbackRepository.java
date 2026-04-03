package com.clothshop.domain.repositories.product;

import com.clothshop.domain.entities.product.ProductFeedback;
import com.clothshop.domain.entities.product.Product;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ProductFeedbackRepository extends JpaRepository<ProductFeedback, Long> {

    List<ProductFeedback> findByProductAndFeedbackStatusOrderByCreatedAtDesc(Product product, String feedbackStatus);

    List<ProductFeedback> findByProductOrderByCreatedAtDesc(Product product);

        @Query("SELECT pf FROM ProductFeedback pf " +
            "LEFT JOIN FETCH pf.product p " +
            "LEFT JOIN FETCH pf.customer c " +
            "ORDER BY pf.createdAt DESC")
        List<ProductFeedback> findAllWithProductAndCustomerOrderByCreatedAtDesc();

        @Query("SELECT pf FROM ProductFeedback pf " +
            "LEFT JOIN FETCH pf.product p " +
            "LEFT JOIN FETCH pf.customer c " +
            "WHERE pf.id = :id")
        java.util.Optional<ProductFeedback> findByIdWithProductAndCustomer(@Param("id") Long id);

        long countByFeedbackStatus(String feedbackStatus);
}
