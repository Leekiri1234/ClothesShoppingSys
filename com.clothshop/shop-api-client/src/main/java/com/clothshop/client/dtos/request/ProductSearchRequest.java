package com.clothshop.client.dtos.request;

import com.clothshop.domain.enums.AvailabilityStatus;
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

    private AvailabilityStatus availabilityStatus = AvailabilityStatus.ALL;

    @Builder.Default
    private Integer page = 0;

    @Builder.Default
    private Integer size = 12;

    @Builder.Default
    private String sort = "newest";

    private Boolean isNew;
    private Boolean isSale;

    private Long filterMask;
}
