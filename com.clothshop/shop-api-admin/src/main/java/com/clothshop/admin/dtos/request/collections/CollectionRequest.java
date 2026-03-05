package com.clothshop.admin.dtos.request.collections;

import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class CollectionRequest {
    @NotBlank(message = "Tên bộ sưu tập không được để trống")
    private String name;
    private String description;
    private Boolean active = true;
}
