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
    @Mapping(source = "variant.imageUrl", target = "imageUrl", qualifiedByName = "getVariantImage")
    @Mapping(source = "variant.stockQuantity", target = "maxStock")
    // Check null an toàn trước khi nhân tiền
    @Mapping(target = "subtotal", expression = "java(item.getPrice() != null ? item.getPrice().doubleValue() * item.getQuantity() : 0.0)")
    CartItemResponse toItemResponse(CartItem item);

    @Named("getVariantImage")
    default String getVariantImage(String variantImage) {
        return variantImage != null ? variantImage : "/images/no-image.png";
    }
}