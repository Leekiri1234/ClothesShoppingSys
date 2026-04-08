package com.clothshop.admin.dtos.response.products;

import com.clothshop.domain.enums.CategoryStatus;
import lombok.*;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class CategoryAdminResponse {
    private Long id;
    private String categoryName;
    private String categorySlug;
    private Long parentId;
    private String parentName;
    private CategoryStatus status;
    private Boolean isActive;
    private LocalDateTime createdAt;

    @Builder.Default
    private List<CategoryAdminResponse> children = new ArrayList<>();
}