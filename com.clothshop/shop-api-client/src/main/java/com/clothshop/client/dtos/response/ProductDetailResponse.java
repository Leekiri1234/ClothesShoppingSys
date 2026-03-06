package com.clothshop.client.dtos.response;

import lombok.*;

import java.util.List;

/**
 * Response DTO for Product Detail (Client/Public side).
 * Excludes sensitive administrative data like stock levels.
 * Optimized for public viewing.
 */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ProductDetailResponse {

    private Long productId;
    private String productName;
    private String productSlug;
    private String categoryName;
    private String description;
    private Double price;
    private String imageUrl;
    private Boolean available; // Derived from stock > 0, not exposing actual stock count
    private List<VariantDetailResponse> variants;
    private List<String> images;
}
