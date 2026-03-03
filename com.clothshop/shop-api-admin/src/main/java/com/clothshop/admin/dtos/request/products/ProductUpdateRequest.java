package com.clothshop.admin.dtos.request.products;

import jakarta.validation.constraints.*;
import lombok.*;

/**
 * Request DTO for updating an existing product (Admin side).
 * All fields are optional to support partial updates.
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
    private Double price;

    @Min(value = 0, message = "Stock cannot be negative")
    private Integer stock;

    private String imageUrl;

    private Boolean isActive;
}
