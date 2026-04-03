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
import com.clothshop.domain.entities.product.ProductImage;
import com.clothshop.domain.entities.product.ProductVariant;
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
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import jakarta.persistence.criteria.CriteriaBuilder;
import jakarta.persistence.criteria.CriteriaQuery;
import jakarta.persistence.criteria.Predicate;
import jakarta.persistence.criteria.Root;
import jakarta.persistence.criteria.Subquery;
import java.util.List;
import java.util.Map;
import java.util.HashMap;
import java.util.ArrayList;
import java.util.Locale;
import java.util.Optional;
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
        Product product = productRepository.findProductWithVariantsById(productId)
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
        response.setImageUrl(productImageRepository.findByProductIdAndIsMainTrue(productId)
            .map(ProductImage::getImageUrl)
            .map(url -> normalizeProductImageUrl(url, product))
            .orElseGet(() -> resolveFallbackProductImage(product)));

        // Logic tự động tính tổng stock dựa vào các variant đang hoạt động
        response.setStock(calculateTotalStock(product));

        return response;
    }

    /**
     * Get all products with pagination.
     * Memory-optimized with paging to avoid loading all records.
     */
    @Transactional(readOnly = true)
    public PageResponse<ProductAdminResponse> getAllProducts(PagingRequest pagingRequest,
                                                             String search,
                                                             Long categoryId,
                                                             String status) {
        pagingRequest.validate();

        // Create pageable with sorting
        Sort sort = Sort.by(Sort.Direction.fromString(pagingRequest.getSortDirection()),
                pagingRequest.getSortBy() != null ? pagingRequest.getSortBy() : "createdAt");
        Pageable pageable = PageRequest.of(pagingRequest.getPageNumber(), pagingRequest.getPageSize(), sort);

        // Fetch from database with pagination + dynamic filters
        Specification<Product> specification = buildProductFilterSpecification(search, categoryId, status);
        Page<Product> productPage = productRepository.findAll(specification, pageable);

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

        Map<Long, String> imageUrlByProductId = new HashMap<>();
        if (!productIds.isEmpty()) {
            productImageRepository.findMainImageUrlsByProductIds(productIds)
                .forEach(row -> imageUrlByProductId.put((Long) row[0], (String) row[1]));
        }

        // Convert to DTOs and set total stock from the aggregate map
        List<ProductAdminResponse> content = productPage.getContent().stream()
                .map(product -> {
                    ProductAdminResponse response = productMapper.toResponse(product);
                    response.setStock(stockByProductId.getOrDefault(product.getId(), 0));
                        response.setImageUrl(Optional.ofNullable(imageUrlByProductId.get(product.getId()))
                            .map(url -> normalizeProductImageUrl(url, product))
                            .filter(url -> !url.isBlank())
                    .orElseGet(() -> resolveFallbackProductImage(product)));
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

    private Specification<Product> buildProductFilterSpecification(String search,
                                                                   Long categoryId,
                                                                   String status) {
        return (root, query, cb) -> {
            List<Predicate> predicates = new ArrayList<>();

            if (search != null && !search.isBlank()) {
                String keyword = "%" + search.trim().toLowerCase(Locale.ROOT) + "%";
                predicates.add(cb.or(
                        cb.like(cb.lower(root.get("productName")), keyword),
                        cb.like(cb.lower(root.get("productSlug")), keyword)
                ));
            }

            if (categoryId != null) {
                predicates.add(cb.equal(root.get("category").get("id"), categoryId));
            }

            String normalizedStatus = status == null ? "" : status.trim().toLowerCase(Locale.ROOT);
            if (!normalizedStatus.isBlank()) {
                switch (normalizedStatus) {
                    case "inactive" -> predicates.add(cb.isFalse(root.get("isActive")));
                    case "out_of_stock" -> {
                        predicates.add(cb.isTrue(root.get("isActive")));
                        predicates.add(cb.lessThanOrEqualTo(buildActiveStockSumSubQuery(query, cb, root), 0));
                    }
                    case "low_stock" -> {
                        predicates.add(cb.isTrue(root.get("isActive")));
                        predicates.add(cb.greaterThan(buildActiveStockSumSubQuery(query, cb, root), 0));
                        predicates.add(cb.lessThanOrEqualTo(buildActiveStockSumSubQuery(query, cb, root), 10));
                    }
                    case "active" -> {
                        predicates.add(cb.isTrue(root.get("isActive")));
                        predicates.add(cb.greaterThan(buildActiveStockSumSubQuery(query, cb, root), 0));
                    }
                    default -> {
                        // No-op for unsupported status value.
                    }
                }
            }

            return cb.and(predicates.toArray(new Predicate[0]));
        };
    }

    private Subquery<Integer> buildActiveStockSumSubQuery(CriteriaQuery<?> query,
                                                           CriteriaBuilder cb,
                                                           Root<Product> productRoot) {
        Subquery<Integer> stockSumSubQuery = query.subquery(Integer.class);
        Root<ProductVariant> variantRoot = stockSumSubQuery.from(ProductVariant.class);
        stockSumSubQuery.select(cb.coalesce(cb.sum(variantRoot.get("stockQuantity")), 0));
        stockSumSubQuery.where(
                cb.equal(variantRoot.get("product"), productRoot),
                cb.isTrue(variantRoot.get("isActive"))
        );
        return stockSumSubQuery;
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

    @Transactional
    public int bulkSetProductStatus(List<Long> productIds, boolean activate) {
        if (productIds == null || productIds.isEmpty()) {
            throw new BusinessException(ErrorCode.INVALID_KEY, "Danh sach san pham trong");
        }

        List<Product> products = productRepository.findAllById(productIds);
        if (products.isEmpty()) {
            throw new BusinessException(ErrorCode.RESOURCE_NOT_FOUND, "Khong tim thay san pham nao");
        }

        products.forEach(product -> {
            product.setIsActive(activate);
            product.setProdStatus(activate ? ProductStatus.ACTIVE : ProductStatus.INACTIVE);
        });

        productRepository.saveAll(products);
        log.info("Bulk set product status: count={}, activate={}", products.size(), activate);
        return products.size();
    }

    @Transactional
    public int bulkDeleteProducts(List<Long> productIds) {
        if (productIds == null || productIds.isEmpty()) {
            throw new BusinessException(ErrorCode.INVALID_KEY, "Danh sach san pham trong");
        }

        List<Product> products = productRepository.findAllById(productIds);
        if (products.isEmpty()) {
            throw new BusinessException(ErrorCode.RESOURCE_NOT_FOUND, "Khong tim thay san pham nao");
        }

        products.forEach(product -> {
            product.setIsActive(false);
            product.setProdStatus(ProductStatus.INACTIVE);
        });

        productRepository.saveAll(products);
        log.info("Bulk soft delete products: count={}", products.size());
        return products.size();
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

    private String resolveFallbackProductImage(Product product) {
        String categorySlug = product.getCategory() != null && product.getCategory().getCategorySlug() != null
                ? product.getCategory().getCategorySlug().toLowerCase(Locale.ROOT)
                : "";

        if (categorySlug.contains("women")) {
            return "/images/admin/product-dress.svg";
        }
        if (categorySlug.contains("bag") || categorySlug.contains("accessories")) {
            return "/images/admin/product-bag.svg";
        }
        return "/images/admin/product-shirt.svg";
    }

    private String normalizeProductImageUrl(String imageUrl, Product product) {
        if (imageUrl == null || imageUrl.isBlank()) {
            return resolveFallbackProductImage(product);
        }
        if (imageUrl.startsWith("http://") || imageUrl.startsWith("https://")) {
            return resolveFallbackProductImage(product);
        }
        return imageUrl;
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
