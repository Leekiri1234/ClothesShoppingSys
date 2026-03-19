package com.clothshop.admin.dtos.response.marketing;

import com.clothshop.domain.enums.DiscountType;
import lombok.*;
import java.math.BigDecimal;
import java.time.LocalDateTime;

@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class VoucherResponse {
    private Long id;
    private String code;
    private DiscountType discountType;
    private BigDecimal discountValue;
    private BigDecimal minOrderAmount;
    private BigDecimal maxDiscount;
    private Integer currentUsage;
    private Integer usageLimit;
    private String status;
    private LocalDateTime validFrom;
    private LocalDateTime validTo;
}