package com.clothshop.admin.dtos.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class SalesReportResponse implements Serializable {
    
    private BigDecimal totalRevenue;
    private Long totalOrders;
    private Long totalCustomers;
    private Long totalProducts;
    private List<DailySalesDTO> dailySales;
    private List<TopProductDTO> topProducts;
    
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class DailySalesDTO {
        private LocalDate date;
        private BigDecimal amount;
        private Long orderCount;
    }
    
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class TopProductDTO {
        private Long productId;
        private String productName;
        private Long quantity;
        private BigDecimal revenue;
    }
}
