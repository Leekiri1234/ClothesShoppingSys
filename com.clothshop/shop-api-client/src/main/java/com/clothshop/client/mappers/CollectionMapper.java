package com.clothshop.client.mappers;

import com.clothshop.client.dtos.response.CollectionResponse;
import com.clothshop.client.dtos.response.ProductListResponse;
import com.clothshop.domain.entities.marketing.Collection;
import com.clothshop.domain.entities.marketing.CollectionItem;
import com.clothshop.domain.entities.product.Product;
import com.clothshop.domain.entities.product.ProductImage;
import com.clothshop.domain.entities.product.ProductVariant;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.Named;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Mapper(componentModel = "spring")
public interface CollectionMapper {

    // Thay vì map trực tiếp "products", ta map từ "items" của Entity
    @Mapping(source = "items", target = "products", qualifiedByName = "mapItemsToProducts")
    CollectionResponse toCollectionResponse(Collection collection);

    // Hàm chuyển đổi từ List<CollectionItem> sang List<ProductListResponse>
    @Named("mapItemsToProducts")
    default List<ProductListResponse> mapItemsToProducts(List<CollectionItem> items) {
        if (items == null) return new ArrayList<>();
        return items.stream()
                .map(item -> toProductItemResponse(item.getProduct())) // Lấy product từ trong item ra
                .collect(Collectors.toList());
    }

    @Mapping(source = "id", target = "id")
    @Mapping(source = "id", target = "productId")
    @Mapping(source = "productName", target = "name")
    @Mapping(source = "productName", target = "productName")
    @Mapping(source = "productSlug", target = "slug")
    @Mapping(source = "productSlug", target = "productSlug")
    @Mapping(source = "category.categoryName", target = "categoryName")
    @Mapping(source = "product", target = "minPrice", qualifiedByName = "mapMinPrice")
    @Mapping(source = "product", target = "price", qualifiedByName = "mapMinPrice")
    @Mapping(source = "basePrice", target = "originalPrice")
    @Mapping(source = "product", target = "thumbnail", qualifiedByName = "getFirstImage")
    @Mapping(source = "product", target = "imageUrl", qualifiedByName = "getFirstImage")
    @Mapping(source = "product", target = "totalStock", qualifiedByName = "calculateTotalStock")
    @Mapping(target = "available", source = "product", qualifiedByName = "calculateAvailability")
    ProductListResponse toProductItemResponse(Product product);

    @Named("mapMinPrice")
    default Double mapMinPrice(Product product) {
        if (product == null || product.getVariants() == null || product.getVariants().isEmpty()) {
            return product != null && product.getBasePrice() != null ? product.getBasePrice().doubleValue() : 0.0;
        }
        return product.getVariants().stream()
                .filter(v -> v.getRetailPrice() != null)
                .map(v -> v.getRetailPrice().doubleValue())
                .min(Double::compareTo)
                .orElse(product.getBasePrice() != null ? product.getBasePrice().doubleValue() : 0.0);
    }

    @Named("calculateTotalStock")
    default Integer calculateTotalStock(Product product) {
        if (product == null || product.getVariants() == null) return 0;
        return product.getVariants().stream()
                .mapToInt(v -> v.getStockQuantity() != null ? v.getStockQuantity() : 0)
                .sum();
    }

    @Named("getFirstImage")
    default String getFirstImage(Product product) {
        if (product == null || product.getImages() == null || product.getImages().isEmpty()) {
            return "/images/no-image.png";
        }
        return product.getImages().get(0).getImageUrl();
    }

    @Named("calculateAvailability")
    default boolean calculateAvailability(Product product) {
        if (product == null || product.getVariants() == null) return false;
        return product.getVariants().stream()
                .anyMatch(v -> v.getStockQuantity() != null && v.getStockQuantity() > 0);
    }
}