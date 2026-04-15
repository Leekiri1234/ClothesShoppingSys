package com.clothshop.admin.mappers;

import com.clothshop.admin.dtos.response.customer.CustomerAdminResponse;
import com.clothshop.domain.models.auth.Customer;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring")
public interface CustomerAdminMapper {

    @Mapping(source = "account.username", target = "username")
    @Mapping(source = "account.accountStatus", target = "accountStatus")
    CustomerAdminResponse toResponse(Customer customer);
}