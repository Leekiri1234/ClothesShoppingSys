package com.clothshop.client.mappers;

import com.clothshop.client.dtos.response.ProductDetailResponse;
import com.clothshop.client.dtos.response.ProductListResponse;
import com.clothshop.client.dtos.response.VariantDetailResponse;
import com.clothshop.domain.entities.product.Category;
import com.clothshop.domain.entities.product.Product;
import com.clothshop.domain.entities.product.ProductImage;
import com.clothshop.domain.entities.product.ProductVariant;
import jakarta.persistence.EntityNotFoundException;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.Named;

import java.util.List;
import java.util.Set; // Thêm import Set
import java.util.stream.Collectors;

@Mapper(componentModel = "spring")
public interface ProductClientMapper {

    @Mapping(source = "id", target = "productId")
    @Mapping(source = "productDesc", target = "description")
    @Mapping(source = "category", target = "categoryName", qualifiedByName = "mapSafeCategoryName")
    @Mapping(source = ".", target = "price", qualifiedByName = "mapPrice")
    @Mapping(source = ".", target = "imageUrl", qualifiedByName = "getFirstImage")
    @Mapping(target = "available", source = ".", qualifiedByName = "calculateAvailability")
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

    // FIX: Đổi List thành Set ở tham số đầu vào cho khớp với Entity
    List<VariantDetailResponse> toVariantResponseList(Set<ProductVariant> variants);

    @Mapping(source = "category", target = "categoryName", qualifiedByName = "mapSafeCategoryName")
    @Mapping(source = ".", target = "price", qualifiedByName = "mapPrice")
    @Mapping(source = ".", target = "imageUrl", qualifiedByName = "getFirstImage")
    @Mapping(target = "available", source = ".", qualifiedByName = "calculateAvailability")
    ProductListResponse toListResponse(Product product);

    @Named("mapSafeCategoryName")
    default String mapSafeCategoryName(Category category) {
        if (category == null) return "Chưa phân loại";
        try {
            return category.getCategoryName();
        } catch (EntityNotFoundException e) {
            return "Danh mục không khả dụng";
        }
    }

    @Named("mapPrice")
    default Double mapPrice(Product product) {
        if (product == null) return 0.0;
        // variants giờ là Set nên dùng .isEmpty() vẫn OK
        if (product.getVariants() != null && !product.getVariants().isEmpty()) {
            return product.getVariants().stream()
                    .map(v -> v.getRetailPrice() != null ? v.getRetailPrice().doubleValue() : Double.MAX_VALUE)
                    .min(Double::compare)
                    .orElse(product.getBasePrice() != null ? product.getBasePrice().doubleValue() : 0.0);
        }
        return product.getBasePrice() != null ? product.getBasePrice().doubleValue() : 0.0;
    }

    @Named("getFirstImage")
    default String getFirstImage(Product product) {
        // FIX: Set không có .get(0), dùng stream().findFirst() để lấy ảnh đầu tiên
        if (product == null || product.getImages() == null || product.getImages().isEmpty()) {
            return "https://via.placeholder.com/300x300?text=No+Image";
        }
        return product.getImages().stream()
                .findFirst()
                .map(ProductImage::getImageUrl)
                .orElse("https://via.placeholder.com/300x300?text=No+Image");
    }

    @Named("calculateAvailability")
    default boolean calculateAvailability(Product product) {
        if (product == null || product.getVariants() == null) {
            return false;
        }
        return product.getVariants().stream()
                .anyMatch(variant -> variant.getStockQuantity() != null && variant.getStockQuantity() > 0);
    }

    @Named("mapImages")
    // FIX: Đổi List thành Set ở đây
    default List<String> mapImages(Set<ProductImage> images) {
        if (images == null) return null;
        return images.stream()
                .map(ProductImage::getImageUrl)
                .collect(Collectors.toList());
    }
}