package com.clothshop.client.dtos.response;

import com.clothshop.domain.enums.OrderStatus;
import com.clothshop.domain.enums.PaymentMethod;
import lombok.*;

import java.time.LocalDateTime;
import java.util.List;

@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class OrderDetailResponse {
    private Long id;
    private String orderInvoice;
    private LocalDateTime createdAt;
    private OrderStatus status;

    private Double totalAmount; // Tổng tiền hàng
    private Double discount;    // Giảm giá
    private Double totalPrice;  // Tiền thanh toán cuối

    private String shippingAddress;
    private String customerPhone;
    private String paymentMethod;

    private List<OrderItemClientResponse> items;
    private List<OrderHistoryClientResponse> history;

    @Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
    public static class OrderItemClientResponse {
        private String productName;
        private String productSlug;
        private String variantInfo; // Ví dụ: "Đen | M"
        private String imageUrl;
        private Integer quantity;
        private Double unitPrice;
        private Double subtotal;
    }

    @Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
    public static class OrderHistoryClientResponse {
        private OrderStatus statusId;
        private String note;
        private LocalDateTime changedAt;
    }
}