package com.clothshop.admin.dtos.response.marketing;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class WishlistProductDetailResponse {
    private Long productId;
    private String productName;
    private LocalDate startDate;
    private LocalDate endDate;
    private Long totalWishlists;
    private Long totalCustomers;
    private List<WishlistInsightsResponse.TrendPointDTO> trend;
    private List<CustomerWishlistDTO> customers;

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class CustomerWishlistDTO {
        private Long customerId;
        private String customerName;
        private String customerEmail;
        private LocalDateTime wishlistedAt;
    }
}
