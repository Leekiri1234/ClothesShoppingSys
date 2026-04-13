package com.clothshop.admin.dtos.response.customer;

import com.clothshop.domain.enums.AccountStatus;
import lombok.*;

import java.time.LocalDateTime;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CustomerAdminResponse {

    private Long id;
    private String fullName;
    private String email;
    private String phoneNumber;
    private String username;

    private AccountStatus accountStatus;

    private LocalDateTime createdAt;

    private boolean isActive;
}