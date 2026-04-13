package com.clothshop.admin.dtos;

import com.clothshop.domain.enums.NotificationType;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class NotificationDTO {
    private String title;
    private String content;
    private NotificationType type; // Ví dụ: "ORDER", "PROMOTION", "SYSTEM"
}
