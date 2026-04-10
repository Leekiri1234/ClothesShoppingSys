package com.clothshop.domain.models.order;

import com.clothshop.domain.models.auth.Customer;
import com.clothshop.domain.models.base.BaseEntity;
import com.clothshop.domain.enums.RmaStatus;
import com.clothshop.domain.enums.RmaType;
import jakarta.persistence.*;
import lombok.*;
import lombok.experimental.SuperBuilder;
import org.hibernate.annotations.SQLDelete;
import org.hibernate.annotations.SQLRestriction;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(name = "rma_requests")
@SQLDelete(sql = "UPDATE rma_requests SET is_active = false WHERE rma_id = ?")
@SQLRestriction("is_active = true")
@AttributeOverride(name = "id", column = @Column(name = "rma_id"))
@Getter @Setter @NoArgsConstructor @AllArgsConstructor @SuperBuilder
public class RmaRequest extends BaseEntity {

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "order_id", nullable = false)
    private Order order;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "customer_id", nullable = false)
    private Customer customer;

    @Enumerated(EnumType.STRING)
    @Column(name = "rma_type", nullable = false, length = 30)
    private RmaType rmaType;

    @Enumerated(EnumType.STRING)
    // SỬA TẠI ĐÂY: Dùng ngoặc huyền để thoát từ khóa 'status'
    @Column(name = "`status`", nullable = false, length = 30)
    @Builder.Default
    private RmaStatus status = RmaStatus.PENDING;

    // SỬA TẠI ĐÂY: Dùng length thay vì columnDefinition để tránh lỗi cú pháp SQL
    @Column(name = "reason", length = 1000, nullable = false)
    private String reason;

    @Column(name = "admin_note", length = 1000)
    private String adminNote;

    @Column(name = "refund_amount", precision = 12, scale = 2)
    private BigDecimal refundAmount;

    @Column(name = "evidence_images", length = 1000)
    private String evidenceImages;

    @Column(name = "processed_at")
    private LocalDateTime processedAt;
}