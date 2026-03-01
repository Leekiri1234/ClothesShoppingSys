package com.clothshop.domain.entities.product;

import com.clothshop.domain.entities.base.BaseEntity;
import com.clothshop.domain.entities.auth.Customer;
import com.clothshop.domain.entities.order.Order;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.SQLDelete;
import org.hibernate.annotations.SQLRestriction;

import java.time.LocalDateTime;

/**
 * Product Feedback - Đánh giá sản phẩm từ khách hàng.
 */
@Entity
@Table(name = "product_feedback")
@SQLDelete(sql = "UPDATE product_feedback SET is_active = false WHERE id = ?")
@SQLRestriction("is_active = true")
@AttributeOverride(name = "id", column = @Column(name = "feedback_id"))
@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class ProductFeedback extends BaseEntity {

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "product_id", nullable = false)
    private Product product;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "customer_id", nullable = false)
    private Customer customer;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "order_id")
    private Order order; // Chỉ cho phép đánh giá nếu đã mua

    @Column(name = "rating", nullable = false)
    private Integer rating; // 1-5 sao

    @Lob
    @Column(name = "comment")
    private String comment;

    @Column(name = "feedback_status", length = 20)
    private String feedbackStatus; // PENDING, APPROVED, REJECTED (kiểm duyệt)

    @Column(name = "moderated_at")
    private LocalDateTime moderatedAt;

    @Column(name = "moderated_by", length = 50)
    private String moderatedBy; // Staff username
}
