package com.clothshop.client.dtos.response;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class DirectPurchaseItemDTO {
    private Long variantId;
    private String productName;
    private String imageUrl;
    private String color;
    private String sizeValue;
    private int quantity;
    private double unitPrice;
    private double totalPrice;
}