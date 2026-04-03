package com.clothshop.client.dtos.request;

import com.clothshop.domain.enums.PaymentMethod;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class OrderCreateRequest {

    /** Họ và tên người nhận */
    @NotBlank(message = "Vui lòng nhập họ và tên")
    private String fullName;

    /** Số điện thoại */
    @NotBlank(message = "Vui lòng nhập số điện thoại")
    private String phoneNumber;

    /** Email */
    @Email(message = "Email không hợp lệ")
    private String email;

    /** Địa chỉ giao hàng (số nhà, tên đường) */
    @NotBlank(message = "Vui lòng nhập địa chỉ giao hàng")
    private String shippingAddress;

    /** Tỉnh / Thành */
    private String province;

    /** Quận / Huyện */
    private String district;

    /** Phương thức vận chuyển: STANDARD | EXPRESS */
    private String shippingMethod = "STANDARD";

    /** Mã voucher (optional) */
    private String voucherCode;

    /** Phương thức thanh toán */
    @NotNull(message = "Vui lòng chọn phương thức thanh toán")
    private PaymentMethod paymentMethod = PaymentMethod.COD;
}