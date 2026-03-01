package com.clothshop.domain.entities.order;

import com.clothshop.domain.entities.base.BaseEntity;
import com.clothshop.domain.enums.OrderStatus;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.SQLDelete;
import org.hibernate.annotations.SQLRestriction;

@Entity
@Table(name = "order_status_history")
@SQLDelete(sql = "UPDATE order_status_history SET is_active = false WHERE id = ?")
@SQLRestriction("is_active = true")
@AttributeOverride(name = "id", column = @Column(name = "history_id"))
@Getter @Setter
public class OrderStatusHistory extends BaseEntity {

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "order_id", nullable = false)
    private Order order;

    @Enumerated(EnumType.STRING)
    @Column(name = "status_id", nullable = false, length = 20)
    private OrderStatus statusId;

    @Column(name = "changed_at")
    private java.time.LocalDateTime changedAt;

    @Lob
    @Column(name = "note")
    private String note;
}
