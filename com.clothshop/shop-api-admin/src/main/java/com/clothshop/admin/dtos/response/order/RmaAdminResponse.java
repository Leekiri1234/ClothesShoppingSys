package com.clothshop.admin.dtos.response.order;

import com.clothshop.domain.enums.RmaStatus;
import com.clothshop.domain.enums.RmaType;
import lombok.Builder;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

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
}