package com.clothshop.admin.services;

import com.clothshop.admin.dtos.request.NotificationCreateRequest;
import com.clothshop.admin.dtos.request.NotificationUpdateRequest;
import com.clothshop.admin.dtos.response.NotificationResponse;
import com.clothshop.domain.entities.auth.Account;
import com.clothshop.domain.entities.cms.Notification;
import com.clothshop.domain.entities.cms.NotificationRecipient;
import com.clothshop.domain.enums.AccountType;
import com.clothshop.domain.repositories.auth.AccountRepository;
import com.clothshop.domain.repositories.cms.NotificationRecipientRepository;
import com.clothshop.domain.repositories.cms.NotificationRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.cache.annotation.CacheEvict;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class AdminNotificationService {

    private final NotificationRepository notificationRepository;
    private final NotificationRecipientRepository notificationRecipientRepository;
    private final AccountRepository accountRepository;

    public NotificationResponse getNotificationById(Long id) {
        Notification notif = notificationRepository.findById(id).orElse(null);
        if (notif == null) return null;
        NotificationResponse res = new NotificationResponse();
        res.setId(notif.getId());
        res.setTitle(notif.getTitle());
        res.setContent(notif.getContent());
        res.setTargetType(notif.getTargetType());
        res.setCreatedAt(notif.getCreatedAt());
        return res;
    }

    @Transactional
    @CacheEvict(value = "systemNotifications", allEntries = true)
    public void deleteNotification(Long id) {
        notificationRepository.deleteById(id);
    }

    @Transactional
    @CacheEvict(value = "systemNotifications", allEntries = true)
    public void updateNotification(Long id, NotificationUpdateRequest request) {
        Notification notification = notificationRepository.findById(id).orElseThrow();
        notification.setTitle(request.getTitle());
        notification.setContent(request.getMessage());
        notificationRepository.save(notification);
    }

    @Transactional
    @CacheEvict(value = "systemNotifications", allEntries = true)
    public void createNotification(NotificationCreateRequest request) {
        // 1. Tạo Notification
        Notification notification = new Notification();
        notification.setTitle(request.getTitle());
        notification.setContent(request.getMessage());
        notification.setTargetType(request.getType());
        notification.setScheduledAt(LocalDateTime.now());
        notification.setDeliveredAt(LocalDateTime.now());

        notification = notificationRepository.save(notification);

        // 2. Xử lý Recipient
        List<Account> targetAccounts;
        if ("ALL".equalsIgnoreCase(request.getTargetType())) {
            // Lấy tất cả user (hoặc phân trang tùy hệ thống, prototype lấy hết)
            targetAccounts = accountRepository.findAll().stream()
                .filter(a -> a.getAccountType() == AccountType.CUSTOMER)
                .collect(Collectors.toList());
        } else {
            // "CUSTOM" - list customerIds
            if (request.getCustomerIds() != null && !request.getCustomerIds().isEmpty()) {
                targetAccounts = accountRepository.findAllById(request.getCustomerIds());
            } else {
                targetAccounts = List.of();
            }
        }

        if (!targetAccounts.isEmpty()) {
            Notification finalNotification = notification;
            List<NotificationRecipient> recipients = targetAccounts.stream().map(acc -> {
                NotificationRecipient r = new NotificationRecipient();
                r.setNotification(finalNotification);
                r.setAccount(acc);
                r.setIsRead(false);
                return r;
            }).collect(Collectors.toList());
            notificationRecipientRepository.saveAll(recipients);
        }
    }

    @Transactional(readOnly = true)
    public List<NotificationResponse> getAllNotifications() {
        return notificationRepository.findAll().stream().map(notif -> {
            NotificationResponse res = new NotificationResponse();
            res.setId(notif.getId());
            res.setTitle(notif.getTitle());
            res.setContent(notif.getContent());
            res.setTargetType(notif.getTargetType());
            res.setCreatedAt(notif.getCreatedAt());
            return res;
        }).collect(Collectors.toList());
    }
}
