package com.clothshop.admin.dtos.response.products;

import com.clothshop.domain.enums.ProductStatus;
import lombok.*;

import java.time.LocalDateTime;

/**
 * Response DTO for Product (Admin side).
 * Includes administrative fields like stock, is_active.
 * Used for SSR views in admin panel.
 */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ProductAdminResponse {

    private Long productId;
    private String productName;
    private String productSlug;
    private Long categoryId;
    private String categoryName;
    private String description;
    private Double price;
    private Integer stock;
    private String imageUrl;
    private ProductStatus status;
    private Boolean isActive;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
