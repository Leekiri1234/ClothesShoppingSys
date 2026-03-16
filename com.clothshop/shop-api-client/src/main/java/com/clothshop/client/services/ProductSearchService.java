package com.clothshop.client.services;

import com.clothshop.client.dtos.request.ProductSearchRequest;
import com.clothshop.client.dtos.response.ProductListResponse;
import com.clothshop.client.mappers.ProductClientMapper;
import com.clothshop.domain.entities.product.Product;
import com.clothshop.domain.repositories.product.ProductRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
@Slf4j
public class ProductSearchService {
    private final ProductRepository productRepository;
    private final ProductClientMapper productMapper;

    public Page<ProductListResponse> search(ProductSearchRequest request) {
        Pageable pageable = PageRequest.of(request.getPage(), request.getSize());
        Page<Product> productPage;

        if (request.getKeyword() != null && !request.getKeyword().isBlank()) {
            productPage = productRepository.findByProductNameContainingIgnoreCaseAndIsActiveTrue(request.getKeyword(), pageable);
        } else if (request.getCategoryId() != null) {
            productPage = productRepository.findByCategory_IdAndIsActiveTrue(request.getCategoryId(), pageable);
        } else if (request.getCollectionSlug() != null) {
            productPage = productRepository.findByCollectionSlug(request.getCollectionSlug(), pageable);
        } else {
            productPage = productRepository.findAll(pageable); // Hoặc lấy sản phẩm mới nhất
        }

        return productPage.map(productMapper::toListResponse);    }
}
