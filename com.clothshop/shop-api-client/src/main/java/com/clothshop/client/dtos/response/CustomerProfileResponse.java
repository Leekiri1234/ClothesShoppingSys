package com.clothshop.client.dtos.response;

import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CustomerProfileResponse {
    private String username;
    private String email;
    private String fullName;
    private String phone;
    private String address;
}
