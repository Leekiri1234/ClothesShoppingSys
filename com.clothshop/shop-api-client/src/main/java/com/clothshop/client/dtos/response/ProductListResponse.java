package com.clothshop.client.dtos.response;

import lombok.*;

/**
 * Response DTO for Product List (Client/Public side).
 * Lightweight DTO for product listing pages.
 * Does NOT include full description to optimize memory.
 */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ProductListResponse {

    private Long productId;
    private String productName;
    private String productSlug;
    private String categoryName;
    private Double price;
    private String imageUrl;
    private Boolean available;
}
