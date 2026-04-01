package com.clothshop.admin.controllers;

import com.clothshop.admin.dtos.request.NotificationCreateRequest;
import com.clothshop.admin.dtos.request.NotificationUpdateRequest;
import com.clothshop.admin.dtos.response.NotificationResponse;
import com.clothshop.admin.services.AdminNotificationService;
import com.clothshop.domain.enums.NotificationType;
import com.clothshop.domain.repositories.auth.AccountRepository;
import com.clothshop.domain.enums.AccountType;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.PathVariable;
import com.clothshop.domain.entities.cms.Notification;

@Controller
@RequestMapping("/admin/notifications")
@RequiredArgsConstructor
public class AdminNotificationController {

    private final AdminNotificationService notificationService;
    private final AccountRepository accountRepository;

    @GetMapping
    public String listNotifications(Model model) {
        model.addAttribute("notifications", notificationService.getAllNotifications());
        return "admin/notifications/list";
    }

    @GetMapping("/create")
    public String showCreateForm(Model model) {
        model.addAttribute("request", new NotificationCreateRequest());
        model.addAttribute("types", NotificationType.values());
        model.addAttribute("customers", accountRepository.findAll().stream().filter(a -> a.getAccountType() == AccountType.CUSTOMER).toList());
        return "admin/notifications/form";
    }

    @PostMapping("/create")
    public String createNotification(@ModelAttribute NotificationCreateRequest request) {
        notificationService.createNotification(request);
        return "redirect:/admin/notifications?success=true";
    }

    @GetMapping("/edit/{id}")
    public String showEditForm(@PathVariable Long id, Model model) {
        NotificationResponse notification = notificationService.getNotificationById(id);

        // Tái sử dụng NotificationCreateRequest để form.html không bị lỗi binding các trường
        NotificationCreateRequest request = new NotificationCreateRequest();
        request.setId(notification.getId());
        request.setTitle(notification.getTitle());
        request.setMessage(notification.getContent());
        // Edit mode chỉ cho phép sửa Text, lock các trường phân phối

        model.addAttribute("request", request);
        model.addAttribute("isEdit", true); // Flag để UI biết là đang ở mode Edit
        return "admin/notifications/form"; // Trỏ về form.html thay vì edit.html
    }

    @PostMapping("/edit/{id}")
    public String updateNotification(@PathVariable Long id, @ModelAttribute NotificationUpdateRequest request) {
        notificationService.updateNotification(id, request);
        return "redirect:/admin/notifications?success=true";
    }

    @PostMapping("/delete/{id}")
    public String deleteNotification(@PathVariable Long id) {
        notificationService.deleteNotification(id);
        return "redirect:/admin/notifications?success=true";
    }
}
