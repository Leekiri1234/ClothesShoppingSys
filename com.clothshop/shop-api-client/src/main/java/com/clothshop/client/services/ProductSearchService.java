package com.clothshop.client.services;

import com.clothshop.client.dtos.request.ProductSearchRequest;
import com.clothshop.client.dtos.response.ProductListResponse;
import com.clothshop.client.mappers.ProductClientMapper;
import com.clothshop.domain.models.product.Product;
import com.clothshop.domain.repositories.product.ProductRepository;
import com.clothshop.domain.specifications.ProductSpecification;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;

@Service
@RequiredArgsConstructor
@Slf4j
public class ProductSearchService {
    private final ProductRepository productRepository;
    private final ProductClientMapper productMapper;

    @Transactional(readOnly = true)
    public Page<ProductListResponse> search(ProductSearchRequest request) {

        Sort sort = Sort.by(Sort.Direction.DESC, "createdAt");
        if ("price_asc".equals(request.getSort())) {
            sort = Sort.by(Sort.Direction.ASC, "basePrice");
        } else if ("price_desc".equals(request.getSort())) {
            sort = Sort.by(Sort.Direction.DESC, "basePrice");
        }

        Pageable pageable = PageRequest.of(request.getPage(), request.getSize(), sort);

        BigDecimal minPrice = request.getMinPrice() != null ? BigDecimal.valueOf(request.getMinPrice()) : null;
        BigDecimal maxPrice = request.getMaxPrice() != null ? BigDecimal.valueOf(request.getMaxPrice()) : null;

        Specification<Product> spec = ProductSpecification.filterProducts(
                request.getKeyword(),
                request.getCategoryIds(),
                minPrice,
                maxPrice,
                request.getColors(),
                request.getSizes()
        );

        // Also add collection logic if needed, but since it's just dynamic filter we can combine:
        if (request.getCollectionSlug() != null && !request.getCollectionSlug().isBlank()) {
            Specification<Product> collSpec = (root, query, cb) -> {
                jakarta.persistence.criteria.Join<Object, Object> collItems = root.join("collectionItems");
                return cb.equal(collItems.get("collection").get("slug"), request.getCollectionSlug());
            };
            spec = spec.and(collSpec);
        }

        Page<Product> productPage = productRepository.findAll(spec, pageable);

        return productPage.map(productMapper::toListResponse);
    }
}
