package com.clothshop.admin.mappers;

import com.clothshop.admin.dtos.request.products.CategoryCreateRequest;
import com.clothshop.admin.dtos.request.products.CategoryUpdateRequest;
import com.clothshop.admin.dtos.response.products.CategoryAdminResponse;
import com.clothshop.domain.models.product.Category;
import org.mapstruct.*;

@Mapper(componentModel = "spring", nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
public interface CategoryAdminMapper {

    @Mapping(target = "parentId", expression = "java(getParentId(category))")
    @Mapping(target = "parentName", expression = "java(getParentName(category))")
    CategoryAdminResponse toResponse(Category category);

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "categorySlug", ignore = true)
    @Mapping(target = "parent", ignore = true)
    @Mapping(target = "children", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    @Mapping(target = "isActive", ignore = true)
    @Mapping(target = "products", ignore = true)
    Category toEntity(CategoryCreateRequest request);

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "categorySlug", ignore = true)
    @Mapping(target = "parent", ignore = true)
    @Mapping(target = "children", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    @Mapping(target = "isActive", ignore = true)
    @Mapping(target = "products", ignore = true)
    void updateEntityFromRequest(CategoryUpdateRequest request, @MappingTarget Category category);

    // Helper methods to safely get parent info without circular reference
    default Long getParentId(Category category) {
        try {
            return category.getParent() != null ? category.getParent().getId() : null;
        } catch (Exception e) {
            // Parent has been soft deleted
            return null;
        }
    }

    default String getParentName(Category category) {
        try {
            return category.getParent() != null ? category.getParent().getCategoryName() : null;
        } catch (Exception e) {
            // Parent has been soft deleted
            return null;
        }
    }
}