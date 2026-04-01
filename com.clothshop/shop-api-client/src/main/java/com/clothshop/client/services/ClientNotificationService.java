package com.clothshop.client.services;

import com.clothshop.domain.entities.cms.Notification;
import com.clothshop.domain.enums.NotificationType;
import com.clothshop.domain.repositories.cms.NotificationRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class ClientNotificationService {

    private final NotificationRepository notificationRepository;

    @Cacheable(value = "systemNotifications", key = "'allSystem'")
    @Transactional(readOnly = true)
    public List<Notification> getSystemNotifications() {
        // Lấy tạm các thông báo hệ thống (như thay cho target_type = ALL vì không có Enum ALL)
        return notificationRepository.findByTargetTypeOrderByCreatedAtDesc(NotificationType.SYSTEM);
    }
}

