package com.clothshop.admin.controllers;

import com.clothshop.admin.dtos.response.customer.CustomerAdminResponse;
import com.clothshop.admin.services.CustomerManagementService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Controller;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.ui.Model;
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

        Page<CustomerAdminResponse> customerPage = customerService.getAllCustomers(
                PageRequest.of(page, size), keyword, status, createdFrom, createdTo);

        model.addAttribute("customers", customerPage);
        model.addAttribute("currentPage", page);
        model.addAttribute("keyword", keyword);
        model.addAttribute("status", status);
        model.addAttribute("createdFrom", createdFrom);
        model.addAttribute("createdTo", createdTo);
        return "admin/customer/list"; // Khớp với thư mục template hiện tại
    }

    /**
     * Khóa hoặc Mở khóa tài khoản khách hàng
     */
    @PostMapping("/{id}/toggle-status")
    public String toggleStatus(@PathVariable Long id, RedirectAttributes ra) {
        try {
            customerService.toggleStatus(id);
            ra.addFlashAttribute("success", "Cập nhật trạng thái khách hàng thành công!");
        } catch (Exception e) {
            ra.addFlashAttribute("error", "Có lỗi xảy ra: " + e.getMessage());
        }
        return "redirect:/admin/customers";
    }
}