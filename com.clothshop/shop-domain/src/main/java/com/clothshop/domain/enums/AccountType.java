package com.clothshop.domain.enums;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public enum AccountType {
    CUSTOMER("Khách hàng"),
    STAFF("Nhân viên");

    private final String displayName;
}
