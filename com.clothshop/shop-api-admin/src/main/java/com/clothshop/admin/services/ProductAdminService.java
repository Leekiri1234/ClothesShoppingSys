package com.clothshop.admin.services;

import com.clothshop.admin.dtos.request.products.ProductCreateRequest;
import com.clothshop.admin.dtos.request.products.ProductUpdateRequest;
import com.clothshop.admin.dtos.response.products.ProductAdminResponse;
import com.clothshop.admin.mappers.ProductAdminMapper;
import com.clothshop.common.dtos.request.PagingRequest;
import com.clothshop.common.dtos.response.PageResponse;
import com.clothshop.common.exceptions.BusinessException;
import com.clothshop.common.exceptions.ErrorCode;
import com.clothshop.common.utils.FileUploadUtil;
import com.clothshop.common.utils.SlugUtils;
import com.clothshop.domain.models.product.Category;
import com.clothshop.domain.models.product.Product;
import com.clothshop.domain.models.product.ProductImage;
import com.clothshop.domain.enums.ProductStatus;
import com.clothshop.domain.repositories.product.CategoryRepository;
import com.clothshop.domain.repositories.product.ProductImageRepository;
import com.clothshop.domain.repositories.product.ProductRepository;
import com.clothshop.domain.repositories.product.ProductVariantRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;
import java.util.Map;
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
    private final ProductVariantRepository productVariantRepository;
    private final ProductImageRepository productImageRepository;
    private final FileUploadUtil fileUploadUtil;

    /**
     * Create new product with automatic slug generation.
     * Business rules:
     * - Product name must be unique
     * - Category must exist
     * - Slug is auto-generated from name
     */
    @Transactional
    public ProductAdminResponse createProduct(ProductCreateRequest request, MultipartFile imageFile) {
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

        // Handle image upload overriding string url
        if (imageFile != null && !imageFile.isEmpty()) {
            String fileUrl = fileUploadUtil.upload(imageFile, "products");
            updateMainProductImage(savedProduct, fileUrl);
        } else if (request.getImageUrl() != null && !request.getImageUrl().isEmpty()) {
            updateMainProductImage(savedProduct, request.getImageUrl());
        }

        return productMapper.toResponse(savedProduct);
    }

    /**
     * Update existing product.
     * Supports partial updates (only non-null fields are updated).
     */
    @Transactional
    public ProductAdminResponse updateProduct(Long productId, ProductUpdateRequest request, MultipartFile imageFile) {
        log.info("Updating product ID: {}", productId);

        // Find existing product
        Product product = productRepository.findProductWithDetailsById(productId)
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

        // Handle image upload overriding string url
        if (imageFile != null && !imageFile.isEmpty()) {
            String fileUrl = fileUploadUtil.upload(imageFile, "products");
            updateMainProductImage(updatedProduct, fileUrl);
        } else if (request.getImageUrl() != null && !request.getImageUrl().isEmpty()) {
            updateMainProductImage(updatedProduct, request.getImageUrl());
        }

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
        response.setStock(calculateTotalStock(product));
        
        productImageRepository.findByProductIdAndIsMainTrue(productId)
                .ifPresent(img -> response.setImageUrl(img.getImageUrl()));

        return response;
    }

    /**
     * Get all products with pagination.
     * Memory-optimized with paging to avoid loading all records.
     */
    @Transactional(readOnly = true)
    public PageResponse<ProductAdminResponse> getAllProducts(
            String keyword,
            Long categoryId,
            ProductStatus prodStatus,
            PagingRequest pagingRequest) {

        pagingRequest.validate();

        // Create pageable with sorting
        Sort sort = Sort.by(Sort.Direction.fromString(pagingRequest.getSortDirection()),
                pagingRequest.getSortBy() != null ? pagingRequest.getSortBy() : "createdAt");
        Pageable pageable = PageRequest.of(pagingRequest.getPageNumber(), pagingRequest.getPageSize(), sort);

        // Fetch from database with pagination and filter
        Page<Product> productPage = productRepository.filterProductsForAdmin(keyword, categoryId, prodStatus, pageable);

        // Lấy tổng tồn kho cho tất cả sản phẩm trong trang bằng một truy vấn duy nhất (tránh N+1)
        List<Long> productIds = productPage.getContent().stream()
                .map(Product::getId)
                .collect(Collectors.toList());

        Map<Long, Integer> stockByProductId = productVariantRepository
                .findTotalStockByProductIds(productIds)
                .stream()
                .collect(Collectors.toMap(
                        row -> (Long) row[0],
                        row -> row[1] != null ? ((Number) row[1]).intValue() : 0
                ));
                
        // Lấy main image url
        Map<Long, String> imageUrlByProductId = productImageRepository.findAll()
                .stream()
                .filter(img -> productIds.contains(img.getProduct().getId()) && Boolean.TRUE.equals(img.getIsMain()))
                .collect(Collectors.toMap(
                        img -> img.getProduct().getId(),
                        ProductImage::getImageUrl,
                        (img1, img2) -> img1
                ));

        // Convert to DTOs and set total stock from the aggregate map
        List<ProductAdminResponse> content = productPage.getContent().stream()
                .map(product -> {
                    ProductAdminResponse response = productMapper.toResponse(product);
                    response.setStock(stockByProductId.getOrDefault(product.getId(), 0));
                    response.setImageUrl(imageUrlByProductId.get(product.getId()));
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
     * Toggle trạng thái sản phẩm (ẩn ↔ hiện).
     * - Nếu đang ACTIVE   → chuyển sang isActive=false, prodStatus=INACTIVE (ẩn).
     * - Nếu đang INACTIVE → chuyển sang isActive=true,  prodStatus=ACTIVE   (khôi phục).
     */
    @Transactional
    public String toggleProductStatus(Long productId) {
        Product product = productRepository.findById(productId)
                .orElseThrow(() -> new BusinessException(ErrorCode.RESOURCE_NOT_FOUND));

        boolean activate = !Boolean.TRUE.equals(product.getIsActive());

        product.setIsActive(activate);
        product.setProdStatus(activate ? ProductStatus.ACTIVE : ProductStatus.INACTIVE);
        productRepository.save(product);

        log.info("Product status toggled: id={}, newStatus={}", productId, activate);
        return activate ? "Đã hiện sản phẩm: " + product.getProductName() : "Đã ẩn sản phẩm: " + product.getProductName();
    }

    /**
     * Tính tổng số lượng tồn kho từ các variant đang hoạt động của sản phẩm.
     */
    private int calculateTotalStock(Product product) {
        if (product.getVariants() == null) {
            return 0;
        }
        return product.getVariants().stream()
                .filter(v -> v.getIsActive() != null && v.getIsActive())
                .mapToInt(v -> v.getStockQuantity() != null ? v.getStockQuantity() : 0)
                .sum();
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

    /**
     * Update or create main product image.
     */
    private void updateMainProductImage(Product product, String imageUrl) {
        ProductImage mainImage = productImageRepository.findByProductIdAndIsMainTrue(product.getId())
                .orElse(null);

        if (mainImage != null) {
            mainImage.setImageUrl(imageUrl);
            productImageRepository.save(mainImage);
        } else {
            ProductImage newImage = ProductImage.builder()
                    .product(product)
                    .imageUrl(imageUrl)
                    .isMain(true)
                    .sortOrder(0)
                    .isActive(true)
                    .build();
            productImageRepository.save(newImage);
        }
    }
}
