package com.clothshop.admin.controllers;

// --- Spring Web & MVC ---
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

// --- Spring Data ---
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;

// --- Validation ---
import jakarta.validation.Valid;

// --- Lombok ---
import lombok.RequiredArgsConstructor;

// --- DTOs ---
import com.clothshop.admin.dtos.request.staff.StaffCreateRequest;
import com.clothshop.admin.dtos.request.staff.StaffUpdateRequest;
import com.clothshop.admin.dtos.request.staff.StaffFilterRequest;
import com.clothshop.admin.dtos.response.staff.StaffResponse;
import com.clothshop.common.dtos.response.PageResponse;

// --- Services & Repositories ---
import com.clothshop.admin.services.StaffManagementService;
import com.clothshop.domain.repositories.auth.RoleRepository;
import com.clothshop.domain.enums.AccountStatus;

// --- Exceptions ---
import com.clothshop.common.exceptions.BusinessException;

import java.util.Arrays;

@Controller
@RequestMapping("/admin/staff")
@RequiredArgsConstructor
@PreAuthorize("hasRole('SUPER_ADMIN')")
public class StaffManagementController {

    private final StaffManagementService staffService;
    private final RoleRepository roleRepository;

    // 1. TRANG DANH SÁCH (Hỗ trợ tìm kiếm & Phân trang)
    @GetMapping
    public String listStaff(StaffFilterRequest filter,
                            @PageableDefault(size = 10, sort = "id") Pageable pageable,
                            Model model) {

        if (filter.getStatus() == null && filter.getKeyword() == null && filter.getRoleId() == null) {
            filter.setStatus(AccountStatus.ACTIVE);
        }

        PageResponse<StaffResponse> response = staffService.getAllStaff(filter, pageable);

        model.addAttribute("staffPage", response);
        model.addAttribute("roles", roleRepository.findAll());
        model.addAttribute("allStatus", AccountStatus.values());
        model.addAttribute("filter", filter);

        return "admin/staff/list";
    }

    // 2. TRANG TẠO MỚI (Hiển thị Form)
    @GetMapping("/create")
    public String showCreateForm(Model model) {
        if (!model.containsAttribute("staffRequest")) {
            model.addAttribute("staffRequest", new StaffCreateRequest());
        }
        model.addAttribute("roles", roleRepository.findAll());
        return "admin/staff/create";
    }

    // 3. XỬ LÝ LƯU (POST)
    @PostMapping("/create")
    public String createStaff(@Valid @ModelAttribute("staffRequest") StaffCreateRequest request,
                              BindingResult result,
                              RedirectAttributes redirectAttributes,
                              Model model) {
        if (result.hasErrors()) {
            model.addAttribute("roles", roleRepository.findAll());
            return "admin/staff/create";
        }

        try {
            staffService.createStaff(request);
            redirectAttributes.addFlashAttribute("success", "Thêm mới nhân viên thành công!");
            return "redirect:/admin/staff";
        } catch (BusinessException e) {
            model.addAttribute("error", e.getMessage());
            model.addAttribute("roles", roleRepository.findAll());
            return "admin/staff/create";
        }
    }

    // 4. TRANG CHỈNH SỬA
    @GetMapping("/edit/{id}")
    public String showEditForm(@PathVariable Long id, Model model) {
        StaffResponse staff = staffService.getStaffById(id);

        // Đưa dữ liệu vào form (Lưu ý: Thymeleaf sẽ map các field tương ứng)
        model.addAttribute("staffRequest", staff);
        model.addAttribute("roles", roleRepository.findAll());
        return "admin/staff/edit";
    }

    // 5. XỬ LÝ CẬP NHẬT (POST)
    @PostMapping("/edit/{id}")
    public String updateStaff(@PathVariable Long id,
                              @Valid @ModelAttribute("staffRequest") StaffUpdateRequest request,
                              BindingResult result,
                              RedirectAttributes redirectAttributes,
                              Model model) {
        if (result.hasErrors()) {
            model.addAttribute("roles", roleRepository.findAll());
            return "admin/staff/edit";
        }

        try {
            staffService.updateStaff(id, request);
            redirectAttributes.addFlashAttribute("success", "Cập nhật nhân viên thành công!");
            return "redirect:/admin/staff";
        } catch (BusinessException e) {
            model.addAttribute("error", e.getMessage());
            model.addAttribute("roles", roleRepository.findAll());
            return "admin/staff/edit";
        }
    }

    // 6. THAY ĐỔI TRẠNG THÁI (KHÓA/MỞ KHÓA)
    @PostMapping("/toggle/{id}")
    public String toggleStaffStatus(@PathVariable Long id, RedirectAttributes redirectAttributes) {
        try {
            staffService.toggleStaffStatus(id);
            redirectAttributes.addFlashAttribute("success", "Cập nhật trạng thái thành công!");
        } catch (BusinessException e) {
            redirectAttributes.addFlashAttribute("error", e.getMessage());
        }
        return "redirect:/admin/staff";
    }
}