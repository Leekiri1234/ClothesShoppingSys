package com.clothshop.client.dtos.response;

import lombok.*;

@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class CartItemResponse {
    private Long cartItemId;
    private Long variantId;
    private String productName;
    private String productSlug;
    private String colorName;
    private String sizeName;
    private Double price;
    private Integer quantity;
    private Double subtotal;
    private String imageUrl;
    private Integer maxStock; // Để UI chặn tăng số lượng vượt kho
}