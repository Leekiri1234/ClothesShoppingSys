package com.clothshop.admin.services;

import com.clothshop.admin.dtos.request.products.ProductCreateRequest;
import com.clothshop.admin.dtos.request.products.ProductUpdateRequest;
import com.clothshop.admin.dtos.response.products.ProductAdminResponse;
import com.clothshop.admin.mappers.ProductAdminMapper;
import com.clothshop.common.dtos.request.PagingRequest;
import com.clothshop.common.dtos.response.PageResponse;
import com.clothshop.common.exceptions.BusinessException;
import com.clothshop.common.exceptions.ErrorCode;
import com.clothshop.common.utils.SlugUtils;
import com.clothshop.domain.entities.product.Category;
import com.clothshop.domain.entities.product.Product;
import com.clothshop.domain.enums.ProductStatus;
import com.clothshop.domain.repositories.product.CategoryRepository;
import com.clothshop.domain.repositories.product.ProductRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

/**
 * Product Admin Service - FAT SERVICE pattern.
 * Handles all business logic for product management.
 * Responsibilities:
 * - Generate slug from product name
 * - Validate business rules
 * - Manage transactions
 * - Coordinate with repositories
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class ProductAdminService {

    private final ProductRepository productRepository;
    private final CategoryRepository categoryRepository;
    private final ProductAdminMapper productMapper;

    /**
     * Create new product with automatic slug generation.
     * Business rules:
     * - Product name must be unique
     * - Category must exist
     * - Slug is auto-generated from name
     */
    @Transactional
    public ProductAdminResponse createProduct(ProductCreateRequest request) {
        log.info("Creating new product: {}", request.getProductName());

        // Validate category exists
        Category category = categoryRepository.findById(request.getCategoryId())
                .orElseThrow(() -> new BusinessException(ErrorCode.RESOURCE_NOT_FOUND));

        // Generate unique slug from product name
        String slug = generateUniqueSlug(request.getProductName());

        // Map DTO to Entity
        Product product = productMapper.toEntity(request);
        product.setProductSlug(slug);
        product.setCategory(category);
        product.setProdStatus(ProductStatus.ACTIVE);

        // Save to database
        Product savedProduct = productRepository.save(product);
        log.info("Product created successfully with ID: {}", savedProduct.getId());

        return productMapper.toResponse(savedProduct);
    }

    /**
     * Update existing product.
     * Supports partial updates (only non-null fields are updated).
     */
    @Transactional
    public ProductAdminResponse updateProduct(Long productId, ProductUpdateRequest request) {
        log.info("Updating product ID: {}", productId);

        // Find existing product
        Product product = productRepository.findById(productId)
                .orElseThrow(() -> new BusinessException(ErrorCode.RESOURCE_NOT_FOUND));

        // If category is being updated, validate it exists
        if (request.getCategoryId() != null) {
            Category category = categoryRepository.findById(request.getCategoryId())
                    .orElseThrow(() -> new BusinessException(ErrorCode.RESOURCE_NOT_FOUND));
            product.setCategory(category);
        }

        // If product name is updated, regenerate slug
        if (request.getProductName() != null && !request.getProductName().equals(product.getProductName())) {
            String newSlug = generateUniqueSlug(request.getProductName());
            product.setProductSlug(newSlug);
        }

        // Update entity with request data (partial update)
        productMapper.updateEntityFromRequest(request, product);

        // Save changes
        Product updatedProduct = productRepository.save(product);
        log.info("Product updated successfully: {}", productId);

        return productMapper.toResponse(updatedProduct);
    }

    /**
     * Get product by ID (for editing).
     */
    @Transactional(readOnly = true)
    public ProductAdminResponse getProductById(Long productId) {
        Product product = productRepository.findById(productId)
                .orElseThrow(() -> new BusinessException(ErrorCode.RESOURCE_NOT_FOUND, "Không tìm thấy sản phẩm"));

        ProductAdminResponse response = productMapper.toResponse(product);

        // Logic tự động tính tổng stock dựa vào các variant đang hoạt động
        if (product.getVariants() != null) {
            int totalStock = product.getVariants().stream()
                    .filter(v -> v.getIsActive() != null && v.getIsActive())
                    .mapToInt(v -> v.getStockQuantity() != null ? v.getStockQuantity() : 0)
                    .sum();
            response.setStock(totalStock);
        } else {
            response.setStock(0);
        }

        return response;
    }

    /**
     * Get all products with pagination.
     * Memory-optimized with paging to avoid loading all records.
     */
    @Transactional(readOnly = true)
    public PageResponse<ProductAdminResponse> getAllProducts(PagingRequest pagingRequest) {
        pagingRequest.validate();

        // Create pageable with sorting
        Sort sort = Sort.by(Sort.Direction.fromString(pagingRequest.getSortDirection()),
                pagingRequest.getSortBy() != null ? pagingRequest.getSortBy() : "createdAt");
        Pageable pageable = PageRequest.of(pagingRequest.getPageNumber(), pagingRequest.getPageSize(), sort);

        // Fetch from database with pagination
        Page<Product> productPage = productRepository.findAll(pageable);

        // Convert to DTOs and calculate total stock from variants
        List<ProductAdminResponse> content = productPage.getContent().stream()
                .map(product -> {
                    ProductAdminResponse response = productMapper.toResponse(product);

                    // Calculate total stock from all active variants
                    if (product.getVariants() != null) {
                        int totalStock = product.getVariants().stream()
                                .filter(v -> v.getIsActive() != null && v.getIsActive())
                                .mapToInt(v -> v.getStockQuantity() != null ? v.getStockQuantity() : 0)
                                .sum();
                        response.setStock(totalStock);
                    } else {
                        response.setStock(0);
                    }

                    return response;
                })
                .collect(Collectors.toList());

        return PageResponse.<ProductAdminResponse>builder()
                .content(content)
                .pageNumber(productPage.getNumber())
                .pageSize(productPage.getSize())
                .totalElements(productPage.getTotalElements())
                .totalPages(productPage.getTotalPages())
                .first(productPage.isFirst())
                .last(productPage.isLast())
                .build();
    }

    /**
     * Soft delete product (set isActive = false).
     * Follows soft delete pattern for data retention.
     */
    @Transactional
    public void deleteProduct(Long productId) {
        Product product = productRepository.findById(productId)
                .orElseThrow(() -> new BusinessException(ErrorCode.RESOURCE_NOT_FOUND));

        product.setIsActive(false);
        product.setProdStatus(ProductStatus.INACTIVE);
        productRepository.save(product);

        log.info("Product soft deleted: {}", productId);
    }

    /**
     * Generate unique slug from product name.
     * If slug exists, append counter suffix.
     */
    private String generateUniqueSlug(String productName) {
        String baseSlug = SlugUtils.makeSlug(productName);
        String slug = baseSlug;
        int counter = 1;

        while (productRepository.existsByProductSlug(slug)) {
            slug = baseSlug + "-" + counter;
            counter++;
        }
        return slug;
    }
}
