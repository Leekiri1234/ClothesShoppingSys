package com.clothshop.admin.dtos.response.order;

import com.clothshop.domain.enums.OrderStatus;
import lombok.*;
import java.time.LocalDateTime;

@Data @Builder @NoArgsConstructor @AllArgsConstructor
public class OrderStatusHistoryResponse {
    private OrderStatus oldStatus;
    private OrderStatus newStatus;
    private String changedBy;
    private LocalDateTime changedAt;
    private String note;
}