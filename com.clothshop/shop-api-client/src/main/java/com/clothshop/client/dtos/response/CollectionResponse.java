package com.clothshop.client.dtos.response;

import lombok.Data;
import java.util.List;

@Data
public class CollectionResponse {
    private Long id;
    private String name;
    private String slug;
    private String description;
    private String bannerUrl;

    // Danh sách các sản phẩm thuộc bộ sưu tập này
    // Sử dụng lại ProductListResponse để đồng nhất giao diện thẻ sản phẩm (Card)
    private List<ProductListResponse> products;
}