package com.clothshop.admin.config;

import com.clothshop.domain.enums.OrderStatus;
import com.clothshop.domain.repositories.cms.NotificationRepository;
import com.clothshop.domain.repositories.order.OrderRepository;
import com.clothshop.domain.repositories.product.ProductFeedbackRepository;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ModelAttribute;

import java.time.LocalDateTime;

@ControllerAdvice(basePackages = "com.clothshop.admin.controllers")
@RequiredArgsConstructor
public class AdminGlobalModelAttributes {

    private final OrderRepository orderRepository;
    private final ProductFeedbackRepository productFeedbackRepository;
    private final NotificationRepository notificationRepository;

    @ModelAttribute("currentPath")
    public String currentPath(HttpServletRequest request) {
        return request.getRequestURI();
    }

    @ModelAttribute("adminNotificationCount")
    public long adminNotificationCount() {
        long pendingOrders = orderRepository.countByStatus(OrderStatus.PENDING);
        long pendingFeedback = productFeedbackRepository.countByFeedbackStatus("PENDING");
        long recentCampaigns = notificationRepository.countByDeliveredAtAfter(LocalDateTime.now().minusDays(1));
        return pendingOrders + pendingFeedback + recentCampaigns;
    }

    @ModelAttribute("adminNotificationSummary")
    public String adminNotificationSummary() {
        long pendingOrders = orderRepository.countByStatus(OrderStatus.PENDING);
        long pendingFeedback = productFeedbackRepository.countByFeedbackStatus("PENDING");
        long recentCampaigns = notificationRepository.countByDeliveredAtAfter(LocalDateTime.now().minusDays(1));
        return "Don cho xu ly: " + pendingOrders +
                " | Feedback cho duyet: " + pendingFeedback +
                " | Chien dich 24h: " + recentCampaigns;
    }
}
