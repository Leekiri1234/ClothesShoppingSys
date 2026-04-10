package com.clothshop.client.dtos.request;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class RmaCreateRequest {
    private String orderInvoice; // Use invoice instead of ID for consistency with OrderClientController
    private String type; // RETURN, EXCHANGE
    private String reason;
    private List<MultipartFile> evidenceImages;
}
