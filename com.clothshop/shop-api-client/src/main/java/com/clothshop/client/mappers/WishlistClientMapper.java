package com.clothshop.client.mappers;

import com.clothshop.client.dtos.response.WishlistItemResponse;
import com.clothshop.domain.entities.customer.WishlistItem;
import com.clothshop.domain.entities.product.Product;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.Named;

@Mapper(componentModel = "spring")
public interface WishlistClientMapper {

    @Mapping(source = "id", target = "itemId")
    @Mapping(source = "product.id", target = "productId")
    @Mapping(source = "product.productName", target = "productName")
    @Mapping(source = "product.productSlug", target = "productSlug")
    @Mapping(source = "product.basePrice", target = "price")
    @Mapping(source = "product", target = "thumbnail", qualifiedByName = "getFirstImage")
    @Mapping(source = "product.category.categoryName", target = "categoryName")
    WishlistItemResponse toItemResponse(WishlistItem item);

    @Named("getFirstImage")
    default String getFirstImage(Product product) {
        if (product == null || product.getImages() == null || product.getImages().isEmpty()) {
            return "https://via.placeholder.com/300";
        }

        return product.getImages().stream()
                .findFirst()
                .map(image -> image.getImageUrl())
                .orElse("https://via.placeholder.com/300");
    }
}