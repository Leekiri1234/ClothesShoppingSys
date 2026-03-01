package com.clothshop.domain.enums;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public enum PaymentMethod {
    COD("Thanh toán khi nhận hàng"),
    VNPAY("Thanh toán qua VNPAY"),
    MOMO("Thanh toán qua MoMo"),
    BANK_TRANSFER("Chuyển khoản ngân hàng");

    private final String displayName;
}
