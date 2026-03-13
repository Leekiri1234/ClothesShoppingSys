package com.clothshop.admin.dtos.response.marketing;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

/**
 * DTO để trả về kết quả bulk assign products vào collection
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class BulkAssignResult {
    /**
     * Số lượng sản phẩm đã thêm thành công
     */
    private int addedCount;

    /**
     * Số lượng sản phẩm bị trùng (đã có sẵn trong collection)
     */
    private int duplicateCount;

    /**
     * Danh sách tên sản phẩm bị trùng (để hiển thị warning)
     */
    private List<String> duplicateProductNames;

    /**
     * Tổng số sản phẩm trong request
     */
    private int totalRequested;

    public boolean hasAnySuccess() {
        return addedCount > 0;
    }

    public boolean hasAnyDuplicates() {
        return duplicateCount > 0;
    }

    public boolean isAllDuplicates() {
        return duplicateCount == totalRequested;
    }
}

