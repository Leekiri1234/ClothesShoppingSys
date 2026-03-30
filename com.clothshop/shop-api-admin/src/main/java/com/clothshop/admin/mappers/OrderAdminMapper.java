package com.clothshop.admin.mappers;

import com.clothshop.admin.dtos.response.order.*;
import com.clothshop.domain.entities.order.*;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.Named;

@Mapper(componentModel = "spring")
public interface OrderAdminMapper {

    @Mapping(source = "id", target = "orderId")
    @Mapping(source = "customer.fullName", target = "customerName")
    OrderAdminResponse toListResponse(Order order);

    @Mapping(source = "id", target = "orderId")
    @Mapping(source = "customer.fullName", target = "customerName")
    @Mapping(source = "customer.email", target = "customerEmail")
    @Mapping(source = "statusHistory", target = "history")
    @Mapping(source = "orderItems", target = "items")
    @Mapping(source = "totalAmount", target = "totalAmount") // Explicit mapping
    @Mapping(source = "discount", target = "discount")
    @Mapping(source = "totalPrice", target = "totalPrice")
    OrderDetailResponse toDetailResponse(Order order);

    @Mapping(source = "variant.product.productName", target = "productName")
    @Mapping(source = ".", target = "variantName", qualifiedByName = "mapVariantName")
    @Mapping(target = "subTotal", expression = "java(item.getUnitPrice().multiply(java.math.BigDecimal.valueOf(item.getQuantity())))")
    OrderItemResponse toItemResponse(OrderItem item);

    @Mapping(source = "createdBy", target = "changedBy") // BaseEntity có createdBy
    OrderStatusHistoryResponse toHistoryResponse(OrderStatusHistory history);

    @Named("mapVariantName")
    default String mapVariantName(OrderItem item) {
        if (item.getVariant() == null) return "N/A";
        return item.getVariant().getSizeValue() + " - " + item.getVariant().getColor();
    }
}