package com.clothshop.client.dtos.response;

import lombok.*;

@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class CheckoutSummaryResponse {
    private Double totalAmount;
    private Double discount;
    private Double finalAmount;
    private String voucherCode;
    private String voucherMessage;
}