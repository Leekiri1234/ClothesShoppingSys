package com.clothshop.domain.enums;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public enum RmaType {
    RETURN("Trả hàng - Hoàn tiền"),
    EXCHANGE("Đổi kích cỡ/màu sắc");

    private final String displayName;
}