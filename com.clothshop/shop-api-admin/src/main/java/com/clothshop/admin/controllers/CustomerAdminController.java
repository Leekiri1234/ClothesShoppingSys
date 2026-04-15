package com.clothshop.admin.controllers;

import com.clothshop.admin.dtos.response.customer.CustomerAdminResponse;
import com.clothshop.admin.services.CustomerManagementService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.time.LocalDate;

@Controller
@RequestMapping("/admin/customers")
@RequiredArgsConstructor
public class CustomerAdminController {

    private final CustomerManagementService customerService;

    /**
     * Hiển thị danh sách khách hàng
     */
    @GetMapping
    public String listCustomers(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(required = false) String keyword,
            @RequestParam(defaultValue = "ALL") String status,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate createdFrom,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate createdTo,
            Model model) {

        int safePage = Math.max(page, 0);
        int safeSize = size > 0 ? size : 10;

        Page<CustomerAdminResponse> customerPage = customerService.getAllCustomers(
                PageRequest.of(safePage, safeSize, Sort.by(Sort.Direction.DESC, "createdAt")),
                keyword,
                status,
                createdFrom,
                createdTo
        );

        model.addAttribute("customers", customerPage);
        model.addAttribute("currentPage", safePage);
        model.addAttribute("size", safeSize);
        model.addAttribute("keyword", keyword);
        model.addAttribute("status", status);
        model.addAttribute("createdFrom", createdFrom);
        model.addAttribute("createdTo", createdTo);
        return "admin/customer/list"; // Đường dẫn đến file HTML
    }

    /**
     * Khóa hoặc Mở khóa tài khoản khách hàng
     */
    @PostMapping("/{id}/toggle-status")
    public String toggleStatus(@PathVariable Long id,
                               @RequestParam(defaultValue = "0") int page,
                               @RequestParam(defaultValue = "10") int size,
                               @RequestParam(required = false) String keyword,
                               @RequestParam(defaultValue = "ALL") String status,
                               @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate createdFrom,
                               @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate createdTo,
                               RedirectAttributes ra) {
        try {
            customerService.toggleStatus(id);
            ra.addFlashAttribute("success", "Cập nhật trạng thái khách hàng thành công!");
        } catch (Exception e) {
            ra.addFlashAttribute("error", "Có lỗi xảy ra: " + e.getMessage());
        }

        ra.addAttribute("page", Math.max(page, 0));
        ra.addAttribute("size", size > 0 ? size : 10);
        if (keyword != null && !keyword.trim().isEmpty()) {
            ra.addAttribute("keyword", keyword.trim());
        }
        if (status != null && !status.trim().isEmpty()) {
            ra.addAttribute("status", status.trim());
        }
        if (createdFrom != null) {
            ra.addAttribute("createdFrom", createdFrom);
        }
        if (createdTo != null) {
            ra.addAttribute("createdTo", createdTo);
        }

        return "redirect:/admin/customers";
    }
}