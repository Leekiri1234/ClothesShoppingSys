package com.clothshop.admin.dtos.response.order;

import com.clothshop.domain.enums.OrderStatus;
import lombok.*;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

@Getter @Setter @Builder @NoArgsConstructor @AllArgsConstructor
public class OrderDetailResponse {
    private Long orderId;
    private String orderInvoice;
    private String customerName;
    private String customerEmail;
    private String customerPhone;
    private BigDecimal totalAmount;
    private BigDecimal discount;
    private BigDecimal totalPrice;
    private OrderStatus status;
    private LocalDateTime createdAt;

    private List<OrderItemResponse> items;
    private List<OrderStatusHistoryResponse> history;
}

