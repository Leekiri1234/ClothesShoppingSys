package com.clothshop.client.mappers;

import com.clothshop.client.dtos.response.ProductDetailResponse;
import com.clothshop.client.dtos.response.ProductListResponse;
import com.clothshop.client.dtos.response.VariantDetailResponse;
import com.clothshop.domain.entities.product.Product;
import com.clothshop.domain.entities.product.ProductImage;
import com.clothshop.domain.entities.product.ProductVariant; // Nhớ import cái này
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.Named;

import java.util.List;
import java.util.stream.Collectors;

@Mapper(componentModel = "spring")
public interface ProductClientMapper {

    @Mapping(source = "productDesc", target = "description")
    @Mapping(source = "category.categoryName", target = "categoryName")
    @Mapping(source = "product", target = "price", qualifiedByName = "mapPrice")
    @Mapping(source = "product", target = "imageUrl", qualifiedByName = "getFirstImage")
    @Mapping(target = "available", source = "product", qualifiedByName = "calculateAvailability")
    @Mapping(target = "images", source = "images", qualifiedByName = "mapImages")
    @Mapping(source = "variants", target = "variants")
    ProductDetailResponse toDetailResponse(Product product);

    @Mapping(source = "id", target = "id")
    @Mapping(source = "color", target = "color")
    @Mapping(source = "sizeValue", target = "sizeValue")
    @Mapping(source = "stockQuantity", target = "stockQuantity")
    @Mapping(source = "retailPrice", target = "retailPrice")
    @Mapping(source = "imageUrl", target = "imageUrl")
    VariantDetailResponse toVariantResponse(ProductVariant variant);

    List<VariantDetailResponse> toVariantResponseList(List<ProductVariant> variants);

    @Mapping(source = "category.categoryName", target = "categoryName")
    @Mapping(source = "product", target = "price", qualifiedByName = "mapPrice")
    @Mapping(source = "product", target = "imageUrl", qualifiedByName = "getFirstImage")
    @Mapping(target = "available", source = "product", qualifiedByName = "calculateAvailability")
    ProductListResponse toListResponse(Product product);

    @Named("mapPrice")
    default Double mapPrice(Product product) {
        if (product == null) return 0.0;

        // Nếu có variants, lấy giá retail nhỏ nhất
        if (product.getVariants() != null && !product.getVariants().isEmpty()) {
            return product.getVariants().stream()
                    .map(v -> v.getRetailPrice() != null ? v.getRetailPrice().doubleValue() : Double.MAX_VALUE)
                    .min(Double::compare)
                    .orElse(product.getBasePrice() != null ? product.getBasePrice().doubleValue() : 0.0);
        }

        // Nếu không có variants, lấy giá base
        return product.getBasePrice() != null ? product.getBasePrice().doubleValue() : 0.0;
    }

    @Named("getFirstImage")
    default String getFirstImage(Product product) {
        if (product == null || product.getImages() == null || product.getImages().isEmpty()) {
            return "https://via.placeholder.com/300x300?text=No+Image";
        }
        return product.getImages().get(0).getImageUrl();
    }

    @Named("calculateAvailability")
    default boolean calculateAvailability(Product product) {
        if (product == null || product.getVariants() == null) {
            return false;
        }
        // Một sản phẩm được coi là còn hàng nếu có ít nhất 1 variant còn hàng
        return product.getVariants().stream()
                .anyMatch(variant -> variant.getStockQuantity() != null && variant.getStockQuantity() > 0);
    }

    @Named("mapImages")
    default List<String> mapImages(List<ProductImage> images) {
        if (images == null) return null;
        return images.stream()
                .map(ProductImage::getImageUrl)
                .collect(Collectors.toList());
    }
}