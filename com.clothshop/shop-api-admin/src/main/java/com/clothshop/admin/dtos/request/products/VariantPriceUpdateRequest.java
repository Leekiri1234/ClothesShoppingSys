package com.clothshop.admin.dtos.request.products;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotNull;
import lombok.*;

import java.math.BigDecimal;

/**
 * Request DTO dùng để cập nhật giá bán lẻ của một biến thể sản phẩm.
 */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class VariantPriceUpdateRequest {

    @NotNull(message = "Price is required")
    @DecimalMin(value = "0.0", inclusive = false, message = "Price must be greater than 0")
    private BigDecimal price;
}
