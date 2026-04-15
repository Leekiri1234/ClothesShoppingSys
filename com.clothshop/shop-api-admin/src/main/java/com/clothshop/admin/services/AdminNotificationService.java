package com.clothshop.admin.services;

import com.clothshop.domain.enums.NotificationType;
import com.clothshop.domain.models.auth.Account;
import com.clothshop.domain.models.cms.Notification;
import com.clothshop.domain.models.cms.NotificationRecipient;
import com.clothshop.domain.repositories.cms.NotificationRecipientRepository;
import com.clothshop.domain.repositories.cms.NotificationRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Slf4j
public class AdminNotificationService {

    private static final int MAX_TITLE_LENGTH = 200;

    private final NotificationRepository notificationRepository;
    private final NotificationRecipientRepository notificationRecipientRepository;

    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void sendUserNotification(Account account, String title, String content, NotificationType type) {
        sendUserNotification(account, title, content, type, null);
    }

    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void sendUserNotification(Account account, String title, String content, NotificationType type, String actionUrl) {
        if (account == null) {
            log.warn("Cannot send notification: Account is null. Title: {}", title);
            return;
        }

        // 1. Create and save the Notification core
        Notification notification = new Notification();
        notification.setTitle(limitLength(title, MAX_TITLE_LENGTH));
        notification.setContent(content != null ? content : "");
        notification.setType(type);
        notification.setActionUrl(actionUrl);
        notificationRepository.saveAndFlush(notification);

        // 2. Map Notification to the specific Account
        NotificationRecipient recipient = new NotificationRecipient();
        recipient.setNotification(notification);
        recipient.setAccount(account);
        recipient.setIsRead(false);
        notificationRecipientRepository.save(recipient);

        log.info("Sent automated notification to account ID: {} | Title: {}", account.getId(), title);
    }

    private String limitLength(String value, int maxLength) {
        if (value == null) {
            return "";
        }

        String trimmed = value.trim();
        if (trimmed.length() <= maxLength) {
            return trimmed;
        }

        return trimmed.substring(0, Math.max(0, maxLength - 1)).trim() + "…";
    }
}
