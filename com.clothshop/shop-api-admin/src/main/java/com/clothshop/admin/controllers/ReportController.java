package com.clothshop.admin.controllers;

import com.clothshop.admin.services.ReportService;
import com.clothshop.admin.dtos.response.SalesReportResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.Map;

@Controller
@RequestMapping("/admin/reports")
@RequiredArgsConstructor
@Slf4j
@PreAuthorize("hasAnyRole('SUPER_ADMIN', 'SALE_PRODUCT_STAFF')")
public class ReportController {
    
    private final ReportService reportService;
    
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
    
    /**
     * API endpoint for getting dashboard stats
     * Returns stats for today, weekly, monthly, and quarterly
     */
    @GetMapping("/api/dashboard-stats")
    @ResponseBody
    public ResponseEntity<Map<String, SalesReportResponse>> getDashboardStats() {
        Map<String, SalesReportResponse> stats = reportService.getDashboardStats();
        return ResponseEntity.ok(stats);
    }
    
    /**
     * API endpoint for getting sales report with date range
     */
    @GetMapping("/api/sales")
    @ResponseBody
    public ResponseEntity<SalesReportResponse> getSalesReportApi(
            @RequestParam(value = "startDate", required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
            @RequestParam(value = "endDate", required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate) {
        
        if (startDate == null || endDate == null) {
            startDate = LocalDate.now().minusDays(30);
            endDate = LocalDate.now();
        }
        
        SalesReportResponse report = reportService.getSalesReport(startDate, endDate);
        return ResponseEntity.ok(report);
    }
    
    /**
     * API endpoint for today's report
     */
    @GetMapping("/api/today")
    @ResponseBody
    public ResponseEntity<SalesReportResponse> getTodayReport() {
        return ResponseEntity.ok(reportService.getTodayReport());
    }
    
    /**
     * API endpoint for weekly report
     */
    @GetMapping("/api/weekly")
    @ResponseBody
    public ResponseEntity<SalesReportResponse> getWeeklyReport() {
        return ResponseEntity.ok(reportService.getWeeklyReport());
    }
    
    /**
     * API endpoint for monthly report
     */
    @GetMapping("/api/monthly")
    @ResponseBody
    public ResponseEntity<SalesReportResponse> getMonthlyReport() {
        return ResponseEntity.ok(reportService.getMonthlyReport());
    }
    
    /**
     * API endpoint for quarterly report
     */
    @GetMapping("/api/quarterly")
    @ResponseBody
    public ResponseEntity<SalesReportResponse> getQuarterlyReport() {
        return ResponseEntity.ok(reportService.getQuarterlyReport());
    }
}
