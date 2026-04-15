package com.clothshop.admin.controllers;

import com.clothshop.admin.dtos.NotificationDTO;
import com.clothshop.domain.models.cms.Notification;
import com.clothshop.domain.repositories.cms.NotificationRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/admin/notifications")
@RequiredArgsConstructor
public class AdminNotificationController {

    private final NotificationRepository notificationRepository;

    @PostMapping("/send")
    public ResponseEntity<?> sendNotification(@RequestBody NotificationDTO dto) {
        // 1. Chuyển đổi từ DTO sang Entity
        Notification notification = new Notification();
        notification.setTitle(dto.getTitle());
        notification.setContent(dto.getContent());
        notification.setType(dto.getType());

        // 2. Lưu vào Database
        // Khi save() thành công, NotificationPropagationListener sẽ tự động
        // lấy ID và ghi vào NativeMemoryBridge (RAM) nhờ @PostPersist
        notificationRepository.save(notification);

        return ResponseEntity.ok("Thông báo đã được gửi và đồng bộ qua RAM!");
    }
}
