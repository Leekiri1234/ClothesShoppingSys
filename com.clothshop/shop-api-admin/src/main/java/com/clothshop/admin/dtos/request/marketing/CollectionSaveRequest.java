package com.clothshop.admin.dtos.request.marketing;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CollectionSaveRequest {

    private Long id; // Có giá trị -> Update, Null -> Create

    @NotBlank(message = "Tên bộ sưu tập không được để trống")
    @Size(max = 100, message = "Tên không vượt quá 100 ký tự")
    private String name;

    @Size(max = 500, message = "Mô tả không vượt quá 500 ký tự")
    private String description;

    private Boolean isActive;
}