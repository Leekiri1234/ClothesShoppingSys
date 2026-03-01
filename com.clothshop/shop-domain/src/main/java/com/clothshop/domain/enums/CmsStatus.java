package com.clothshop.domain.enums;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public enum CmsStatus {
    ACTIVE("Hiển thị"),
    INACTIVE("Tạm ẩn");

    private final String displayName;
}
