package com.clothshop.client.dtos.request;

import com.clothshop.domain.enums.PaymentMethod;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.*;

@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class OrderCreateRequest {

    private String voucherCode;

    @NotNull(message = "Vui lòng chọn phương thức thanh toán")
    private PaymentMethod paymentMethod;

    @NotBlank(message = "Địa chỉ giao hàng không được để trống")
    private String shippingAddress;

    @NotBlank(message = "Số điện thoại không được để trống")
    private String phoneNumber;
}