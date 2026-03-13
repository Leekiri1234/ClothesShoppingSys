package com.clothshop.admin.services;

import com.clothshop.admin.dtos.request.products.*;
import com.clothshop.admin.dtos.response.products.CategoryAdminResponse;
import com.clothshop.admin.mappers.CategoryAdminMapper;
import com.clothshop.common.exceptions.BusinessException;
import com.clothshop.common.exceptions.ErrorCode;
import com.clothshop.common.utils.SlugUtils;
import com.clothshop.domain.entities.product.Category;
import com.clothshop.domain.enums.CategoryStatus;
import com.clothshop.domain.repositories.product.CategoryRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class CategoryService {
    private final CategoryRepository categoryRepository;
    private final CategoryAdminMapper categoryMapper;

    @Transactional(readOnly = true)
    public List<CategoryAdminResponse> getAllCategories() {
        return categoryRepository.findAllByIsActiveTrue().stream()
                .map(categoryMapper::toResponse)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public CategoryAdminResponse getCategoryById(Long id) {
        return categoryRepository.findById(id)
                .map(categoryMapper::toResponse)
                .orElseThrow(() -> new BusinessException(ErrorCode.RESOURCE_NOT_FOUND));
    }

    @Transactional
    public void createCategory(CategoryCreateRequest request) {
        Category category = categoryMapper.toEntity(request);
        category.setCategorySlug(generateUniqueSlug(request.getCategoryName()));
        category.setIsActive(true);
        category.setCatStatus(CategoryStatus.ACTIVE);  // Set enum instead of String
        setParent(category, request.getParentId());
        categoryRepository.save(category);
    }

    @Transactional
    public void updateCategory(Long id, CategoryUpdateRequest request) {
        Category category = categoryRepository.findById(id)
                .orElseThrow(() -> new BusinessException(ErrorCode.RESOURCE_NOT_FOUND));

        if (!category.getCategoryName().equals(request.getCategoryName())) {
            category.setCategorySlug(generateUniqueSlug(request.getCategoryName()));
        }

        categoryMapper.updateEntityFromRequest(request, category);
        setParent(category, request.getParentId());
        categoryRepository.save(category);
    }

    @Transactional
    public void deleteCategory(Long id) {
        Category category = categoryRepository.findById(id)
                .orElseThrow(() -> new BusinessException(ErrorCode.RESOURCE_NOT_FOUND));

        // Soft delete this category
        category.setIsActive(false);
        category.setCatStatus(CategoryStatus.INACTIVE);  // Use enum constant

        // Cascade soft delete all children categories
        if (category.getChildren() != null && !category.getChildren().isEmpty()) {
            softDeleteChildren(category.getChildren());
        }

        categoryRepository.save(category);
    }

    /**
     * Recursively soft delete all children categories
     */
    private void softDeleteChildren(List<Category> children) {
        for (Category child : children) {
            child.setIsActive(false);
            child.setCatStatus(CategoryStatus.INACTIVE);

            // Recursively delete grandchildren
            if (child.getChildren() != null && !child.getChildren().isEmpty()) {
                softDeleteChildren(child.getChildren());
            }

            categoryRepository.save(child);
        }
    }

    private void setParent(Category category, Long parentId) {
        if (parentId != null) {
            Category parent = categoryRepository.findById(parentId)
                    .orElseThrow(() -> new BusinessException(ErrorCode.RESOURCE_NOT_FOUND));
            category.setParent(parent);
        } else {
            category.setParent(null);
        }
    }

    private String generateUniqueSlug(String name) {
        String slug = SlugUtils.makeSlug(name);
        if (categoryRepository.existsByCategorySlug(slug)) {
            slug += "-" + System.currentTimeMillis() % 1000;
        }
        return slug;
    }
}