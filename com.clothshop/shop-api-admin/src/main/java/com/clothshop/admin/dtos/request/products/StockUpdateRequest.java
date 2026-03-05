package com.clothshop.admin.dtos.request.products;

import jakarta.validation.constraints.*;
import lombok.*;

/**
 * Request DTO for updating variant stock.
 * Client chỉ gửi số lượng kho thực tế hiện tại (newStock).
 * Backend sẽ tự tính độ lệch (delta) để ghi log, chống việc client thao túng số liệu.
 */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class StockUpdateRequest {

    @NotNull(message = "New stock value is required")
    @Min(value = 0, message = "Stock cannot be negative")
    private Integer newStock;

    @NotBlank(message = "Reason is required")
    @Size(max = 100, message = "Reason must not exceed 100 characters")
    private String reason; // Ví dụ: "RESTOCK", "MANUAL_ADJUSTMENT", "RETURN"

    // Có thể bổ sung note chi tiết nếu cần
    private String note;
}