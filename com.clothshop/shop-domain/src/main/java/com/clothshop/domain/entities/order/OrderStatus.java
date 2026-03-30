package com.clothshop.domain.entities.order;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "order_status")
@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class OrderStatus {
    @Id
    @Column(name = "status_id")
    private Integer id;

    @Column(name = "status_name", nullable = false, length = 50)
    private String statusName; // Ví dụ: Chờ xử lý, Đang giao...
}