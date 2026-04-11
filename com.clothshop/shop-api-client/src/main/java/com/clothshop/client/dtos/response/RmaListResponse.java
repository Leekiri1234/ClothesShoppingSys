package com.clothshop.client.dtos.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class RmaListResponse {
    private Long rmaId;
    private String orderInvoice;
    private String rmaType;
    private String reason;
    private String status;
    private LocalDateTime processedAt;
    private String adminNote;
    private BigDecimal refundAmount;
    private LocalDateTime createdAt;
    private String evidenceImages;
}
