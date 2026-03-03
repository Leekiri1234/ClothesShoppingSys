package com.clothshop.admin.dtos.response.staff;

import com.clothshop.domain.enums.AccountStatus;
import com.clothshop.domain.enums.StaffRole;
import lombok.*;

import java.time.LocalDateTime;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class StaffResponse {
    private Long id;
    private String username;
    private String fullName;
    private String email;
    private String phone;
    private String avatar;
    private Long roleId;
    private StaffRole staffRole;
    private String roleName;
    private AccountStatus accountStatus;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
