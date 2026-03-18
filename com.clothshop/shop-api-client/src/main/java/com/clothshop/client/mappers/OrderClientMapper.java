package com.clothshop.client.mappers;

import com.clothshop.client.dtos.response.OrderDetailResponse;
import com.clothshop.client.dtos.response.OrderListClientResponse;
import com.clothshop.domain.entities.order.Order;
import com.clothshop.domain.entities.order.OrderItem;
import com.clothshop.domain.entities.order.OrderStatusHistory;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

import java.util.List;

@Mapper(componentModel = "spring")
public interface OrderClientMapper {

    OrderListClientResponse toListResponse(Order order);

    @Mapping(source = "customer.address", target = "shippingAddress")
    @Mapping(source = "customer.phoneNumber", target = "customerPhone")
    @Mapping(source = "paymentMethod", target = "paymentMethod")
    @Mapping(source = "orderItems", target = "items")
    @Mapping(source = "statusHistory", target = "history")
    OrderDetailResponse toDetailResponse(Order order);

    @Mapping(source = "variant.product.productName", target = "productName")
    @Mapping(source = "variant.product.productSlug", target = "productSlug")
    @Mapping(expression = "java(item.getVariant().getColor() + \" | Size: \" + item.getVariant().getSizeValue())", target = "variantInfo")
    @Mapping(source = "variant.imageUrl", target = "imageUrl")
    @Mapping(expression = "java(item.getUnitPrice().doubleValue() * item.getQuantity())", target = "subtotal")
    OrderDetailResponse.OrderItemClientResponse toOrderItemResponse(OrderItem item);

    OrderDetailResponse.OrderHistoryClientResponse toHistoryResponse(OrderStatusHistory history);
}