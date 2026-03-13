package com.clothshop.admin.mappers;

import com.clothshop.admin.dtos.request.products.ProductCreateRequest;
import com.clothshop.admin.dtos.request.products.ProductUpdateRequest;
import com.clothshop.admin.dtos.response.products.ProductAdminResponse;
import com.clothshop.domain.entities.product.Category;
import com.clothshop.domain.entities.product.Product;
import jakarta.persistence.EntityNotFoundException;
import org.mapstruct.*;

@Mapper(componentModel = "spring", nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
public interface ProductAdminMapper {

    // 1. Entity -> Response
    @Mapping(source = "productDesc", target = "description")
    @Mapping(source = "prodStatus", target = "status")
    @Mapping(source = "id", target = "productId")
    @Mapping(source = "basePrice", target = "price")
    // Sử dụng custom mapping để tránh EntityNotFoundException
    @Mapping(target = "categoryId", source = "category", qualifiedByName = "mapSafeCategoryId")
    @Mapping(target = "categoryName", source = "category", qualifiedByName = "mapSafeCategoryName")
    ProductAdminResponse toResponse(Product product);

    // Helper method để lấy ID an toàn
    @Named("mapSafeCategoryId")
    default Long mapSafeCategoryId(Category category) {
        if (category == null) return null;
        try {
            return category.getId();
        } catch (EntityNotFoundException e) {
            return null;
        }
    }

    // Helper method để lấy Tên an toàn
    @Named("mapSafeCategoryName")
    default String mapSafeCategoryName(Category category) {
        if (category == null) return "N/A";
        try {
            return category.getCategoryName();
        } catch (EntityNotFoundException e) {
            return "Danh mục không tồn tại"; // Trả về text thay vì làm sập trang
        }
    }

    // 2. Request -> Entity
    @Mapping(source = "description", target = "productDesc")
    @Mapping(source = "price", target = "basePrice")
    @Mapping(target = "id", ignore = true)
    @Mapping(target = "productSlug", ignore = true)
    @Mapping(target = "prodStatus", ignore = true)
    @Mapping(target = "category", ignore = true)
    @Mapping(target = "variants", ignore = true)
    @Mapping(target = "images", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    @Mapping(target = "isActive", ignore = true)
    @Mapping(target = "createdBy", ignore = true)
    @Mapping(target = "updatedBy", ignore = true)
    Product toEntity(ProductCreateRequest request);

    // 3. Update Entity
    @Mapping(source = "description", target = "productDesc")
    @Mapping(source = "price", target = "basePrice")
    @Mapping(target = "id", ignore = true)
    @Mapping(target = "productSlug", ignore = true)
    @Mapping(target = "prodStatus", ignore = true)
    @Mapping(target = "category", ignore = true)
    @Mapping(target = "variants", ignore = true)
    @Mapping(target = "images", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    @Mapping(target = "isActive", ignore = true)
    @Mapping(target = "createdBy", ignore = true)
    @Mapping(target = "updatedBy", ignore = true)
    void updateEntityFromRequest(ProductUpdateRequest request, @MappingTarget Product product);
}