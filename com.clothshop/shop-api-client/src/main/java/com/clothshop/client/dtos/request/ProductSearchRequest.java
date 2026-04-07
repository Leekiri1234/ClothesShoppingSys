package com.clothshop.client.dtos.request;

import lombok.*;

import java.util.List;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ProductSearchRequest {
    private String keyword;
    private List<Long> categoryIds;
    private String collectionSlug;
    private Double minPrice;
    private Double maxPrice;
    private List<String> sizes;
    private List<String> colors;

    @Builder.Default
    private Integer page = 0;

    @Builder.Default
    private Integer size = 12;

    @Builder.Default
    private String sort = "newest";
}
