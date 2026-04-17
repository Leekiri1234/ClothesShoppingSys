package com.clothshop.admin.controllers;

import lombok.extern.slf4j.Slf4j;
import org.springframework.security.authentication.AnonymousAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

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
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth != null && auth.isAuthenticated() && !(auth instanceof AnonymousAuthenticationToken)) {
            log.info("User already authenticated, redirecting to admin dashboard");
            return "redirect:/admin";
        }
        log.info("Accessing admin login page");
        return "admin/login";
    }
}
