package com.clothshop.admin.dtos.response.products;

import lombok.*;
import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * Response DTO for Product Variant (Admin side).
 * Contains all variant information including stock and price.
 */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class VariantResponse {

    private Long variantId;
    private String sku;
    private String color;
    private String sizeValue;
    private BigDecimal retailPrice;
    private Integer stockQuantity;
    private String imageUrl;
    private Boolean isActive;
    private Long productId;
    private String productName;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    private String createdBy;
    private String updatedBy;
}

