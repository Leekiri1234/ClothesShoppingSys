package com.clothshop.admin.controllers;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

/**
 * Login Controller - Handles admin authentication pages.
 * Form-based authentication managed by Spring Security.
 */
@Controller
@Slf4j
public class LoginController {

    /**
     * Display admin login page.
     * GET /admin/login
     */
    @GetMapping("/admin/login")
    public String showLoginPage() {
        log.info("Accessing admin login page");
        return "admin/login";
    }

    /**
     * Display admin forgot password page.
     */
    @GetMapping("/admin/forgot-password")
    public String showForgotPasswordPage() {
        return "admin/forgot-password";
    }

    /**
     * Process admin forgot password request.
     */
    @PostMapping("/admin/forgot-password")
    public String processForgotPassword(String email, RedirectAttributes redirectAttributes) {
        log.info("Admin forgot password requested for email: {}", email);
        redirectAttributes.addFlashAttribute("message",
                "Neu email ton tai, huong dan dat lai mat khau da duoc gui.");
        return "redirect:/admin/forgot-password";
    }
}
