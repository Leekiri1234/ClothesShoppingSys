package com.clothshop.admin.dtos.request.order;

import com.clothshop.domain.enums.RmaStatus;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.math.BigDecimal;

@Data
public class RmaStatusUpdateRequest {
    @NotNull(message = "Trạng thái mới không được để trống")
    private RmaStatus status;

    private String adminNote;

    private BigDecimal refundAmount; // Số tiền chốt hoàn lại cho khách
}