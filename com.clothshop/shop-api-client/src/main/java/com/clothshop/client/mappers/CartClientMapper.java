package com.clothshop.client.mappers;

import com.clothshop.client.dtos.response.CartItemResponse;
import com.clothshop.domain.models.customer.CartItem;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.Named;

@Mapper(componentModel = "spring")
public interface CartClientMapper {

    @Mapping(source = "id", target = "cartItemId")
    @Mapping(source = "variant.id", target = "variantId")
    @Mapping(source = "variant.product.productName", target = "productName")
    @Mapping(source = "variant.product.productSlug", target = "productSlug")
    @Mapping(source = "variant.color", target = "colorName")
    @Mapping(source = "variant.sizeValue", target = "sizeName")
    @Mapping(source = "price", target = "price")
    @Mapping(source = "quantity", target = "quantity")
    @Mapping(target = "imageUrl", expression = "java(getItemImage(item))")
    @Mapping(source = "variant.stockQuantity", target = "maxStock")
    // Check null an toàn trước khi nhân tiền
    @Mapping(target = "subtotal", expression = "java(item.getPrice() != null ? item.getPrice().doubleValue() * item.getQuantity() : 0.0)")
    CartItemResponse toItemResponse(CartItem item);

    @Named("getItemImage")
    default String getItemImage(CartItem item) {
        if (item == null || item.getVariant() == null) return "/images/no-image.png";
        
        if (item.getVariant().getProduct() != null && 
            item.getVariant().getProduct().getImages() != null && 
            !item.getVariant().getProduct().getImages().isEmpty()) {
            return item.getVariant().getProduct().getImages().stream()
                    .findFirst()
                    .map(com.clothshop.domain.models.product.ProductImage::getImageUrl)
                    .orElse("/images/no-image.png");
        }

        String variantImage = item.getVariant().getImageUrl();
        if (variantImage != null && !variantImage.trim().isEmpty()) {
            return variantImage;
        }
        
        return "/images/no-image.png";
    }
}