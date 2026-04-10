package com.clothshop.admin.dtos.request.marketing.validator;

import com.clothshop.admin.dtos.request.marketing.VoucherCreateRequest;
import com.clothshop.domain.enums.DiscountType; // Import Enum
import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;
import java.math.BigDecimal;

public class DiscountValidator implements ConstraintValidator<ValidDiscount, Object> {
    @Override
    public boolean isValid(Object dto, ConstraintValidatorContext context) {
        DiscountType type;
        BigDecimal value;

        if (dto instanceof VoucherCreateRequest req) {
            type = req.getDiscountType();
            value = req.getDiscountValue();
        } else {
            return true; // Bổ sung logic cho UpdateRequest nếu cần
        }

        if (value == null || type == null) return true;

        // So sánh an toàn tuyệt đối bằng Enum
        if (type == DiscountType.PERCENTAGE && value.compareTo(new BigDecimal("100")) > 0) {
            return false;
        }
        return true;
    }
}