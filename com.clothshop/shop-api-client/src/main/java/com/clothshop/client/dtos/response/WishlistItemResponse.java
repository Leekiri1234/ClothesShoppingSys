package com.clothshop.client.dtos.response;

import lombok.*;

@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class WishlistItemResponse {
    private Long itemId;
    private Long productId;
    private String productName;
    private String productSlug;
    private Double price;
    private String thumbnail;
    private String categoryName;
}
