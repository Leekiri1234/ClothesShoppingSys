package com.clothshop.client.dtos.response;

import lombok.*;

import java.math.BigDecimal;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class VariantDetailResponse {
    private Long id;
    private String sku;
    private String color;
    private String sizeValue;
    private BigDecimal retailPrice;
    private Integer stockQuantity;
    private String imageUrl;
}