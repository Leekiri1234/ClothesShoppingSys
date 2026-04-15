package com.clothshop.client.services;

import com.clothshop.domain.models.cms.NotificationRecipient;
import com.clothshop.domain.repositories.cms.NotificationRecipientRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
public class NotificationService {

    private final NotificationRecipientRepository recipientRepository;

    // 1. Lấy 5 thông báo mới nhất cho Dropdown
    public List<NotificationRecipient> getTop5ForUser(Long accountId) {
        return recipientRepository.findAllByAccountIdWithFetch(accountId, PageRequest.of(0, 5));
    }

    // 2. Lấy toàn bộ danh sách cho trang List (Ví dụ lấy 50 cái gần nhất)
    public List<NotificationRecipient> getAllForUser(Long accountId) {
        return recipientRepository.findAllByAccountIdWithFetch(accountId, PageRequest.of(0, 50));
    }

    // 3. Xem chi tiết và đánh dấu đã đọc
    @Transactional
    public NotificationRecipient getDetail(Long notificationId, Long accountId) {
        NotificationRecipient recipient = recipientRepository
                .findByNotificationIdAndAccountId(notificationId, accountId)
                .orElseThrow(() -> new RuntimeException("Không tìm thấy thông báo hoặc bạn không có quyền xem"));

        if (!recipient.getIsRead()) {
            recipient.setIsRead(true);
            recipient.setReadAt(LocalDateTime.now());
        }
        return recipient;
    }

    @Transactional
    public void markAllAsRead(Long accountId) {
        recipientRepository.markAllAsRead(accountId);
    }

    public long countUnread(Long accountId) {
        return recipientRepository.countUnreadNotifications(accountId);
    }
}