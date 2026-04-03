package com.clothshop.admin.controllers;

import com.clothshop.admin.services.AdminExperienceService;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;
import lombok.RequiredArgsConstructor;

import java.time.LocalDateTime;

@Controller
@RequiredArgsConstructor
public class AdminExperienceController {

        private final AdminExperienceService adminExperienceService;

    @GetMapping("/admin/marketing/banners")
    @PreAuthorize("hasAnyRole('SUPER_ADMIN', 'MARKETING_STAFF')")
    public String banners(Model model) {
                model.addAttribute("banners", adminExperienceService.getBanners());
                                model.addAttribute("bannerImageOptions", adminExperienceService.getBannerImageOptions());
        return "admin/marketing/banners";
    }

        @GetMapping("/admin/marketing/banners/{id}/edit")
        @PreAuthorize("hasAnyRole('SUPER_ADMIN', 'MARKETING_STAFF')")
        public String editBannerPage(@PathVariable("id") Long id, Model model) {
                model.addAttribute("banner", adminExperienceService.getBannerForEdit(id));
                model.addAttribute("bannerImageOptions", adminExperienceService.getBannerImageOptions());
                return "admin/marketing/banner-form";
        }

        @PostMapping("/admin/marketing/banners/create")
        @PreAuthorize("hasAnyRole('SUPER_ADMIN', 'MARKETING_STAFF')")
        public String createBanner(RedirectAttributes redirectAttributes) {
                adminExperienceService.createDefaultBanner();
                redirectAttributes.addFlashAttribute("success", "Da tao banner moi.");
                return "redirect:/admin/marketing/banners";
        }

        @PostMapping("/admin/marketing/banners/{id}/edit")
        @PreAuthorize("hasAnyRole('SUPER_ADMIN', 'MARKETING_STAFF')")
        public String editBanner(@PathVariable("id") Long id,
                                                         @RequestParam("title") String title,
                                                         @RequestParam("image") String image,
                                                         @RequestParam("link") String link,
                                                         @RequestParam("order") Integer order,
                                                         @RequestParam("status") String status,
                                                         RedirectAttributes redirectAttributes) {
                adminExperienceService.updateBanner(id, title, image, link, order, status);
                redirectAttributes.addFlashAttribute("success", "Da cap nhat banner thanh cong.");
                return "redirect:/admin/marketing/banners";
        }

        @PostMapping("/admin/marketing/banners/{id}/toggle")
        @PreAuthorize("hasAnyRole('SUPER_ADMIN', 'MARKETING_STAFF')")
        public String toggleBanner(@PathVariable("id") Long id, RedirectAttributes redirectAttributes) {
                adminExperienceService.toggleBannerStatus(id);
                redirectAttributes.addFlashAttribute("success", "Da cap nhat trang thai banner.");
                return "redirect:/admin/marketing/banners";
        }

    @GetMapping("/admin/marketing/notifications")
    @PreAuthorize("hasAnyRole('SUPER_ADMIN', 'MARKETING_STAFF')")
    public String notifications(Model model) {
                model.addAttribute("history", adminExperienceService.getNotificationHistory());
        return "admin/marketing/notifications";
    }

        @PostMapping("/admin/marketing/notifications/send")
        @PreAuthorize("hasAnyRole('SUPER_ADMIN', 'MARKETING_STAFF')")
        public String sendNotificationCampaign(@RequestParam("title") String title,
                                                                                   @RequestParam("content") String content,
                                                                                   @RequestParam("target") String target,
                                                                                   @RequestParam(value = "scheduledAt", required = false)
                                                                                   @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime scheduledAt,
                                                                                   RedirectAttributes redirectAttributes) {
                adminExperienceService.sendNotificationCampaign(title, content, target, scheduledAt);
                redirectAttributes.addFlashAttribute("success", "Da tao va gui chien dich thong bao.");
                return "redirect:/admin/marketing/notifications";
        }

    @GetMapping("/admin/products/inventory")
    @PreAuthorize("hasAnyRole('SUPER_ADMIN', 'SALE_PRODUCT_STAFF')")
    public String inventory(Model model) {
                model.addAllAttributes(adminExperienceService.getInventoryOverview());
        return "admin/products/inventory";
    }

    @GetMapping("/admin/reports/revenue")
    @PreAuthorize("hasAnyRole('SUPER_ADMIN', 'MARKETING_STAFF', 'SALE_PRODUCT_STAFF')")
    public String revenue(Model model) {
                model.addAttribute("monthly", adminExperienceService.getRevenueMonthlyReport());
        return "admin/reports/revenue";
    }

    @GetMapping("/admin/customers")
    @PreAuthorize("hasAnyRole('SUPER_ADMIN', 'CUSTOMER_SERVICE', 'MARKETING_STAFF')")
    public String customers(Model model) {
                model.addAttribute("customers", adminExperienceService.getCustomersOverview());
        return "admin/customers/list";
    }

        @GetMapping("/admin/customers/profile")
        @PreAuthorize("hasAnyRole('SUPER_ADMIN', 'CUSTOMER_SERVICE', 'MARKETING_STAFF')")
        public String customerProfile(@RequestParam("email") String email,
                                                                  Model model,
                                                                  RedirectAttributes redirectAttributes) {
                try {
                        model.addAttribute("customer", adminExperienceService.getCustomerProfileByEmail(email));
                        return "admin/customers/profile";
                } catch (Exception ex) {
                        redirectAttributes.addFlashAttribute("error", ex.getMessage());
                        return "redirect:/admin/customers";
                }
        }

    @GetMapping("/admin/customers/feedback")
    @PreAuthorize("hasAnyRole('SUPER_ADMIN', 'CUSTOMER_SERVICE', 'MARKETING_STAFF')")
    public String feedback(Model model) {
                model.addAttribute("reviews", adminExperienceService.getFeedbackReviews());
        return "admin/customers/feedback";
    }

        @PostMapping("/admin/customers/feedback/seed")
        @PreAuthorize("hasAnyRole('SUPER_ADMIN', 'CUSTOMER_SERVICE', 'MARKETING_STAFF')")
        public String seedFeedbackData(RedirectAttributes redirectAttributes) {
                int created = adminExperienceService.seedFeedbackSamples();
                if (created == 0) {
                        redirectAttributes.addFlashAttribute("success", "Da co du du lieu feedback de test.");
                } else {
                        redirectAttributes.addFlashAttribute("success", "Da seed " + created + " feedback mau de test approve/hide.");
                }
                return "redirect:/admin/customers/feedback";
        }

        @PostMapping("/admin/customers/feedback/{id}/approve")
        @PreAuthorize("hasAnyRole('SUPER_ADMIN', 'CUSTOMER_SERVICE', 'MARKETING_STAFF')")
        public String approveFeedback(@PathVariable("id") Long id,
                                                                  @RequestParam(value = "moderatedBy", defaultValue = "ADMIN") String moderatedBy,
                                                                  RedirectAttributes redirectAttributes) {
                adminExperienceService.moderateFeedback(id, "APPROVE", moderatedBy);
                redirectAttributes.addFlashAttribute("success", "Da duyet feedback thanh cong.");
                return "redirect:/admin/customers/feedback";
        }

        @PostMapping("/admin/customers/feedback/{id}/reject")
        @PreAuthorize("hasAnyRole('SUPER_ADMIN', 'CUSTOMER_SERVICE', 'MARKETING_STAFF')")
        public String rejectFeedback(@PathVariable("id") Long id,
                                                                 @RequestParam(value = "moderatedBy", defaultValue = "ADMIN") String moderatedBy,
                                                                 RedirectAttributes redirectAttributes) {
                adminExperienceService.moderateFeedback(id, "REJECT", moderatedBy);
                redirectAttributes.addFlashAttribute("success", "Da tu choi feedback.");
                return "redirect:/admin/customers/feedback";
        }

        @PostMapping("/admin/customers/feedback/{id}/hide")
        @PreAuthorize("hasAnyRole('SUPER_ADMIN', 'CUSTOMER_SERVICE', 'MARKETING_STAFF')")
        public String hideFeedback(@PathVariable("id") Long id,
                                                           @RequestParam(value = "moderatedBy", defaultValue = "ADMIN") String moderatedBy,
                                                           RedirectAttributes redirectAttributes) {
                adminExperienceService.moderateFeedback(id, "HIDE", moderatedBy);
                redirectAttributes.addFlashAttribute("success", "Da an feedback tren giao dien.");
                return "redirect:/admin/customers/feedback";
        }
}
