package com.clothshop.admin.controllers;

import com.clothshop.admin.services.ReportService;
import com.clothshop.admin.dtos.response.SalesReportResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;

@Controller
@RequestMapping("/admin/reports")
@RequiredArgsConstructor
@Slf4j
@PreAuthorize("hasAnyRole('SUPER_ADMIN', 'SALE_PRODUCT_STAFF')")
public class ReportController {
    
    private final ReportService reportService;
    
    /**
     * Redirect /admin/reports to /admin/reports/sales
     */
    @GetMapping("")
    public String redirectToSalesReport() {
        return "redirect:/admin/reports/sales";
    }
    
    /**
     * Display sales report page with date range filter
     */
    @GetMapping("/sales")
    public String showSalesReport(
            @RequestParam(value = "startDate", required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
            @RequestParam(value = "endDate", required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate,
            Model model) {
        
        if (startDate == null || endDate == null) {
            startDate = LocalDate.now().minusDays(30);
            endDate = LocalDate.now();
        }
        
        SalesReportResponse report = reportService.getSalesReport(startDate, endDate);
        
        model.addAttribute("report", report);
        model.addAttribute("startDate", startDate);
        model.addAttribute("endDate", endDate);
        model.addAttribute("title", "Báo cáo doanh thu");
        
        return "admin/orders/sales";
    }
    
}
