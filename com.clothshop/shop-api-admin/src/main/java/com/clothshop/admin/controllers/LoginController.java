package com.clothshop.admin.controllers;

import lombok.extern.slf4j.Slf4j;
import org.springframework.security.authentication.AnonymousAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
@Slf4j
public class LoginController {

    @GetMapping("/")
    public String redirectRoot() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();

        if (auth != null
                && auth.isAuthenticated()
                && !(auth instanceof AnonymousAuthenticationToken)) {
            log.info("User already authenticated, redirecting from / to admin dashboard");
            return "redirect:/admin/dashboard";
        }

        log.info("User not authenticated, redirecting from / to admin login");
        return "redirect:/admin/login";
    }

    @GetMapping("/login")
    public String redirectLogin() {
        log.info("Redirecting /login to /admin/login");
        return "redirect:/admin/login";
    }

    @GetMapping("/admin/login")
    public String showLoginPage() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();

        if (auth != null
                && auth.isAuthenticated()
                && !(auth instanceof AnonymousAuthenticationToken)) {
            log.info("User already authenticated, redirecting to admin dashboard");
            return "redirect:/admin/dashboard";
        }

        log.info("Accessing admin login page");
        return "admin/login";
    }
}