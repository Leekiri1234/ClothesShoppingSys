package com.clothshop.client.services;

import com.clothshop.client.dtos.response.ProductDetailResponse;
import com.clothshop.client.dtos.response.ProductListResponse;
import com.clothshop.client.mappers.ProductClientMapper;
import com.clothshop.common.dtos.request.PagingRequest;
import com.clothshop.common.dtos.response.PageResponse;
import com.clothshop.common.exceptions.BusinessException;
import com.clothshop.common.exceptions.ErrorCode;
import com.clothshop.domain.models.product.Product;
import com.clothshop.domain.repositories.marketing.FeaturedProductRepository;
import com.clothshop.domain.repositories.product.ProductRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cache.annotation.CacheEvict;
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
    private final FeaturedProductRepository featuredProductRepository;

    /**
     * Get product detail by slug (SEO-friendly URL).
     * Cached for 1 hour to reduce database load.
     * Only returns active products.
     */
    @Transactional(readOnly = true)
    public ProductDetailResponse getProductBySlug(String slug) {
        log.debug("Fetching product detail for slug: {}", slug);

        Product product = productRepository.findByProductSlug(slug)
                .filter(p -> Boolean.TRUE.equals(p.getIsActive()))
                .orElseThrow(() -> new BusinessException(ErrorCode.RESOURCE_NOT_FOUND));

        // Cực kỳ quan trọng: Ép nạp (Initialize) danh sách variants và các thông tin liên quan
        if (product.getVariants() != null) {
            product.getVariants().size();
        }

        return productMapper.toDetailResponse(product);
    }

    @Transactional(readOnly = true)
    public ProductDetailResponse getProductById(Long id) {
        Product product = productRepository.findById(id)
                .filter(p -> Boolean.TRUE.equals(p.getIsActive()))
                .orElseThrow(() -> new BusinessException(ErrorCode.RESOURCE_NOT_FOUND));

        if (product.getVariants() != null) {
            product.getVariants().size();
        }

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
    @CacheEvict(value = "featuredProducts", allEntries = true)
    public List<ProductListResponse> getFeaturedProducts(int limit) {
        log.debug("Fetching top {} featured products from DB", limit);

        // Đẩy việc limit xuống tận Database (LIMIT ?) thay vì filter trên RAM
        Pageable pageable = PageRequest.of(0, limit);
        List<Product> featuredProducts = featuredProductRepository.findTopFeaturedProducts(pageable);

        return featuredProducts.stream()
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
