package com.clothshop.admin.dtos.response.order;

import com.clothshop.domain.enums.RmaStatus;
import com.clothshop.domain.enums.RmaType;
import lombok.Builder;
import lombok.Data;
import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

@Data
@Builder
public class RmaAdminResponse {
    private Long id;
    private String orderInvoice;
    private Long orderId;
    private String customerName;
    private String customerPhone;
    private RmaType rmaType;       // Trả hàng hay Đổi hàng
    private RmaStatus status;      // Trạng thái hiện tại
    private String reason;
    private String adminNote;      // Phản hồi của admin
    private BigDecimal refundAmount; // Tiền hoàn
    private String evidenceImages;  // URL ảnh bằng chứng
    private LocalDateTime createdAt; // Ngày gửi yêu cầu
    private LocalDateTime updatedAt;
    private BigDecimal totalPrice;

    // Danh sách sản phẩm của đơn hàng
    private List<RmaOrderItemResponse> orderItems;

    @Getter
    @Setter
    public static class RmaOrderItemResponse {
        private String productName;
        private String sku;
        private String variantInfo; // VD: "Color: Blue, Size: L"
        private String imageUrl;
        private BigDecimal unitPrice;
        private Integer quantity;
    }
}