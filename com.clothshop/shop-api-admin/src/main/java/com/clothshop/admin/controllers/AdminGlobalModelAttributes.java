package com.clothshop.admin.controllers;

import jakarta.servlet.http.HttpServletRequest;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ModelAttribute;

/**
 * Provides common model attributes for all admin pages.
 */
@ControllerAdvice(basePackages = "com.clothshop.admin.controllers")
public class AdminGlobalModelAttributes {

    @ModelAttribute("currentPath")
    public String currentPath(HttpServletRequest request) {
        return request.getRequestURI();
    }
}

