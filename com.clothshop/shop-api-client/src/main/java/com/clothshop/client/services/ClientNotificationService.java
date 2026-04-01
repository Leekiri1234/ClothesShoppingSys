package com.clothshop.client.services;

import com.clothshop.domain.entities.cms.NotificationRecipient;
import com.clothshop.domain.repositories.cms.NotificationRecipientRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class ClientNotificationService {

    private final NotificationRecipientRepository recipientRepository;

    @Cacheable(value = "userNotifications", key = "#accountId")
    @Transactional(readOnly = true)
    public List<NotificationRecipient> getUserNotifications(Long accountId) {
        return recipientRepository.findByAccountIdOrderByCreatedAtDesc(accountId);
    }

    @Transactional(readOnly = true)
    public Page<NotificationRecipient> getUserNotificationsPaged(Long accountId, Pageable pageable) {
        return recipientRepository.findByAccountIdOrderByCreatedAtDesc(accountId, pageable);
    }

    @Transactional(readOnly = true)
    public long getUnreadCount(Long accountId) {
        return recipientRepository.countByAccountIdAndIsReadFalse(accountId);
    }

    @Transactional
    @CacheEvict(value = "userNotifications", key = "#accountId")
    public void markAllAsRead(Long accountId) {
        List<NotificationRecipient> unread = recipientRepository.findByAccountIdAndIsReadFalse(accountId);
        if (!unread.isEmpty()) {
            unread.forEach(r -> r.setIsRead(true));
            recipientRepository.saveAll(unread);
        }
    }
}