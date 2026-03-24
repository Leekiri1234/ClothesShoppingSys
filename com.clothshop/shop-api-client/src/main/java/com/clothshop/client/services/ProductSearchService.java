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
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Slf4j
public class ProductSearchService {
    private final ProductRepository productRepository;
    private final ProductClientMapper productMapper;

    @Transactional(readOnly = true)
    public Page<ProductListResponse> search(ProductSearchRequest request) {
        int page = Math.max(0, request.getPage());
        int size = request.getSize() > 0 ? request.getSize() : 12;
        Pageable pageable = PageRequest.of(page, size);
        Page<Product> productPage;

        // 1. Nếu có keyword -> Tìm theo Tên SP + Danh mục + Bộ sưu tập
        if (request.getKeyword() != null && !request.getKeyword().isBlank()) {
            productPage = productRepository.searchFullText(request.getKeyword().trim(), pageable);
        }
        // 2. Nếu không có keyword nhưng có CategoryId -> Lọc theo danh mục
        else if (request.getCategoryId() != null) {
            productPage = productRepository.findByCategory_IdAndIsActiveTrue(request.getCategoryId(), pageable);
        }
        // 3. Nếu lọc theo Collection Slug
        else if (request.getCollectionSlug() != null) {
            productPage = productRepository.findByCollectionSlug(request.getCollectionSlug(), pageable);
        }
        // 4. Mặc định lấy tất cả SP đang hoạt động
        else {
            productPage = productRepository.findAllActive(pageable);
        }

        return productPage.map(productMapper::toListResponse);
    }
}
