package com.clothshop.admin.dtos.request.order;

import com.clothshop.domain.enums.OrderStatus;
import lombok.*;
import org.springframework.format.annotation.DateTimeFormat;

import java.time.LocalDateTime;
import java.util.List;

@Getter @Setter @NoArgsConstructor @AllArgsConstructor
public class OrderFilterRequest {
    private OrderStatus status; // Lọc theo 1 trạng thái duy nhất

    @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME)
    private LocalDateTime startDate;

    @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME)
    private LocalDateTime endDate;

    private String keyword; // Tìm theo mã hóa đơn hoặc tên khách hàng
}