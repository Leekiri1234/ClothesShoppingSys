package com.clothshop.admin.controllers;

import com.clothshop.admin.dtos.NotificationDTO;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

@Controller
@RequestMapping("/admin/notifications")
public class AdminNotificationMvcController {

    @GetMapping("/send-manual")
    public String showSendForm(Model model) {
        model.addAttribute("noti", new NotificationDTO());
        return "admin/notifications/send-form"; // Đường dẫn tới file HTML
    }
}
