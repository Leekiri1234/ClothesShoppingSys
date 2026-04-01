package com.clothshop.client.controllers;

import com.clothshop.client.services.ClientNotificationService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

@Controller
@RequestMapping("/notifications")
@RequiredArgsConstructor
public class ClientNotificationController {

    private final ClientNotificationService notificationService;

    @GetMapping
    public String listNotifications(Model model) {
        // Tạm thời hiển thị các thông báo từ cache
        model.addAttribute("notifications", notificationService.getSystemNotifications());
        return "client/notifications/list";
    }
}

