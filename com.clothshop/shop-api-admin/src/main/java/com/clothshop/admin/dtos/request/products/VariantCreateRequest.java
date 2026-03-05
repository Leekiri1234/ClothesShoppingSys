package com.clothshop.admin.dtos.request.products;

import jakarta.validation.constraints.*;
import lombok.*;
import java.math.BigDecimal;

/**
 * Request DTO for creating a new product variant.
 * Sử dụng denormalized fields (color, sizeValue) để map 1-1 với DB,
 * tránh join bảng thừa thãi gây tốn I/O.
 */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class VariantCreateRequest {

    @NotNull(message = "Product ID is required.")
    private Long productId;

    @NotBlank(message = "Color is required.")
    @Size(max = 50, message = "Color must not exceed 50 characters.")
    private String color;

    @NotBlank(message = "Size is required.")
    @Size(max = 20, message = "Size must not exceed 20 characters.")
    private String sizeValue;

    @NotNull(message = "Price is required.")
    @DecimalMin(value = "0.0", inclusive = false, message = "Price must be greater than 0")
    private BigDecimal price;

    @NotNull(message = "Stock is required")
    @Min(value = 0, message = "Stock cannot be negative")
    private Integer stock;

    @Size(max = 500, message = "URL must not exceed 500 characters.")
    private String imageUrl;
}