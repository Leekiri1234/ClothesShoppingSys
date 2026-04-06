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
    @Mapping(source = "customer.phoneNumber", target = "customerPhone")
    @Mapping(source = "statusHistory", target = "history")
    @Mapping(source = "orderItems", target = "items")
    @Mapping(target = "totalAmount", expression = "java(calculateSubtotal(order))") // Explicit mapping
    @Mapping(source = "discount", target = "discount")
    @Mapping(target = "totalPrice", expression = "java(calculateFinalPrice(order))")
    @Mapping(source = "paymentMethod", target = "paymentMethod")
    OrderDetailResponse toDetailResponse(Order order);

    default java.math.BigDecimal calculateSubtotal(Order order) {
        if (order.getOrderItems() == null) return java.math.BigDecimal.ZERO;
        return order.getOrderItems().stream()
                .map(item -> item.getUnitPrice().multiply(java.math.BigDecimal.valueOf(item.getQuantity())))
                .reduce(java.math.BigDecimal.ZERO, java.math.BigDecimal::add);
    }

    default java.math.BigDecimal calculateFinalPrice(Order order) {
        java.math.BigDecimal subtotal = calculateSubtotal(order);
        java.math.BigDecimal discount = order.getDiscount() != null ? order.getDiscount() : java.math.BigDecimal.ZERO;
        java.math.BigDecimal finalPrice = subtotal.subtract(discount);
        return finalPrice.compareTo(java.math.BigDecimal.ZERO) < 0 ? java.math.BigDecimal.ZERO : finalPrice;
    }

    @Mapping(source = "variant.product.productName", target = "productName")
    @Mapping(source = ".", target = "variantName", qualifiedByName = "mapVariantName")
    @Mapping(target = "subTotal", expression = "java(item.getUnitPrice().multiply(java.math.BigDecimal.valueOf(item.getQuantity())))")
    OrderItemResponse toItemResponse(OrderItem item);

    @Mapping(source = "createdBy", target = "changedBy") // BaseEntity có createdBy
    @Mapping(source = "createdAt", target = "changedAt")
    OrderStatusHistoryResponse toHistoryResponse(OrderStatusHistory history);

    @Named("mapVariantName")
    default String mapVariantName(OrderItem item) {
        if (item.getVariant() == null) return "N/A";
        return item.getVariant().getSizeValue() + " - " + item.getVariant().getColor();
    }
}