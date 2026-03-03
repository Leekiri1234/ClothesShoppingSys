package com.clothshop.domain.enums;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public enum AccountStatus {
    ACTIVE("Đang hoạt động"),
    LOCKED("Đã khóa");

    private final String displayName;
}
