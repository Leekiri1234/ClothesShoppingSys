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

    /**
     * Fetch ALL categories including soft-deleted ones.
     * Used by Admin list page to provide full visibility.
     */
    @Transactional(readOnly = true)
    public List<CategoryAdminResponse> getAllCategoriesIncludingInactive() {
        return categoryRepository.findAllIncludingInactive().stream()
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

    /**
     * Toggle trạng thái danh mục (ẩn ↔ hiện).
     * - Nếu đang ACTIVE   → chuyển sang isActive=false, catStatus=INACTIVE (ẩn).
     * - Nếu đang INACTIVE → chuyển sang isActive=true,  catStatus=ACTIVE   (khôi phục).
     * Cascade toggle xuống toàn bộ danh mục con theo cùng chiều.
     */
    @Transactional
    public String toggleCategoryStatus(Long id) {
        Category category = categoryRepository.findByIdIncludingInactive(id)
                .orElseThrow(() -> new BusinessException(ErrorCode.RESOURCE_NOT_FOUND));

        boolean activate = !category.getIsActive(); // true = đang ẩn → cần khôi phục

        applyStatusToCategory(category, activate);
        cascadeToggleToChildren(category, activate);

        categoryRepository.save(category);

        return activate ? "Đã khôi phục danh mục thành công!" : "Đã ẩn danh mục thành công!";
    }

    /** Áp dụng trạng thái lên một category */
    private void applyStatusToCategory(Category category, boolean activate) {
        category.setIsActive(activate);
        category.setCatStatus(activate ? CategoryStatus.ACTIVE : CategoryStatus.INACTIVE);
    }

    /**
     * Đệ quy cascade toggle xuống danh mục con.
     * Dùng findChildrenByParentIdIncludingInactive (bypass restriction) để lấy cả children đã ẩn khi restore.
     */
    private void cascadeToggleToChildren(Category parent, boolean activate) {
        List<Category> children = categoryRepository.findChildrenByParentIdIncludingInactive(parent.getId());
        for (Category child : children) {
            applyStatusToCategory(child, activate);
            cascadeToggleToChildren(child, activate);
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