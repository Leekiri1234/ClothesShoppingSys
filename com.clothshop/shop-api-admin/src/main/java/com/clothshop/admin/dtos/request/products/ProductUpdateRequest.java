package com.clothshop.admin.dtos.request.products;

import jakarta.validation.constraints.*;
import lombok.*;

import java.math.BigDecimal;

/**
 * Request DTO for updating an existing product (Admin side).
 * All fields are optional to support partial updates.
 * Note: Stock is NOT included here as it's managed at the variant level.
 */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ProductUpdateRequest {

    @Size(max = 255, message = "Product name must not exceed 255 characters")
    private String productName;

    private Long categoryId;

    @Size(max = 5000, message = "Description must not exceed 5000 characters")
    private String description;

    @DecimalMin(value = "0.0", inclusive = false, message = "Price must be greater than 0")
    private BigDecimal price;

    private String imageUrl;

    private Boolean isActive;
}
