package com.clothshop.admin.mappers;

import com.clothshop.admin.dtos.response.order.RmaAdminResponse;
import com.clothshop.domain.entities.order.RmaRequest;
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
    RmaAdminResponse toResponse(RmaRequest entity);

    List<RmaAdminResponse> toResponseList(List<RmaRequest> entities);
}