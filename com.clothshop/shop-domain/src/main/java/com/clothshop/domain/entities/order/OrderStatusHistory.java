package com.clothshop.domain.entities.order;

import com.clothshop.domain.entities.base.BaseEntity;
import com.clothshop.domain.enums.OrderStatus;
import jakarta.persistence.*;
import lombok.*;
import lombok.experimental.SuperBuilder;

import java.time.LocalDateTime;

@Entity
@Table(name = "order_status_history")
@AttributeOverride(name = "id", column = @Column(name = "history_id"))
@Getter @Setter @NoArgsConstructor @AllArgsConstructor @SuperBuilder
public class OrderStatusHistory extends BaseEntity {

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "order_id", nullable = false)
    private Order order;

    @Enumerated(EnumType.STRING)
    @Column(name = "old_status", length = 20)
    private OrderStatus oldStatus; // Trạng thái trước khi đổi

    @Enumerated(EnumType.STRING)
    @Column(name = "new_status", length = 20, nullable = false)
    private OrderStatus newStatus; // Trạng thái sau khi đổi

    @Column(name = "changed_at", nullable = false)
    private LocalDateTime changedAt;

    @Column(name = "note", length = 500)
    private String note; // Ghi chú lý do đổi trạng thái (ví dụ: "Khách gọi điện hủy")

    // createdBy (người đổi) và createdAt đã có sẵn từ BaseEntity
}