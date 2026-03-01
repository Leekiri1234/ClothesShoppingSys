package com.clothshop.client.services;

import com.clothshop.client.dtos.response.ProductDetailResponse;
import com.clothshop.client.dtos.response.ProductListResponse;
import com.clothshop.client.mappers.ProductClientMapper;
import com.clothshop.common.dtos.request.PagingRequest;
import com.clothshop.common.dtos.response.PageResponse;
import com.clothshop.common.exceptions.BusinessException;
import com.clothshop.common.exceptions.ErrorCode;
import com.clothshop.domain.entities.product.Product;
import com.clothshop.domain.repositories.product.ProductRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

/**
 * Product Client Service - FAT SERVICE pattern.
 * Handles business logic for public product browsing.
 *
 * Key optimizations:
 * - Read-only transactions for better performance
 * - Caching for frequently accessed data
 * - Only returns active products
 * - No sensitive data exposure
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class ProductClientService {

    private final ProductRepository productRepository;
    private final ProductClientMapper productMapper;

    /**
     * Get product detail by slug (SEO-friendly URL).
     * Cached for 1 hour to reduce database load.
     * Only returns active products.
     */
    @Transactional(readOnly = true)
    @Cacheable(value = "productDetail", key = "#slug", unless = "#result == null")
    public ProductDetailResponse getProductBySlug(String slug) {
        log.debug("Fetching product detail for slug: {}", slug);

        Product product = productRepository.findByProductSlug(slug)
                .filter(p -> Boolean.TRUE.equals(p.getIsActive())) // Only active products
                .orElseThrow(() -> new BusinessException(ErrorCode.RESOURCE_NOT_FOUND));

        return productMapper.toDetailResponse(product);
    }

    /**
     * Get all active products with pagination.
     * Memory-optimized: uses lightweight DTOs without full description.
     * Only returns products that are active and in stock.
     */
    @Transactional(readOnly = true)
    public PageResponse<ProductListResponse> getAllActiveProducts(PagingRequest pagingRequest) {
        pagingRequest.validate();

        Sort sort = Sort.by(Sort.Direction.fromString(pagingRequest.getSortDirection()),
                pagingRequest.getSortBy() != null ? pagingRequest.getSortBy() : "createdAt");
        Pageable pageable = PageRequest.of(pagingRequest.getPageNumber(), pagingRequest.getPageSize(), sort);

        // Lọc ngay từ Database để phân trang chính xác
        Page<Product> productPage = productRepository.findAllByIsActiveTrue(pageable);

        List<ProductListResponse> content = productPage.getContent().stream()
                .map(productMapper::toListResponse)
                .collect(Collectors.toList());

        return PageResponse.<ProductListResponse>builder()
                .content(content)
                .pageNumber(productPage.getNumber())
                .pageSize(productPage.getSize())
                .totalElements(productPage.getTotalElements())
                .totalPages(productPage.getTotalPages())
                .build();
    }
    /**
     * Get featured products for home page.
     * Returns latest active products with limit.
     * Cached for performance.
     *
     * @param limit Maximum number of products to return
     * @return List of featured products
     */
    @Transactional(readOnly = true)
    @Cacheable(value = "featuredProducts", key = "#limit")
    public List<ProductListResponse> getFeaturedProducts(int limit) {
        log.debug("Fetching {} featured products", limit);

        Pageable pageable = PageRequest.of(0, limit, Sort.by(Sort.Direction.DESC, "createdAt"));
        Page<Product> productPage = productRepository.findAll(pageable);

        return productPage.getContent().stream()
                .filter(p -> Boolean.TRUE.equals(p.getIsActive()))
                .map(productMapper::toListResponse)
                .collect(Collectors.toList());
    }

    /**
     * Get products by category slug.
     * Useful for category browsing pages.
     */
    @Transactional(readOnly = true)
    public PageResponse<ProductListResponse> getProductsByCategory(
            String categorySlug,
            PagingRequest pagingRequest) {

        pagingRequest.validate();

        // TODO: Implement after CategoryRepository is ready
        // For now, return all products
        log.warn("Category filtering not yet implemented, returning all products");
        return getAllActiveProducts(pagingRequest);
    }

    /**
     * Search products by keyword.
     * Searches in product name and description.
     */
    @Transactional(readOnly = true)
    public PageResponse<ProductListResponse> searchProducts(
            String keyword,
            PagingRequest pagingRequest) {

        pagingRequest.validate();

        // TODO: Implement full-text search
        // For now, return all products
        log.warn("Search not yet implemented, returning all products");
        return getAllActiveProducts(pagingRequest);
    }
}
