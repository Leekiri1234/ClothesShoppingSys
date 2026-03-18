package com.clothshop.client.dtos.request;

import lombok.Data;

@Data
public class OrderCancellationRequest {
    private String orderInvoice;
    private String reason;
}

