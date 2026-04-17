package com.clothshop.admin.dtos.response.marketing;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class WishlistInsightsResponse {
    private String search;
    private LocalDate startDate;
    private LocalDate endDate;
    private Long selectedCategoryId;
    private Long selectedProductId;
    private Long totalWishlistItems;
    private Long totalWishlistedProducts;
    private Long totalCustomers;
    private List<OptionDTO> categoryOptions;
    private List<OptionDTO> productOptions;
    private List<TopProductDTO> topProducts;
    private List<TrendPointDTO> trend;

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class OptionDTO {
        private Long id;
        private String name;
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class TopProductDTO {
        private Long productId;
        private String productName;
        private String categoryName;
        private Long wishlistCount;
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class TrendPointDTO {
        private LocalDate date;
        private Long wishlistCount;
    }
}
