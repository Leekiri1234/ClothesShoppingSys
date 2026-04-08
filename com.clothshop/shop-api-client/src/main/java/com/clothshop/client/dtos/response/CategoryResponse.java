package com.clothshop.client.dtos.response;

import lombok.*;

import java.util.ArrayList;
import java.util.List;

@Getter @Setter @NoArgsConstructor @AllArgsConstructor
@Builder
public class CategoryResponse {
    private Long id;
    private String name;
    private String slug;
    private Long parentId;

    @Builder.Default
    private List<CategoryResponse> children = new ArrayList<>();
}
