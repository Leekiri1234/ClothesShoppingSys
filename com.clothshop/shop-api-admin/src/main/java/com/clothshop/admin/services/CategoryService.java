package com.clothshop.admin.services;

import com.clothshop.admin.dtos.response.CategorySimpleResponse;
import com.clothshop.domain.repositories.product.CategoryRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

/**
 * Category Service for Admin.
 * Provides category data for dropdowns and management.
 */
@Service
@RequiredArgsConstructor
public class CategoryService {

    private final CategoryRepository categoryRepository;

    /**
     * Get all categories for dropdown (lightweight).
     */
    @Transactional(readOnly = true)
    public List<CategorySimpleResponse> getAllCategoriesForDropdown() {
        return categoryRepository.findAll().stream()
                .map(category -> CategorySimpleResponse.builder()
                        .categoryId(category.getId())
                        .categoryName(category.getCategoryName())
                        .build())
                .collect(Collectors.toList());
    }
}
