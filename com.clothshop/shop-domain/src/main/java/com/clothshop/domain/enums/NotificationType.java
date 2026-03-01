package com.clothshop.domain.enums;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public enum NotificationType {
    SYSTEM("Hệ thống"),
    PROMOTION("Khuyến mãi"),
    ORDER_UPDATE("Cập nhật đơn hàng");

    private final String displayName;
}
