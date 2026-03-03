package com.clothshop.admin.dtos.request.staff;

import com.clothshop.domain.enums.AccountStatus;
import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class StaffFilterRequest {
    private Long roleId;

    private AccountStatus status;

    private String keyword; // Tìm kiếm theo username, email, hoặc fullName
}
