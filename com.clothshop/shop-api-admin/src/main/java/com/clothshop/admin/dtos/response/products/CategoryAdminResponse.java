package com.clothshop.admin.dtos.response.products;

import lombok.*;
import java.time.LocalDateTime;

@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class CategoryAdminResponse {
    private Long id;
    private String categoryName;
    private String categorySlug;
    private String catStatus;
    private Long parentId;
    private String parentName;
    private Boolean isActive;
    private LocalDateTime createdAt;
}