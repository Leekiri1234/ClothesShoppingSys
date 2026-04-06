package com.clothshop.admin.services;

import com.clothshop.admin.dtos.response.dashboard.RecentOrderDTO;
import com.clothshop.admin.dtos.response.dashboard.RevenueDTO;
import com.clothshop.admin.dtos.response.dashboard.TopProductDTO;
import com.clothshop.admin.dtos.response.SalesReportResponse;
import com.clothshop.admin.services.FeaturedProductService;
import com.clothshop.domain.entities.order.Order;
import com.clothshop.domain.entities.product.Product;
import com.clothshop.domain.enums.OrderStatus;
import com.clothshop.domain.repositories.order.OrderRepository;
import com.clothshop.domain.projections.DailyRevenueSummary;
import com.clothshop.domain.projections.ProductSalesSummary;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.*;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class ReportService {
    private final OrderRepository orderRepository;
    private final FeaturedProductService featuredProductService;
    private static final LocalDate HISTORIC_TOP_PRODUCT_START = LocalDate.of(2000, 1, 1);
    private static final List<OrderStatus> SALES_STATUSES = List.of(
            OrderStatus.PENDING,
            OrderStatus.CONFIRMED,
            OrderStatus.SHIPPING,
            OrderStatus.DELIVERED,
            OrderStatus.COMPLETED
    );
    private static final List<OrderStatus> REVENUE_STATUSES = List.of(
            OrderStatus.DELIVERED,
            OrderStatus.COMPLETED
    );

    /**
     * Get sales report for the specified date range
     */
    public SalesReportResponse getSalesReport(LocalDate startDate, LocalDate endDate) {
        log.info("Generating sales report from {} to {}", startDate, endDate);

        LocalDateTime startDateTime = startDate.atStartOfDay();
        LocalDateTime endDateTime = endDate.atTime(LocalTime.MAX);

        List<Order> salesOrders = orderRepository.findSalesOrders(SALES_STATUSES, startDateTime, endDateTime);
        List<Long> featuredProductIds = getFeaturedProductCandidateIds();
        List<SalesReportResponse.TopProductDTO> topProducts = getTopProducts(salesOrders, 5, featuredProductIds, true, true);
        List<SalesReportResponse.DailySalesDTO> dailySales = groupSalesByDate(salesOrders, startDate, endDate);

        if (salesOrders.isEmpty()) {
            return SalesReportResponse.builder()
                    .totalRevenue(BigDecimal.ZERO)
                    .totalOrders(0L)
                    .totalCustomers(0L)
                    .totalProducts(0L)
                    .dailySales(dailySales)
                    .topProducts(topProducts)
                    .build();
        }

        BigDecimal totalRevenue = BigDecimal.ZERO;
        long totalProductsSold = 0;
        Set<Long> customerIds = new HashSet<>();

        for (Order order : salesOrders) {
            if (order.getTotalPrice() != null) {
                totalRevenue = totalRevenue.add(order.getTotalPrice());
            }
            if (order.getCustomer() != null && order.getCustomer().getId() != null) {
                customerIds.add(order.getCustomer().getId());
            }
            if (order.getOrderItems() != null) {
                for (var item : order.getOrderItems()) {
                    if (item != null && item.getQuantity() != null) {
                        totalProductsSold += item.getQuantity();
                    }
                }
            }
        }

        // already computed above when salesOrders non-empty

        return SalesReportResponse.builder()
                .totalRevenue(totalRevenue)
                .totalOrders((long) salesOrders.size())
                .totalCustomers((long) customerIds.size())
                .totalProducts(totalProductsSold)
                .dailySales(dailySales)
                .topProducts(topProducts)
                .build();
    }
    
    /**
     * Get today's sales summary
     */
    public SalesReportResponse getTodayReport() {
        return getSevenDayReportWithGlobalCounts();
    }
    
    /**
     * Get last 7 days sales summary
     */
    public SalesReportResponse getWeeklyReport() {
        LocalDate today = LocalDate.now();
        LocalDate sevenDaysAgo = today.minusDays(7);
        return getSalesReport(sevenDaysAgo, today);
    }

    @Transactional(readOnly = true)
    public List<RevenueDTO> getRevenueLast7Days() {
        LocalDate today = LocalDate.now();
        LocalDate startDate = today.minusDays(6);
        LocalDateTime windowStart = startDate.atStartOfDay();
        LocalDateTime windowEnd = today.atTime(LocalTime.MAX);

        List<DailyRevenueSummary> summaries = orderRepository.findRevenueByDateRange(
                REVENUE_STATUSES, windowStart, windowEnd);

        Map<LocalDate, RevenueDTO> revenueMap = summaries.stream()
                .collect(Collectors.toMap(
                        DailyRevenueSummary::getDate,
                        summary -> RevenueDTO.builder()
                                .date(summary.getDate())
                                .revenue(summary.getRevenue())
                                .orderCount(summary.getOrderCount())
                                .build()));

        List<RevenueDTO> weeklyRevenue = new ArrayList<>();
        LocalDate cursor = startDate;
        while (!cursor.isAfter(today)) {
            weeklyRevenue.add(revenueMap.getOrDefault(
                    cursor,
                    RevenueDTO.builder()
                            .date(cursor)
                            .revenue(BigDecimal.ZERO)
                            .orderCount(0L)
                            .build()));
            cursor = cursor.plusDays(1);
        }
        return weeklyRevenue;
    }

    @Transactional(readOnly = true)
    public List<TopProductDTO> getTopSellingProducts() {
        List<ProductSalesSummary> summary = orderRepository.findTopSellingProducts(
                SALES_STATUSES, PageRequest.of(0, 5));

        return summary.stream()
                .map(item -> TopProductDTO.builder()
                        .productName(item.getProductName())
                        .quantity(item.getQuantity())
                        .revenue(item.getRevenue())
                        .build())
                .collect(Collectors.toList());
    }

    public List<SalesReportResponse.TopProductDTO> getDashboardTopProducts() {
        List<ProductSalesSummary> summary = orderRepository.findTopSellingProducts(SALES_STATUSES, PageRequest.of(0, 5));
        return summary.stream()
                .map(item -> SalesReportResponse.TopProductDTO.builder()
                        .productId(item.getProductId())
                        .productName(item.getProductName())
                        .quantity(item.getQuantity())
                        .revenue(item.getRevenue())
                        .build())
                .collect(Collectors.toList());
    }
    
    /**
     * Get last 30 days sales summary
     */
    public SalesReportResponse getMonthlyReport() {
        LocalDate today = LocalDate.now();
        LocalDate thirtyDaysAgo = today.minusDays(30);
        return getSalesReport(thirtyDaysAgo, today);
    }
    
    /**
     * Get last 90 days sales summary
     */
    public SalesReportResponse getQuarterlyReport() {
        LocalDate today = LocalDate.now();
        LocalDate ninetyDaysAgo = today.minusDays(90);
        return getSalesReport(ninetyDaysAgo, today);
    }
    
    /**
     * Get dashboard quick stats
     * Returns summary for today, last 7 days, and overall
     */
    public Map<String, SalesReportResponse> getDashboardStats() {
        return Map.of(
                "today", getTodayReport(),
                "weekly", getWeeklyReport(),
                "monthly", getMonthlyReport(),
                "quarterly", getQuarterlyReport()
        );
    }

    public SalesReportResponse getSevenDayReportWithGlobalCounts() {
        LocalDate today = LocalDate.now();
        LocalDate sevenDaysAgo = today.minusDays(6);
        SalesReportResponse sevenDayReport = getSalesReport(sevenDaysAgo, today);

        return SalesReportResponse.builder()
                .totalRevenue(sevenDayReport.getTotalRevenue())
                .totalOrders(sevenDayReport.getTotalOrders())
                .totalCustomers(fetchLifetimeCustomerCount())
                .totalProducts(fetchLifetimeProductCount())
                .dailySales(sevenDayReport.getDailySales())
                .topProducts(sevenDayReport.getTopProducts())
                .build();
    }
    
    // Private helper methods

    private List<SalesReportResponse.DailySalesDTO> groupSalesByDate(List<Order> orders, LocalDate startDate, LocalDate endDate) {
        Map<LocalDate, SalesReportResponse.DailySalesDTO> dailyMap = new TreeMap<>();

        // Initialize all dates in range with zero values
        LocalDate current = startDate;
        while (!current.isAfter(endDate)) {
            dailyMap.put(current, SalesReportResponse.DailySalesDTO.builder()
                    .date(current)
                    .amount(BigDecimal.ZERO)
                    .orderCount(0L)
                    .build());
            current = current.plusDays(1);
        }

        // Aggregate actual order data
        orders.forEach(order -> {
            if (order.getCreatedAt() == null) {
                return;
            }
            LocalDate orderDate = order.getCreatedAt().toLocalDate();
            SalesReportResponse.DailySalesDTO daily = dailyMap.getOrDefault(orderDate,
                    SalesReportResponse.DailySalesDTO.builder()
                            .date(orderDate)
                            .amount(BigDecimal.ZERO)
                            .orderCount(0L)
                            .build());

            BigDecimal dailyTotal = order.getTotalPrice() != null ? order.getTotalPrice() : BigDecimal.ZERO;
            daily.setAmount(daily.getAmount().add(dailyTotal));
            daily.setOrderCount(daily.getOrderCount() + 1);
            dailyMap.put(orderDate, daily);
        });

        return new ArrayList<>(dailyMap.values());
    }

    private List<Long> getFeaturedProductCandidateIds() {
        return featuredProductService.getActiveFeaturedProducts().stream()
                .map(fp -> fp.getProduct())
                .filter(Objects::nonNull)
                .map(Product::getId)
                .filter(Objects::nonNull)
                .limit(9)
                .collect(Collectors.toList());
    }

    private List<Order> fetchHistoricSalesOrders() {
        LocalDateTime historicStart = HISTORIC_TOP_PRODUCT_START.atStartOfDay();
        LocalDateTime historicEnd = LocalDateTime.now();
        return orderRepository.findSalesOrders(SALES_STATUSES, historicStart, historicEnd);
    }

    private List<SalesReportResponse.TopProductDTO> getTopProducts(List<Order> orders, int limit,
                                                                  List<Long> candidateProductIds,
                                                                  boolean enforceFeaturedFilter,
                                                                  boolean allowHistoricFallback) {
        if (enforceFeaturedFilter && (candidateProductIds == null || candidateProductIds.isEmpty())) {
            return Collections.emptyList();
        }

        Set<Long> candidateSet = candidateProductIds != null ? new HashSet<>(candidateProductIds) : new HashSet<>();
        Map<Long, ProductAccumulator> accumulator = new HashMap<>();

        if (orders != null) {
            for (Order order : orders) {
                if (order == null || order.getOrderItems() == null) {
                    continue;
                }
                LocalDateTime orderTime = order.getCreatedAt();
                for (var item : order.getOrderItems()) {
                    if (item == null || item.getVariant() == null || item.getVariant().getProduct() == null) {
                        continue;
                    }
                    Product product = item.getVariant().getProduct();
                    Long productId = product.getId();
                    if (productId == null || (enforceFeaturedFilter && !candidateSet.contains(productId))) {
                        continue;
                    }

                    long quantity = item.getQuantity() == null ? 0L : item.getQuantity();
                    BigDecimal unitPrice = item.getUnitPrice() == null ? BigDecimal.ZERO : item.getUnitPrice();
                    BigDecimal itemRevenue = unitPrice.multiply(BigDecimal.valueOf(quantity));

                    ProductAccumulator data = accumulator.computeIfAbsent(productId,
                            id -> new ProductAccumulator(id, product.getProductName()));

                    data.accumulate(quantity, itemRevenue, orderTime);
                }
            }
        }

        Comparator<ProductAccumulator> topProductComparator = Comparator
                .comparingLong(ProductAccumulator::getQuantity).reversed()
                .thenComparing(ProductAccumulator::getRevenue, Comparator.nullsLast(Comparator.reverseOrder()))
                .thenComparing(acc -> acc.getFirstSoldAt() == null ? LocalDateTime.MAX : acc.getFirstSoldAt());

        List<SalesReportResponse.TopProductDTO> ranked = accumulator.values().stream()
                .sorted(topProductComparator)
                .limit(limit)
                .map(acc -> SalesReportResponse.TopProductDTO.builder()
                        .productId(acc.getProductId())
                        .productName(acc.getProductName())
                        .quantity(acc.getQuantity())
                        .revenue(acc.getRevenue())
                        .build())
                .collect(Collectors.toList());

        if (ranked.isEmpty() && allowHistoricFallback) {
            return getTopProducts(fetchHistoricSalesOrders(), limit, candidateProductIds, enforceFeaturedFilter, false);
        }

        return ranked;
    }

    private long fetchLifetimeCustomerCount() {
        Long count = orderRepository.countDistinctCustomers();
        return count != null ? count : 0L;
    }

    private long fetchLifetimeProductCount() {
        Long sum = orderRepository.sumOrderItemQuantities(SALES_STATUSES);
        return sum != null ? sum : 0L;
    }

    @Transactional(readOnly = true)
    public List<RecentOrderDTO> getRecentOrders(int limit) {
        List<Order> recent = orderRepository.findTopNByStatusIn(
                SALES_STATUSES,
                org.springframework.data.domain.PageRequest.of(0, limit, Sort.by(Sort.Direction.DESC, "createdAt")) );

        return recent.stream()
                .map(order -> RecentOrderDTO.builder()
                        .orderInvoice(order.getOrderInvoice())
                        .customerName(order.getCustomer() != null ? order.getCustomer().getFullName() : "Khách ẩn danh")
                        .totalPrice(order.getTotalPrice())
                        .status(order.getStatus())
                        .build())
                .collect(Collectors.toList());
    }

    private static final class ProductAccumulator {
        private final Long productId;
        private final String productName;
        private long quantity;
        private BigDecimal revenue = BigDecimal.ZERO;
        private LocalDateTime firstSoldAt;

        private ProductAccumulator(Long productId, String productName) {
            this.productId = productId;
            this.productName = productName != null ? productName : "";
        }

        private void accumulate(long qty, BigDecimal value, LocalDateTime dateTime) {
            this.quantity += qty;
            this.revenue = this.revenue.add(value != null ? value : BigDecimal.ZERO);
            if (dateTime == null) {
                return;
            }
            if (this.firstSoldAt == null || dateTime.isBefore(this.firstSoldAt)) {
                this.firstSoldAt = dateTime;
            }
        }

        private Long getProductId() {
            return productId;
        }

        private String getProductName() {
            return productName;
        }

        private long getQuantity() {
            return quantity;
        }

        private BigDecimal getRevenue() {
            return revenue;
        }

        private LocalDateTime getFirstSoldAt() {
            return firstSoldAt;
        }
    }
}



