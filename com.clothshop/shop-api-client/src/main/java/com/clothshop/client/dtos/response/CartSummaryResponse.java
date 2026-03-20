package com.clothshop.client.dtos.response;

import lombok.*;
import java.util.List;

@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class CartSummaryResponse {
    private List<CartItemResponse> items;
    private Integer totalItems;
    private Double totalAmount;
}