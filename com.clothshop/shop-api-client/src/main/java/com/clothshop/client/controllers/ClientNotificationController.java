package com.clothshop.client.controllers;

import com.clothshop.client.services.ClientNotificationService;
import com.clothshop.domain.entities.auth.Account;
import com.clothshop.domain.repositories.auth.AccountRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.security.Principal;

@Controller
@RequiredArgsConstructor
public class ClientNotificationController {

    private final ClientNotificationService notificationService;
    private final AccountRepository accountRepository;

    // 1. Trang danh sách đầy đủ (Render Full Page)
    @GetMapping("/notifications")
    public String listAllNotifications(Principal principal,
                                       @RequestParam(defaultValue = "0") int page,
                                       Model model) {
        if (principal == null) return "redirect:/login";

        Account account = getAuthenticatedAccount(principal);
        if (account == null) return "redirect:/login";

        model.addAttribute("notifications", notificationService.getUserNotificationsPaged(account.getId(), PageRequest.of(page, 10)));
        model.addAttribute("currentPage", page);
        return "client/notifications/index";
    }

    // 2. API Fragment cho cái Chuông (AJAX call)
    @GetMapping("/api/notifications/latest")
    public String getLatestNotifications(Principal principal, Model model) {
        if (principal == null) return "client/fragments/notification-items :: unauthenticated";

        Account account = getAuthenticatedAccount(principal);
        if (account != null) {
            model.addAttribute("notifications", notificationService.getUserNotifications(account.getId()));
            model.addAttribute("unreadCount", notificationService.getUnreadCount(account.getId()));
            return "client/fragments/notification-items :: notification-list";
        }
        return "client/fragments/notification-items :: unauthenticated";
    }

    // 3. API Đánh dấu đã đọc (AJAX POST)
    @PostMapping("/api/notifications/mark-all-read")
    @ResponseBody
    public ResponseEntity<?> markAllAsRead(Principal principal) {
        Account account = getAuthenticatedAccount(principal);
        if (account != null) {
            notificationService.markAllAsRead(account.getId());
            return ResponseEntity.ok().build();
        }
        return ResponseEntity.status(401).build();
    }

    // Helper method để code sạch hơn (DRY)
    private Account getAuthenticatedAccount(Principal principal) {
        if (principal == null) return null;
        return accountRepository.findByUsernameAndIsActiveTrue(principal.getName()).orElse(null);
    }
}