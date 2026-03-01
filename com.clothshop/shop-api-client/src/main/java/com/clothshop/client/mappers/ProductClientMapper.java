package com.clothshop.client.mappers;

import com.clothshop.client.dtos.response.ProductDetailResponse;
import com.clothshop.client.dtos.response.ProductListResponse;
import com.clothshop.domain.entities.product.Product;
import com.clothshop.domain.entities.product.ProductVariant; // Nhớ import cái này
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.Named;

@Mapper(componentModel = "spring")
public interface ProductClientMapper {

    @Mapping(source = "productDesc", target = "description")
    @Mapping(source = "category.categoryName", target = "categoryName")
    @Mapping(target = "available", source = "product", qualifiedByName = "calculateAvailability")
    ProductDetailResponse toDetailResponse(Product product);

    @Mapping(source = "category.categoryName", target = "categoryName")
    @Mapping(target = "available", source = "product", qualifiedByName = "calculateAvailability")
    ProductListResponse toListResponse(Product product);

    @Named("calculateAvailability")
    default boolean calculateAvailability(Product product) {
        if (product == null || product.getVariants() == null) {
            return false;
        }
        // Một sản phẩm được coi là còn hàng nếu có ít nhất 1 variant còn hàng
        return product.getVariants().stream()
                .anyMatch(variant -> variant.getStockQuantity() != null && variant.getStockQuantity() > 0);
    }
}