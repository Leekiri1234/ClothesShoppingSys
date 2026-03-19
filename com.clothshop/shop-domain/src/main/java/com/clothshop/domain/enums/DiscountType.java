package com.clothshop.domain.enums;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public enum DiscountType {
    PERCENTAGE("Phần trăm (%)"),
    FIXED_AMOUNT("Số tiền cố định (VND)");

    private final String displayName;
}