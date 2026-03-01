package com.clothshop.domain.enums;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public enum RmaStatus {
    PENDING("Chờ phê duyệt"),
    APPROVED("Đã chấp nhận"),
    REJECTED("Đã từ chối"),
    COMPLETED("Đã xử lý xong");

    private final String displayName;
}
