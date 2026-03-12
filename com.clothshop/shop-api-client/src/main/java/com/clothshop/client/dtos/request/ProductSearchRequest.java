package com.clothshop.client.dtos.request;

import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ProductSearchRequest {
    private String keyword;
    private Long categoryId;
    private String collectionSlug;
    private Integer page = 0;
    private Integer size = 12;
    private String sort = "newest";
}

