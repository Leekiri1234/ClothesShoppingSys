package com.clothshop.admin.controllers;

import lombok.extern.slf4j.Slf4j;
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
        log.info("Accessing admin login page");
        return "admin/login";
    }
}
