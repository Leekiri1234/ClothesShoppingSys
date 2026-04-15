package com.clothshop.client.controllers;

import com.clothshop.client.services.NotificationService;
import com.clothshop.domain.models.auth.Account;
import com.clothshop.domain.models.cms.NotificationRecipient;
import com.clothshop.domain.repositories.auth.AccountRepository;
import lombok.RequiredArgsConstructor;
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
        return accountRepository.findByUsername(principal.getName())
                .map(Account::getId)
                .orElse(null);
    }

    // Endpoint trả về HTML Fragment cho Dropdown
    @GetMapping("/fragment")
    public String getNotificationFragment(Model model, Principal principal) {
        Long currentUserId = getCurrentUserId(principal);
        if (currentUserId == null) return "fragments/notification-items :: empty-list";

        List<NotificationRecipient> recipients = notificationService.getTop5ForUser(currentUserId);
        model.addAttribute("notifications", recipients);

        return "fragments/notification-items :: notification-list";
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
}