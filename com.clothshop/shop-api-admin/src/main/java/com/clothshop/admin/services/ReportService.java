package com.clothshop.admin.services;

import com.clothshop.domain.entities.order.Order;
import com.clothshop.domain.enums.OrderStatus;
import com.clothshop.domain.repositories.order.OrderRepository;
import com.clothshop.admin.dtos.response.SalesReportResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

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
    
    /**
     * Get sales report for the specified date range
     * Only counts COMPLETED orders for revenue calculation
     */
    public SalesReportResponse getSalesReport(LocalDate startDate, LocalDate endDate) {
        log.info("Generating sales report from {} to {}", startDate, endDate);
        
        LocalDateTime startDateTime = startDate.atStartOfDay();
        LocalDateTime endDateTime = endDate.atTime(LocalTime.MAX);
        
        List<Order> completedOrders = orderRepository.findByStatus(OrderStatus.COMPLETED)
                .stream()
                .filter(order -> isWithinDateRange(order.getCreatedAt(), startDateTime, endDateTime))
                .collect(Collectors.toList());
        
        if (completedOrders.isEmpty()) {
            return SalesReportResponse.builder()
                    .totalRevenue(BigDecimal.ZERO)
                    .totalOrders(0L)
                    .totalCustomers(0L)
                    .totalProducts(0L)
                    .dailySales(Collections.emptyList())
                    .topProducts(Collections.emptyList())
                    .build();
        }
        
        // Calculate total revenue
        BigDecimal totalRevenue = completedOrders.stream()
                .map(Order::getTotalPrice)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
        
        // Count unique customers
        Long totalCustomers = completedOrders.stream()
                .map(order -> order.getCustomer().getId())
                .distinct()
                .count();
        
        // Count total unique products sold
        Long totalProducts = completedOrders.stream()
                .flatMap(order -> order.getOrderItems().stream())
                .map(item -> item.getVariant().getProduct().getId())
                .distinct()
                .count();
        
        // Group sales by date
        List<SalesReportResponse.DailySalesDTO> dailySales = groupSalesByDate(completedOrders, startDate, endDate);
        
        // Get top 5 products
        List<SalesReportResponse.TopProductDTO> topProducts = getTopProducts(completedOrders, 5);
        
        return SalesReportResponse.builder()
                .totalRevenue(totalRevenue)
                .totalOrders((long) completedOrders.size())
                .totalCustomers(totalCustomers)
                .totalProducts(totalProducts)
                .dailySales(dailySales)
                .topProducts(topProducts)
                .build();
    }
    
    /**
     * Get today's sales summary
     */
    public SalesReportResponse getTodayReport() {
        LocalDate today = LocalDate.now();
        return getSalesReport(today, today);
    }
    
    /**
     * Get last 7 days sales summary
     */
    public SalesReportResponse getWeeklyReport() {
        LocalDate today = LocalDate.now();
        LocalDate sevenDaysAgo = today.minusDays(7);
        return getSalesReport(sevenDaysAgo, today);
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
    
    // Private helper methods
    
    private boolean isWithinDateRange(LocalDateTime dateTime, LocalDateTime startDateTime, LocalDateTime endDateTime) {
        return !dateTime.isBefore(startDateTime) && !dateTime.isAfter(endDateTime);
    }
    
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
            LocalDate orderDate = order.getCreatedAt().toLocalDate();
            SalesReportResponse.DailySalesDTO daily = dailyMap.getOrDefault(orderDate,
                    SalesReportResponse.DailySalesDTO.builder()
                            .date(orderDate)
                            .amount(BigDecimal.ZERO)
                            .orderCount(0L)
                            .build());
            
            daily.setAmount(daily.getAmount().add(order.getTotalPrice()));
            daily.setOrderCount(daily.getOrderCount() + 1);
            dailyMap.put(orderDate, daily);
        });
        
        return new ArrayList<>(dailyMap.values());
    }
    
    private List<SalesReportResponse.TopProductDTO> getTopProducts(List<Order> orders, int limit) {
        return orders.stream()
                .flatMap(order -> order.getOrderItems().stream())
                .collect(Collectors.groupingBy(
                        item -> item.getVariant().getProduct(),
                        Collectors.reducing(
                                new Object[]{0L, BigDecimal.ZERO},
                                item -> new Object[]{item.getQuantity().longValue(), item.getUnitPrice().multiply(new BigDecimal(item.getQuantity()))},
                                (acc, item) -> {
                                    Long qty = (Long) acc[0] + (Long) item[0];
                                    BigDecimal revenue = ((BigDecimal) acc[1]).add((BigDecimal) item[1]);
                                    return new Object[]{qty, revenue};
                                }
                        )
                ))
                .entrySet().stream()
                .map(entry -> SalesReportResponse.TopProductDTO.builder()
                        .productId(entry.getKey().getId())
                        .productName(entry.getKey().getProductName())
                        .quantity((Long) entry.getValue()[0])
                        .revenue((BigDecimal) entry.getValue()[1])
                        .build())
                .sorted(Comparator.comparing(SalesReportResponse.TopProductDTO::getRevenue).reversed())
                .limit(limit)
                .collect(Collectors.toList());
    }
}
