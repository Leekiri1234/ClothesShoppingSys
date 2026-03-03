package com.clothshop.admin.dtos.request.staff;

import lombok.*;
import jakarta.validation.constraints.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class StaffUpdateRequest {

    @NotBlank(message = "Họ tên bắt buộc")
    private String fullName;

    @NotBlank(message = "Email bắt buộc")
    @Email(message = "Email không hợp lệ")
    private String email;

    @NotBlank(message = "Số điện thoại bắt buộc")
    @Pattern(regexp = "^(0|\\+84)(\\d{9})$", message = "Số điện thoại không hợp lệ")
    private String phone;

    private String avatar;

    @NotNull(message = "Vai trò bắt buộc")
    private Long roleId;
}
