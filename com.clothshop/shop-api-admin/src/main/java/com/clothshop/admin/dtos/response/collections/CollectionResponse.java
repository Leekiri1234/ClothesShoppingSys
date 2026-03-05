package com.clothshop.admin.dtos.response.collections;

import com.clothshop.admin.dtos.response.marketing.ProductSummaryDTO;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CollectionResponse {
    private Long id;
    private String name;
    private String description;
    private boolean active;

    // Danh sách sản phẩm thu gọn để hiển thị trong Collection
    private List<ProductSummaryDTO> products;
}
