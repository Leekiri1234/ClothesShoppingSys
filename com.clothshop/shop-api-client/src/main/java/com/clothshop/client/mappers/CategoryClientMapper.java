package com.clothshop.client.mappers;

import com.clothshop.client.dtos.response.CategoryResponse;
import com.clothshop.domain.models.product.Category;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

import java.util.List;

/**
 * MapStruct Mapper for Category Entity to CategoryResponse DTO conversion.
 * Follows the architectural guideline: NEVER expose Entity to View layer.
 */
@Mapper(componentModel = "spring")
public interface CategoryClientMapper {

    /**
     * Convert Category entity to CategoryResponse DTO.
     *
     * @param category Category entity from shop-domain
     * @return CategoryResponse DTO for shop-api-client
     */
    @Mapping(source = "categoryName", target = "name")
    @Mapping(source = "categorySlug", target = "slug")
    @Mapping(source = "parent.id", target = "parentId")
    CategoryResponse toCategoryResponse(Category category);

    /**
     * Convert list of Category entities to list of CategoryResponse DTOs.
     *
     * @param categories List of Category entities
     * @return List of CategoryResponse DTOs
     */
    List<CategoryResponse> toCategoryResponseList(List<Category> categories);
}
