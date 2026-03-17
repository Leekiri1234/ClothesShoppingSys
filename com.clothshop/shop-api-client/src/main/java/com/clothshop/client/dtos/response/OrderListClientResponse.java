package com.clothshop.client.dtos.response;

import com.clothshop.domain.enums.OrderStatus;
import lombok.*;
import java.time.LocalDateTime;

@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class OrderListClientResponse {
    private Long id;
    private String orderInvoice;
    private LocalDateTime createdAt;
    private Double totalPrice; // final price
    private OrderStatus status;
}