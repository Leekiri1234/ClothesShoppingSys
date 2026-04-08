package com.clothshop.admin.mappers;

import com.clothshop.admin.dtos.request.marketing.VoucherCreateRequest;
import com.clothshop.admin.dtos.request.marketing.VoucherUpdateRequest;
import com.clothshop.admin.dtos.response.marketing.VoucherResponse;
import com.clothshop.domain.models.marketing.Voucher;
import org.mapstruct.*;

@Mapper(componentModel = "spring", nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
public interface VoucherMapper {
    @Mapping(source = "minOrderValue", target = "minOrderAmount")
    VoucherResponse toResponse(Voucher voucher);

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "currentUsage", constant = "0")
    @Mapping(target = "status", constant = "ACTIVE")
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    @Mapping(target = "createdBy", ignore = true)
    @Mapping(target = "updatedBy", ignore = true)
    @Mapping(target = "isActive", constant = "true")
    Voucher toEntity(VoucherCreateRequest request);

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "code", ignore = true) // Cấm sửa code
    @Mapping(target = "currentUsage", ignore = true)
    @Mapping(target = "status", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    @Mapping(target = "createdBy", ignore = true)
    @Mapping(target = "updatedBy", ignore = true)
    @Mapping(target = "isActive", ignore = true)
    void updateEntityFromRequest(VoucherUpdateRequest request, @MappingTarget Voucher voucher);
}