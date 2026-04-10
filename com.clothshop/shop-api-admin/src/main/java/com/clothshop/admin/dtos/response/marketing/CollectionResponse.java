package com.clothshop.admin.dtos.response.marketing;

import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CollectionResponse {

    private Long id;
    private String name;
    private String description;
    private Boolean isActive;

    //Trả về số lượng sản phẩm để hiển thị ở danh sách, tối ưu RAM thay vì trả cả List
    private Long itemCount;
}