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
import java.util.stream.Collectors;

@Mapper(componentModel = "spring")
public interface ProductClientMapper {

    @Mapping(source = "productDesc", target = "description")
    // Sử dụng map an toàn cho Category Name
    @Mapping(source = "category", target = "categoryName", qualifiedByName = "mapSafeCategoryName")
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

    // Sử dụng map an toàn cho Category Name
    @Mapping(source = "category", target = "categoryName", qualifiedByName = "mapSafeCategoryName")
    @Mapping(source = "product", target = "price", qualifiedByName = "mapPrice")
    @Mapping(source = "product", target = "imageUrl", qualifiedByName = "getFirstImage")
    @Mapping(target = "available", source = "product", qualifiedByName = "calculateAvailability")
    ProductListResponse toListResponse(Product product);

    /**
     * Phương thức giải quyết lỗi EntityNotFoundException
     * Khi Category bị Soft Delete hoặc Inactive, Hibernate sẽ ném lỗi khi truy cập property
     */
    @Named("mapSafeCategoryName")
    default String mapSafeCategoryName(Category category) {
        if (category == null) return "Chưa phân loại";
        try {
            // Ép Hibernate initialize proxy để bắt lỗi tại đây
            return category.getCategoryName();
        } catch (EntityNotFoundException e) {
            return "Danh mục không khả dụng";
        }
    }

    @Named("mapPrice")
    default Double mapPrice(Product product) {
        if (product == null) return 0.0;
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