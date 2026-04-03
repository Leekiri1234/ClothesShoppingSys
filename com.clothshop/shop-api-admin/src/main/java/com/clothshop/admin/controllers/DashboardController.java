package com.clothshop.admin.controllers;

import com.clothshop.admin.services.AdminExperienceService;
import com.clothshop.admin.services.ReportService;
import com.clothshop.admin.dtos.response.SalesReportResponse;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

/**
 * Dashboard Controller - Admin Panel Home.
 * Displays summary statistics and recent activity.
 */
@Controller
@RequestMapping("/admin")
@RequiredArgsConstructor
@Slf4j
public class DashboardController {

    private final ReportService reportService;

    private final AdminExperienceService adminExperienceService;

    /**
     * Display admin dashboard with summary statistics.
     * GET /admin/dashboard
     */
    @GetMapping("/dashboard")
    public String showDashboard(Model model, HttpServletRequest request) {
        log.info("Accessing admin dashboard");

        // Add current path for active menu highlighting
        model.addAttribute("currentPath", request.getRequestURI());

        model.addAttribute("dashboard", adminExperienceService.getDashboardData());
        SalesReportResponse todayReport = reportService.getTodayReport();

        model.addAttribute("dashboard", adminExperienceService.getDashboardData());
        model.addAttribute("todayRevenue", todayReport.getTotalRevenue());
        model.addAttribute("todayOrders", todayReport.getTotalOrders());
        model.addAttribute("totalCustomers", todayReport.getTotalCustomers());
        model.addAttribute("totalProducts", todayReport.getTotalProducts());

        return "admin/dashboard";
    }

    /**
     * Redirect root admin URL to dashboard.
     * GET /admin
     */
    @GetMapping("")
    public String redirectToDashboard() {
        return "redirect:/admin/dashboard";
    }
}
