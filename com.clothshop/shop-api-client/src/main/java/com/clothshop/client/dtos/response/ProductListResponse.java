package com.clothshop.client.dtos.response;

import lombok.*;

/**
 * Response DTO cho danh sách sản phẩm (Client/Public side).
 * Dùng chung cho: Trang chủ, Danh mục, Tìm kiếm và Bộ sưu tập.
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

    private Long id;
    private String name;
    private String slug;
    private String thumbnail;

    private Double originalPrice;
    private Double minPrice;
    private Integer totalStock;
    private Integer discountPercent;

    private Long defaultVariantId;
}