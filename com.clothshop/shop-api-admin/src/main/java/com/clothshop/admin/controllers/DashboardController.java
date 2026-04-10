package com.clothshop.admin.controllers;

import com.clothshop.admin.dtos.response.SalesReportResponse;
import com.clothshop.admin.dtos.response.dashboard.RevenueDTO;
import com.clothshop.admin.dtos.response.dashboard.TopProductDTO;
import com.clothshop.admin.services.ReportService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

import java.util.Collections;
import java.util.List;

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

    /**
     * Display admin dashboard with summary statistics.
     * GET /admin/dashboard
     */
    @GetMapping("/dashboard")
    public String showDashboard(Model model) {
        log.info("Accessing admin dashboard");


        // Get today's sales report for real data
        SalesReportResponse todayReport = reportService.getTodayReport();
        model.addAttribute("todayReport", todayReport);
        model.addAttribute("todayRevenue", todayReport.getTotalRevenue());
        model.addAttribute("todayOrders", todayReport.getTotalOrders());
        model.addAttribute("totalCustomers", todayReport.getTotalCustomers());
        model.addAttribute("totalProducts", todayReport.getTotalProducts());

        List<RevenueDTO> revenue7Days = reportService.getRevenueLast7Days();
        log.info("Revenue 7 days: {}", revenue7Days);
        model.addAttribute("revenue7Days", revenue7Days != null ? revenue7Days : Collections.emptyList());
        model.addAttribute("topProducts", reportService.getTopSellingProducts());

        model.addAttribute("recentOrders", reportService.getRecentOrders(9));

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
