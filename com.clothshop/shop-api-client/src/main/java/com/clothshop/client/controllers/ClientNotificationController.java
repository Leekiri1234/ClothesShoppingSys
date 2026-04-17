package com.clothshop.client.controllers;

import com.clothshop.client.services.NotificationService;
import com.clothshop.domain.models.auth.Account;
import com.clothshop.domain.models.cms.NotificationRecipient;
import com.clothshop.domain.repositories.auth.AccountRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.security.Principal;
import java.util.List;

@Controller
@RequestMapping("/profile/notifications")
@RequiredArgsConstructor
public class ClientNotificationController {

    private final NotificationService notificationService;
    private final AccountRepository accountRepository;

    // Hàm bổ trợ lấy AccountID từ Principal (Session)
    private Long getCurrentUserId(Principal principal) {
        if (principal == null) return null;
        // Tìm theo username từ CustomUserDetailsService
        return accountRepository.findByUsername(principal.getName())
                .map(Account::getId)
                .orElse(null);
    }

    // Endpoint trả về HTML Fragment cho Dropdown
    @GetMapping("/fragment")
    public String getNotificationFragment(Model model, Principal principal) {
        Long userId = getCurrentUserId(principal);
        if (userId == null) {
            model.addAttribute("notifications", null);
            return "fragments/notification-item :: notification-list";
        }

        // Lấy danh sách Recipient gắn với User này
        List<NotificationRecipient> list = notificationService.getTop5ForUser(userId);
        model.addAttribute("notifications", list);
        return "fragments/notification-item :: notification-list";
    }

    // Trang danh sách thông báo đầy đủ
    @GetMapping
    public String list(Model model, Principal principal) {
        Long currentUserId = getCurrentUserId(principal);
        if (currentUserId == null) return "redirect:/login";

        model.addAttribute("notifications", notificationService.getAllForUser(currentUserId));
        return "client/notifications/list";
    }

    // Trang chi tiết thông báo
    @GetMapping("/{id}")
    public String detail(@PathVariable Long id, Model model, Principal principal) {
        Long currentUserId = getCurrentUserId(principal);
        if (currentUserId == null) return "redirect:/login";

        try {
            NotificationRecipient detail = notificationService.getDetail(id, currentUserId);
            model.addAttribute("detail", detail);
            return "client/notifications/detail";
        } catch (Exception e) {
            return "error/404";
        }
    }

    @PostMapping("/mark-all-read")
    @ResponseBody
    public ResponseEntity<?> markAllRead(Principal principal) {
        Long currentUserId = getCurrentUserId(principal);
        if (currentUserId == null) return org.springframework.http.ResponseEntity.status(401).build();

        notificationService.markAllAsRead(currentUserId);
        return ResponseEntity.ok().build();
    }

    @GetMapping("/unread-count")
    @ResponseBody
    public ResponseEntity<?> getUnreadCount(Principal principal) {
        Long currentUserId = getCurrentUserId(principal);
        if (currentUserId == null) return org.springframework.http.ResponseEntity.ok(0);

        long count = notificationService.countUnread(currentUserId);
        return ResponseEntity.ok(count);
    }

    @GetMapping("/latest")
    @ResponseBody
    public ResponseEntity<?> getLatest(Principal principal) {
        Long currentUserId = getCurrentUserId(principal);
        if (currentUserId == null) return ResponseEntity.status(401).build();

        List<NotificationRecipient> top = notificationService.getTop5ForUser(currentUserId);
        if (top != null && !top.isEmpty()) {
            NotificationRecipient latest = top.get(0);
            return ResponseEntity.ok(java.util.Map.of(
                "newId", latest.getNotification().getId(),
                "title", latest.getNotification().getTitle(),
                "isRead", latest.getIsRead()
            ));
        }
        return ResponseEntity.ok(null);
    }
}
