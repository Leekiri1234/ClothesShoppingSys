package com.clothshop.admin.dtos.request.marketing;

import com.clothshop.admin.dtos.request.marketing.validator.ValidDiscount;
import com.clothshop.domain.enums.DiscountType;
import jakarta.validation.constraints.*;
import lombok.*;
import java.math.BigDecimal;
import java.time.LocalDateTime;

@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
@ValidDiscount
public class VoucherCreateRequest {

    @NotBlank(message = "Mã voucher không được để trống")
    @Pattern(regexp = "^[A-Z0-9]+$", message = "Mã voucher chỉ chứa chữ in hoa và số")
    private String code;

    @NotNull(message = "Loại giảm giá không được để trống")
    private DiscountType discountType;

    @NotNull(message = "Giá trị giảm không được để trống")
    @DecimalMin(value = "0.1", message = "Giá trị giảm phải lớn hơn 0")
    private BigDecimal discountValue;

    @Min(value = 0, message = "Giá trị đơn tối thiểu không được âm")
    private BigDecimal minOrderValue;

    @Min(value = 0, message = "Giảm tối đa không được âm")
    private BigDecimal maxDiscount;

    @NotNull(message = "Ngày bắt đầu không được để trống")
    private LocalDateTime validFrom;

    @NotNull(message = "Ngày kết thúc không được để trống")
    private LocalDateTime validTo;

    @Min(value = 1, message = "Giới hạn sử dụng phải lớn hơn 0")
    private Integer usageLimit;
}