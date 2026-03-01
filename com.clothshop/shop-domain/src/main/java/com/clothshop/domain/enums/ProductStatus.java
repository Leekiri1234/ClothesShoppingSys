package com.clothshop.domain.enums;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public enum ProductStatus {
    ACTIVE("Đang bán"),
    INACTIVE("Ngừng kinh doanh");

    private final String displayName;
}
