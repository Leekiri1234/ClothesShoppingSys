package com.clothshop.domain.entities.order;

import com.clothshop.domain.entities.base.BaseEntity;
import com.clothshop.domain.entities.auth.Customer;
import com.clothshop.domain.enums.RmaStatus;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.SQLDelete;
import org.hibernate.annotations.SQLRestriction;

import java.time.LocalDateTime;

@Entity
@Table(name = "rma_requests")
@SQLDelete(sql = "UPDATE rma_requests SET is_active = false WHERE id = ?")
@SQLRestriction("is_active = true")
@AttributeOverride(name = "id", column = @Column(name = "rma_id"))
@Getter @Setter
public class RmaRequest extends BaseEntity {

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "order_id", nullable = false)
    private Order order;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "customer_id", nullable = false)
    private Customer customer;

    @Column(name = "rma_type", length = 20)
    private String rmaType; // RETURN, REFUND

    @Lob
    @Column(name = "reason", nullable = false)
    private String reason;

    @Enumerated(EnumType.STRING)
    @Column(name = "rma_status", length = 20)
    private RmaStatus rmaStatus; // PENDING, APPROVED, REJECTED

    @Column(name = "processed_at")
    private LocalDateTime processedAt;
}
