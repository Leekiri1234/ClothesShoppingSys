package com.clothshop.domain.enums;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public enum VoucherStatus {
    ACTIVE("Đang áp dụng"),
    EXPIRED("Hết hạn"),
    DISABLED("Đã tạm dừng");

    private final String displayName;
}