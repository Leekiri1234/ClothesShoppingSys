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

    /**
     * Display minimal login variant.
     * GET /admin/login/minimal
     */
    @GetMapping("/admin/login/minimal")
    public String showMinimalLoginPage() {
        log.info("Accessing minimal admin login page");
        return "admin/login-minimal";
    }
}
