package com.clothshop.admin.dtos.request;

import lombok.Data;

@Data
public class NotificationUpdateRequest {
    private Long id;
    private String title;
    private String message;
}

