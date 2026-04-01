package com.clothshop.admin.dtos.response;

import com.clothshop.domain.enums.NotificationType;
import lombok.Data;

import java.time.LocalDateTime;

@Data
public class NotificationResponse {
    private Long id;
    private String title;
    private String content;
    private NotificationType targetType;
    private LocalDateTime createdAt;
}

