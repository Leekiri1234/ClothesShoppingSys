package com.clothshop.admin.dtos.request.marketing;

import jakarta.validation.constraints.NotNull;
import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class FeaturedProductRequest {

    @NotNull(message = "Product ID không được để trống")
    private Long productId;

    @NotNull(message = "Thứ tự hiển thị không được để trống")
    private Integer displayOrder;
}