package com.clothshop.admin.dtos.request.marketing.validator;

import jakarta.validation.Constraint;
import jakarta.validation.Payload;
import java.lang.annotation.*;

@Documented
@Constraint(validatedBy = DiscountValidator.class)
@Target({ElementType.TYPE})
@Retention(RetentionPolicy.RUNTIME)
public @interface ValidDiscount {
    String message() default "Giá trị giảm giá không hợp lệ (Phần trăm phải <= 100)";
    Class<?>[] groups() default {};
    Class<? extends Payload>[] payload() default {};
}