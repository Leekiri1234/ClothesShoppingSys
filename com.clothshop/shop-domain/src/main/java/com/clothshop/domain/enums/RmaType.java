package com.clothshop.domain.enums;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public enum RmaType {
    // Tên cũ: Trả hàng - Hoàn tiền
    RETURN("Trả hàng & Hoàn tiền"),

    // Tên cũ: Đổi kích cỡ/màu sắc
    // Sửa thành: Trả hàng để đổi mới (Để khách hiểu là vẫn phải trả hàng lấy tiền)
    EXCHANGE("Trả hàng để đổi sản phẩm khác");

    private final String displayName;
}