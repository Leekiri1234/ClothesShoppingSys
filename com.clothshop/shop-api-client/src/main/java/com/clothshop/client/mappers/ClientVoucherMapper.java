package com.clothshop.client.mappers;

import com.clothshop.client.dtos.response.VoucherClientResponse;
import com.clothshop.domain.models.marketing.Voucher;
import org.mapstruct.Mapper;

import java.util.List;

@Mapper(componentModel = "spring")
public interface ClientVoucherMapper {
    VoucherClientResponse toResponse(Voucher voucher);
    List<VoucherClientResponse> toResponseList(List<Voucher> vouchers);
}