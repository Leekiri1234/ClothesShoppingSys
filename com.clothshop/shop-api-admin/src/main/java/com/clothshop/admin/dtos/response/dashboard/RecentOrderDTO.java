package com.clothshop.admin.dtos.response.dashboard;

import com.clothshop.domain.enums.OrderStatus;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.io.Serializable;
import java.math.BigDecimal;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class RecentOrderDTO implements Serializable {
    private String orderInvoice;
    private String customerName;
    private BigDecimal totalPrice;
    private OrderStatus status;

    public String getStatusBadgeClass() {
        if (status == null) return " badge-secondary";
        return switch (status) {
            case PENDING -> " badge-warning";
            case SHIPPING -> " badge-info";
            case DELIVERED, COMPLETED -> " badge-success";
            default -> " badge-secondary";
        };
    }
}
