package com.clothshop.admin.controllers;

import com.clothshop.admin.dtos.NotificationDTO;
import com.clothshop.domain.enums.NotificationType;
import com.clothshop.domain.models.cms.Notification;
import com.clothshop.domain.models.cms.NotificationRecipient;
import com.clothshop.domain.models.auth.Account;
import com.clothshop.domain.repositories.cms.NotificationRepository;
import com.clothshop.domain.repositories.cms.NotificationRecipientRepository;
import com.clothshop.domain.repositories.auth.AccountRepository;
import com.clothshop.domain.repositories.order.OrderRepository;
import com.clothshop.domain.repositories.order.RmaRequestRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import jakarta.annotation.PostConstruct;
import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/admin/notifications")
@RequiredArgsConstructor
@Slf4j
public class AdminNotificationController {

    private final NotificationRepository notificationRepository;
    private final NotificationRecipientRepository notificationRecipientRepository;
    private final AccountRepository accountRepository;
    private final OrderRepository orderRepository;
    private final RmaRequestRepository rmaRequestRepository;

    // Tự động fix đồng bộ dữ liệu cũ lúc test, không bỏ sot thông báo cũ trong NotificationRecipient
    @PostConstruct
    public void syncOldNotificationsToRecipients() {
        List<Notification> allNotifications = notificationRepository.findAll();
        List<Account> allAccounts = accountRepository.findAll();
        for (Notification noti : allNotifications) {

            // Xử lý thông báo đơn hàng (ORDER_UPDATE)
            if (noti.getType() == NotificationType.ORDER_UPDATE) {
                String title = noti.getTitle();
                if (title != null && title.startsWith("Cập nhật trạng thái đơn hàng: ")) {
                    String invoice = title.substring("Cập nhật trạng thái đơn hàng: ".length()).trim();
                    orderRepository.findByOrderInvoice(invoice).ifPresent(order -> {
                        if (order.getCustomer() != null && order.getCustomer().getAccount() != null) {
                            Account ownerAccount = order.getCustomer().getAccount();
                            // Chỉ tạo thông báo cho ĐÚNG owner
                            if (notificationRecipientRepository.findByNotificationIdAndAccountId(noti.getId(), ownerAccount.getId()).isEmpty()) {
                                NotificationRecipient recipient = new NotificationRecipient();
                                recipient.setNotification(noti);
                                recipient.setAccount(ownerAccount);
                                recipient.setIsRead(false);
                                notificationRecipientRepository.save(recipient);
                            }
                        }
                    });
                } else if (title != null && title.startsWith("Cập nhật yêu cầu trả hàng: ")) {
                    try {
                        Long rmaId = Long.parseLong(title.substring("Cập nhật yêu cầu trả hàng: ".length()).trim());
                        rmaRequestRepository.findById(rmaId).ifPresent(rma -> {
                            if (rma.getCustomer() != null && rma.getCustomer().getAccount() != null) {
                                Account ownerAccount = rma.getCustomer().getAccount();
                                if (notificationRecipientRepository.findByNotificationIdAndAccountId(noti.getId(), ownerAccount.getId()).isEmpty()) {
                                    NotificationRecipient recipient = new NotificationRecipient();
                                    recipient.setNotification(noti);
                                    recipient.setAccount(ownerAccount);
                                    recipient.setIsRead(false);
                                    notificationRecipientRepository.save(recipient);
                                }
                            }
                        });
                    } catch (NumberFormatException e) {
                        // ignore invalid ID format
                    }
                }
                continue; // Bỏ qua không đồng bộ cho mọi người
            }

            // Chỉ đồng bộ cho các thông báo hệ thống (SYSTEM, PROMOTION, vv) cho toàn bộ user
            if (noti.getType() != NotificationType.PROMOTION && noti.getType() != NotificationType.SYSTEM) {
                continue;
            }

            for (Account acc : allAccounts) {
                if (notificationRecipientRepository.findByNotificationIdAndAccountId(noti.getId(), acc.getId()).isEmpty()) {
                    NotificationRecipient recipient = new NotificationRecipient();
                    recipient.setNotification(noti);
                    recipient.setAccount(acc);
                    recipient.setIsRead(false);
                    notificationRecipientRepository.save(recipient);
                }
            }
        }
    }

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

        // 3. Tạo NotificationRecipient cho tất cả các tài khoản
        List<Account> allAccounts = accountRepository.findAll();
        List<NotificationRecipient> recipients = allAccounts.stream().map(account -> {
            NotificationRecipient recipient = new NotificationRecipient();
            recipient.setNotification(notification);
            recipient.setAccount(account);
            recipient.setIsRead(false);
            return recipient;
        }).collect(Collectors.toList());

        notificationRecipientRepository.saveAll(recipients);

        return ResponseEntity.ok("Thông báo đã được gửi và đồng bộ qua RAM!");
    }
}
