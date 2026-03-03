package com.clothshop.domain.entities.order;

import com.clothshop.domain.entities.base.BaseEntity;
import com.clothshop.domain.enums.PaymentMethod;
import com.clothshop.domain.enums.PaymentStatus;
import jakarta.persistence.*;
import lombok.*;
import lombok.experimental.SuperBuilder;
import org.hibernate.annotations.SQLDelete;
import org.hibernate.annotations.SQLRestriction;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(name = "payments")
@SQLDelete(sql = "UPDATE payments SET is_active = false WHERE id = ?")
@SQLRestriction("is_active = true")
@AttributeOverride(name = "id", column = @Column(name = "payment_id"))
@Getter @Setter @NoArgsConstructor @AllArgsConstructor @SuperBuilder
public class Payment extends BaseEntity {

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "order_id", unique = true, nullable = false)
    private Order order;

    @Enumerated(EnumType.STRING)
    @Column(name = "payment_method", length = 50)
    private PaymentMethod paymentMethod;

    @Column(name = "amount", precision = 10, scale = 2, nullable = false)
    private BigDecimal amount;

    @Enumerated(EnumType.STRING)
    @Column(name = "payment_status", length = 20)
    private PaymentStatus paymentStatus;

    @Column(name = "processed_by", length = 50)
    private String processedBy; // Staff username xác nhận thanh toán

    @Column(name = "processed_at")
    private LocalDateTime processedAt;

    @Column(name = "verified_at")
    private LocalDateTime verifiedAt;

    @Column(name = "refund_date")
    private LocalDateTime refundDate;
}
