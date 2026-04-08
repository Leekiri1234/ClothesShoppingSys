package com.clothshop.client.mappers;

import com.clothshop.client.dtos.request.ProfileUpdateRequest;
import com.clothshop.client.dtos.response.CustomerProfileResponse;
import com.clothshop.domain.models.auth.Customer;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;

@Mapper(componentModel = "spring")
public interface CustomerProfileMapper {

    // Map Entity ra Response để hiện lên View
    @Mapping(source = "account.username", target = "username")
    @Mapping(source = "account.email", target = "email") // Lấy email từ Account
    @Mapping(source = "phoneNumber", target = "phone")
    CustomerProfileResponse toResponse(Customer customer);

    // Cập nhật Entity từ Request
    @Mapping(source = "phone", target = "phoneNumber")
    @Mapping(target = "id", ignore = true)
    @Mapping(target = "account", ignore = true)
    @Mapping(target = "email", ignore = true) // Cấm đổi email trong form này
    void updateCustomerFromRequest(ProfileUpdateRequest request, @MappingTarget Customer customer);
}