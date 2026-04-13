package com.clothshop.client.mappers;

import com.clothshop.client.dtos.response.CollectionResponse;
import com.clothshop.client.dtos.response.ProductListResponse;
import com.clothshop.domain.models.marketing.Collection;
import com.clothshop.domain.models.marketing.CollectionItem;
import com.clothshop.domain.models.product.Category;
import com.clothshop.domain.models.product.Product;
import com.clothshop.domain.models.product.ProductImage;
import jakarta.persistence.EntityNotFoundException;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.Named;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Mapper(componentModel = "spring")
public interface CollectionClientMapper {

    // ÉP MAPPING các trường cơ bản để đảm bảo slug không bao giờ bị null khi trả về
    @Mapping(source = "id", target = "id")
    @Mapping(source = "name", target = "name")
    @Mapping(source = "slug", target = "slug")
    @Mapping(source = "description", target = "description")
    @Mapping(source = ".", target = "bannerUrl", qualifiedByName = "normalizeCollectionImageUrl")
    @Mapping(source = "items", target = "products", qualifiedByName = "mapItemsToProducts")
    CollectionResponse toCollectionResponse(Collection collection);

    @Named("normalizeCollectionImageUrl")
    default String normalizeCollectionImageUrl(Collection collection) {
        if (collection == null) return "/images/no-image.png";

        String raw = collection.getImageUrl();
        if (raw == null || raw.isBlank()) {
            raw = collection.getBannerUrl();
        }

        if (raw == null || raw.isBlank() || "undefined".equalsIgnoreCase(raw.trim())) {
            return "/images/no-image.png";
        }

        String normalized = raw.trim().replace("\\", "/");
        if (normalized.startsWith("http://") || normalized.startsWith("https://") || normalized.startsWith("/")) {
            return normalized;
        }
        return "/uploads/" + normalized;
    }

    @Named("mapItemsToProducts")
    default List<ProductListResponse> mapItemsToProducts(List<CollectionItem> items) {
        if (items == null) return new ArrayList<>();
        return items.stream()
                .map(item -> toProductItemResponse(item.getProduct()))
                .filter(res -> res != null)
                .collect(Collectors.toList());
    }

    @Mapping(source = "id", target = "productId")
    @Mapping(source = "productName", target = "productName")
    @Mapping(source = "productSlug", target = "productSlug")
    @Mapping(source = "category", target = "categoryName", qualifiedByName = "mapSafeCategoryName")
    @Mapping(source = "basePrice", target = "originalPrice")
    @Mapping(source = ".", target = "price", qualifiedByName = "mapMinPrice")
    @Mapping(source = ".", target = "imageUrl", qualifiedByName = "getFirstImage")
    @Mapping(source = ".", target = "totalStock", qualifiedByName = "calculateTotalStock")
    @Mapping(source = ".", target = "available", qualifiedByName = "calculateAvailability")
    ProductListResponse toProductItemResponse(Product product);

    @Named("mapSafeCategoryName")
    default String mapSafeCategoryName(Category category) {
        if (category == null) return "Chưa phân loại";
        try {
            return category.getCategoryName();
        } catch (EntityNotFoundException e) {
            return "Danh mục không khả dụng";
        }
    }

    @Named("mapMinPrice")
    default Double mapMinPrice(Product product) {
        if (product == null) return 0.0;
        if (product.getVariants() == null || product.getVariants().isEmpty()) {
            return product.getBasePrice() != null ? product.getBasePrice().doubleValue() : 0.0;
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
        return product.getImages().stream()
                .findFirst()
                .map(ProductImage::getImageUrl)
                .orElse("/images/no-image.png");
    }

    @Named("calculateAvailability")
    default boolean calculateAvailability(Product product) {
        if (product == null || product.getVariants() == null) return false;
        return product.getVariants().stream()
                .anyMatch(v -> v.getStockQuantity() != null && v.getStockQuantity() > 0);
    }
}