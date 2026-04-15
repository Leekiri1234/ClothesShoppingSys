package com.clothshop.admin.services;

import com.clothshop.admin.dtos.response.marketing.WishlistInsightsResponse;
import com.clothshop.admin.dtos.response.marketing.WishlistProductDetailResponse;
import com.clothshop.common.exceptions.BusinessException;
import com.clothshop.common.exceptions.ErrorCode;
import com.clothshop.domain.models.product.Product;
import com.clothshop.domain.projections.WishlistCustomerSummary;
import com.clothshop.domain.projections.WishlistProductSummary;
import com.clothshop.domain.projections.WishlistTrendSummary;
import com.clothshop.domain.repositories.customer.WishlistItemRepository;
import com.clothshop.domain.repositories.product.CategoryRepository;
import com.clothshop.domain.repositories.product.ProductRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.*;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class WishlistInsightsService {

    private static final int TOP_LIMIT = 20;

    private final WishlistItemRepository wishlistItemRepository;
    private final CategoryRepository categoryRepository;
    private final ProductRepository productRepository;

    @Transactional(readOnly = true)
    public WishlistInsightsResponse getInsights(LocalDate startDate, LocalDate endDate, Long categoryId, Long productId) {
        DateRange dateRange = normalizeDateRange(startDate, endDate);
        validateFilterIds(categoryId, productId);

        List<WishlistProductSummary> topProducts = wishlistItemRepository.findTopWishlistedProducts(
                dateRange.startDateTime, dateRange.endDateTime, categoryId, productId, PageRequest.of(0, TOP_LIMIT));

        Long totalWishlistItems = safeLong(wishlistItemRepository.countWishlists(
                dateRange.startDateTime, dateRange.endDateTime, categoryId, productId));
        Long totalCustomers = safeLong(wishlistItemRepository.countDistinctCustomers(
                dateRange.startDateTime, dateRange.endDateTime, categoryId, productId));
        Long totalProducts = safeLong(wishlistItemRepository.countDistinctProducts(
                dateRange.startDateTime, dateRange.endDateTime, categoryId, productId));

        List<WishlistTrendSummary> trendSummary = wishlistItemRepository.getWishlistTrend(
                dateRange.startDateTime, dateRange.endDateTime, categoryId, productId);

        return WishlistInsightsResponse.builder()
                .startDate(dateRange.startDate)
                .endDate(dateRange.endDate)
                .selectedCategoryId(categoryId)
                .selectedProductId(productId)
                .totalWishlistItems(totalWishlistItems)
                .totalWishlistedProducts(totalProducts)
                .totalCustomers(totalCustomers)
                .categoryOptions(buildCategoryOptions())
                .productOptions(buildProductOptions(categoryId))
                .topProducts(topProducts.stream().map(this::toTopProductDto).collect(Collectors.toList()))
                .trend(fillTrendRange(dateRange.startDate, dateRange.endDate, trendSummary))
                .build();
    }

    @Transactional(readOnly = true)
    public WishlistProductDetailResponse getProductDetail(Long productId, LocalDate startDate, LocalDate endDate) {
        DateRange dateRange = normalizeDateRange(startDate, endDate);
        Product product = productRepository.findById(productId)
                .orElseThrow(() -> new BusinessException(ErrorCode.RESOURCE_NOT_FOUND, "Sản phẩm không tồn tại"));

        Long totalWishlistItems = safeLong(wishlistItemRepository.countWishlists(
                dateRange.startDateTime, dateRange.endDateTime, null, productId));
        Long totalCustomers = safeLong(wishlistItemRepository.countDistinctCustomers(
                dateRange.startDateTime, dateRange.endDateTime, null, productId));

        List<WishlistTrendSummary> trendSummary = wishlistItemRepository.getWishlistTrend(
                dateRange.startDateTime, dateRange.endDateTime, null, productId);
        List<WishlistCustomerSummary> customers = wishlistItemRepository.findCustomersByProduct(
                productId, dateRange.startDateTime, dateRange.endDateTime);

        return WishlistProductDetailResponse.builder()
                .productId(productId)
                .productName(product.getProductName())
                .startDate(dateRange.startDate)
                .endDate(dateRange.endDate)
                .totalWishlists(totalWishlistItems)
                .totalCustomers(totalCustomers)
                .trend(fillTrendRange(dateRange.startDate, dateRange.endDate, trendSummary))
                .customers(customers.stream().map(this::toCustomerDto).collect(Collectors.toList()))
                .build();
    }

    @Transactional(readOnly = true)
    public String exportTopProductsCsv(LocalDate startDate, LocalDate endDate, Long categoryId, Long productId) {
        WishlistInsightsResponse insights = getInsights(startDate, endDate, categoryId, productId);

        StringBuilder csv = new StringBuilder();
        csv.append("Product ID,Product Name,Category,Wishlist Count\n");
        for (WishlistInsightsResponse.TopProductDTO row : insights.getTopProducts()) {
            csv.append(row.getProductId()).append(",")
                    .append(escapeCsv(row.getProductName())).append(",")
                    .append(escapeCsv(row.getCategoryName())).append(",")
                    .append(row.getWishlistCount()).append("\n");
        }
        return csv.toString();
    }

    private void validateFilterIds(Long categoryId, Long productId) {
        if (categoryId != null && !categoryRepository.existsById(categoryId)) {
            throw new BusinessException(ErrorCode.RESOURCE_NOT_FOUND, "Danh mục không tồn tại");
        }
        if (productId != null && !productRepository.existsById(productId)) {
            throw new BusinessException(ErrorCode.RESOURCE_NOT_FOUND, "Sản phẩm không tồn tại");
        }
    }

    private List<WishlistInsightsResponse.OptionDTO> buildCategoryOptions() {
        return categoryRepository.findAllByIsActiveTrue().stream()
                .sorted(Comparator.comparing(
                        c -> c.getCategoryName() == null ? "" : c.getCategoryName().toLowerCase()))
                .map(c -> WishlistInsightsResponse.OptionDTO.builder()
                        .id(c.getId())
                        .name(c.getCategoryName())
                        .build())
                .collect(Collectors.toList());
    }

    private List<WishlistInsightsResponse.OptionDTO> buildProductOptions(Long categoryId) {
        List<Product> products;
        Sort sort = Sort.by(Sort.Direction.ASC, "productName");
        if (categoryId == null) {
            products = productRepository.findAllByIsActiveTrue(sort);
        } else {
            products = productRepository.findByCategory_IdAndIsActiveTrue(categoryId, sort);
        }

        return products.stream()
                .map(p -> WishlistInsightsResponse.OptionDTO.builder()
                        .id(p.getId())
                        .name(p.getProductName())
                        .build())
                .collect(Collectors.toList());
    }

    private WishlistInsightsResponse.TopProductDTO toTopProductDto(WishlistProductSummary row) {
        return WishlistInsightsResponse.TopProductDTO.builder()
                .productId(row.getProductId())
                .productName(row.getProductName())
                .categoryName(row.getCategoryName())
                .wishlistCount(safeLong(row.getWishlistCount()))
                .build();
    }

    private WishlistProductDetailResponse.CustomerWishlistDTO toCustomerDto(WishlistCustomerSummary row) {
        return WishlistProductDetailResponse.CustomerWishlistDTO.builder()
                .customerId(row.getCustomerId())
                .customerName(row.getCustomerName())
                .customerEmail(row.getCustomerEmail())
                .wishlistedAt(row.getWishlistedAt())
                .build();
    }

    private List<WishlistInsightsResponse.TrendPointDTO> fillTrendRange(
            LocalDate startDate,
            LocalDate endDate,
            List<WishlistTrendSummary> trendSummary
    ) {
        Map<LocalDate, Long> countByDate = new HashMap<>();
        for (WishlistTrendSummary row : trendSummary) {
            countByDate.put(row.getDate(), safeLong(row.getWishlistCount()));
        }

        List<WishlistInsightsResponse.TrendPointDTO> points = new ArrayList<>();
        LocalDate cursor = startDate;
        while (!cursor.isAfter(endDate)) {
            points.add(WishlistInsightsResponse.TrendPointDTO.builder()
                    .date(cursor)
                    .wishlistCount(countByDate.getOrDefault(cursor, 0L))
                    .build());
            cursor = cursor.plusDays(1);
        }
        return points;
    }

    private DateRange normalizeDateRange(LocalDate startDate, LocalDate endDate) {
        LocalDate normalizedEnd = endDate != null ? endDate : LocalDate.now();
        LocalDate normalizedStart = startDate != null ? startDate : normalizedEnd.minusDays(30);
        if (normalizedStart.isAfter(normalizedEnd)) {
            LocalDate swap = normalizedStart;
            normalizedStart = normalizedEnd;
            normalizedEnd = swap;
        }
        return new DateRange(
                normalizedStart,
                normalizedEnd,
                normalizedStart.atStartOfDay(),
                normalizedEnd.atTime(LocalTime.MAX)
        );
    }

    private String escapeCsv(String input) {
        if (input == null) {
            return "";
        }
        String escaped = input.replace("\"", "\"\"");
        return "\"" + escaped + "\"";
    }

    private Long safeLong(Long value) {
        return value == null ? 0L : value;
    }

    private static final class DateRange {
        private final LocalDate startDate;
        private final LocalDate endDate;
        private final LocalDateTime startDateTime;
        private final LocalDateTime endDateTime;

        private DateRange(LocalDate startDate, LocalDate endDate, LocalDateTime startDateTime, LocalDateTime endDateTime) {
            this.startDate = startDate;
            this.endDate = endDate;
            this.startDateTime = startDateTime;
            this.endDateTime = endDateTime;
        }
    }
}
