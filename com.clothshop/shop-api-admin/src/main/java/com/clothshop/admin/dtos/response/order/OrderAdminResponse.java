package com.clothshop.admin.dtos.response.order;

import com.clothshop.domain.enums.OrderStatus;
import lombok.*;
import java.math.BigDecimal;
import java.time.LocalDateTime;

@Getter @Setter @Builder @NoArgsConstructor @AllArgsConstructor
public class OrderAdminResponse {
    private Long orderId;
    private String orderInvoice;
    private String customerName; // Chỉ lấy tên, không lấy cả object User
    private BigDecimal totalPrice;
    private Integer totalQuantity;
    private OrderStatus status;
    private String paymentMethod;
    private LocalDateTime createdAt;
}