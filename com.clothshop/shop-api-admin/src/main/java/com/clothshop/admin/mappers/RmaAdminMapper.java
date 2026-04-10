package com.clothshop.admin.mappers;

import com.clothshop.admin.dtos.response.order.RmaAdminResponse;
import com.clothshop.domain.models.order.OrderItem;
import com.clothshop.domain.models.order.RmaRequest;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.ReportingPolicy;

import java.util.List;

@Mapper(componentModel = "spring", unmappedTargetPolicy = ReportingPolicy.IGNORE)
public interface RmaAdminMapper {

    @Mapping(source = "order.id", target = "orderId")
    @Mapping(source = "order.orderInvoice", target = "orderInvoice")
    @Mapping(source = "customer.fullName", target = "customerName")
    // Ánh xạ số điện thoại từ Entity Customer sang DTO
    @Mapping(source = "customer.phoneNumber", target = "customerPhone")
    @Mapping(source = "order.orderItems", target = "orderItems")
    @Mapping(source = "order.totalPrice", target = "totalPrice")
    RmaAdminResponse toResponse(RmaRequest entity);

    // Map chi tiết từng Item trong đơn hàng
    @Mapping(source = "variant.product.productName", target = "productName")
    @Mapping(source = "variant.sku", target = "sku")
    @Mapping(source = "variant.imageUrl", target = "imageUrl")
    @Mapping(target = "variantInfo", expression = "java(\"Màu: \" + item.getVariant().getColor() + \", Size: \" + item.getVariant().getSizeValue())")
    RmaAdminResponse.RmaOrderItemResponse toItemResponse(OrderItem item);

    List<RmaAdminResponse> toResponseList(List<RmaRequest> entities);
}