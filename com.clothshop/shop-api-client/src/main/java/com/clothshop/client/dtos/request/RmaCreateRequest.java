package com.clothshop.client.dtos.request;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class RmaCreateRequest {
    private String orderInvoice; // Use invoice instead of ID for consistency with OrderClientController
    private String type; // RETURN, EXCHANGE
    private String reason;
}
