package com.clothshop.domain.enums;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public enum RmaStatus {
    PENDING("Chờ phê duyệt"),
    APPROVED("Đã chấp nhận (Chờ khách gửi hàng)"),
    RECEIVED("Đã nhận được hàng trả"),
    REJECTED("Đã từ chối"),
    COMPLETED("Đã xử lý xong (Đã hoàn tiền/đổi hàng)");

    private final String displayName;
}