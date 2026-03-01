package com.clothshop.admin.dtos.response;

import lombok.*;

/**
 * Simple DTO for Category dropdown in forms.
 * Lightweight - only ID and Name.
 */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CategorySimpleResponse {
    private Long categoryId;
    private String categoryName;
}
