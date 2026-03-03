package com.clothshop.admin.dtos.request.staff;

import jakarta.validation.constraints.*;
import lombok.*;

@Getter @Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class StaffCreateRequest {

    @NotBlank(message = "Username bắt buộc")
    @Size(min = 5, max = 50, message = "Username từ 5 đến 50 ký tự")
    private String username;

    @NotBlank(message = "Password bắt buộc")
    @Size(min = 8, max = 100, message = "Password từ 8 đến 100 ký tự")
    private String password;

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

    @Builder.Default
    private Boolean isActive = true;
}
