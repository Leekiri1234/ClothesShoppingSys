package com.clothshop.admin.dtos.request;

import com.clothshop.domain.enums.NotificationType;
import lombok.Data;

import java.util.List;

@Data
public class NotificationCreateRequest {
    private String title;
    private String message;
    private NotificationType type;
    private String targetType; // ALL or CUSTOM
    private List<Long> customerIds;
    private Long id; // for edit
}
