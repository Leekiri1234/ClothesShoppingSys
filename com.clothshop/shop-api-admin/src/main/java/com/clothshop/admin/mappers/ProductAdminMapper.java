package com.clothshop.admin.mappers;

import com.clothshop.admin.dtos.request.products.ProductCreateRequest;
import com.clothshop.admin.dtos.request.products.ProductUpdateRequest;
import com.clothshop.admin.dtos.response.products.ProductAdminResponse;
import com.clothshop.domain.entities.product.Product;
import org.mapstruct.*;

@Mapper(componentModel = "spring", nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
public interface ProductAdminMapper {

    // 1. Entity -> Response
    @Mapping(source = "productDesc", target = "description")
    @Mapping(source = "prodStatus", target = "status")
    // Sửa 'categoryId' thành 'id' vì trong Java nó là biến id từ BaseEntity
    @Mapping(source = "category.id", target = "categoryId")
    @Mapping(source = "category.categoryName", target = "categoryName")
    // Map PK của Product
    @Mapping(source = "id", target = "productId")
    ProductAdminResponse toResponse(Product product);

    // 2. Request -> Entity
    @Mapping(source = "description", target = "productDesc")
    @Mapping(target = "id", ignore = true)
    @Mapping(target = "productSlug", ignore = true)
    @Mapping(target = "prodStatus", ignore = true)
    @Mapping(target = "category", ignore = true)
    @Mapping(target = "variants", ignore = true)
    @Mapping(target = "images", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    @Mapping(target = "isActive", ignore = true)
    Product toEntity(ProductCreateRequest request);

    // 3. Update Entity
    @Mapping(source = "description", target = "productDesc")
    @Mapping(target = "id", ignore = true)
    @Mapping(target = "productSlug", ignore = true)
    @Mapping(target = "prodStatus", ignore = true)
    @Mapping(target = "category", ignore = true)
    @Mapping(target = "variants", ignore = true)
    @Mapping(target = "images", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    @Mapping(target = "isActive", ignore = true)
    void updateEntityFromRequest(ProductUpdateRequest request, @MappingTarget Product product);
}