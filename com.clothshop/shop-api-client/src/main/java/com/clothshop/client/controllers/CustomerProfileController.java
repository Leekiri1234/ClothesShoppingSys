package com.clothshop.client.controllers;

import com.clothshop.client.dtos.request.ProfileUpdateRequest;
import com.clothshop.client.dtos.response.CustomerProfileResponse;
import com.clothshop.client.services.CustomerProfileService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.security.Principal;

@Controller
@RequestMapping("/profile")
@RequiredArgsConstructor
@PreAuthorize("hasRole('CUSTOMER')")
public class CustomerProfileController {

    private final CustomerProfileService profileService;

    /**
     * 1. Hiển thị trang Profile (Read-only)
     */
    @GetMapping
    public String viewProfile(Principal principal, Model model) {
        // Không cần try-catch, nếu lỗi (hết session/không tìm thấy user), Global Handler sẽ lo
        CustomerProfileResponse profile = profileService.getProfile(principal.getName());
        model.addAttribute("profile", profile);
        return "client/profile/view";
    }

    /**
     * 2. Hiển thị form chỉnh sửa
     */
    @GetMapping("/edit")
    public String showEditForm(Principal principal, Model model) {
        // Lấy profile hiện tại để hiển thị các trường readonly và điền sẵn form
        CustomerProfileResponse profile = profileService.getProfile(principal.getName());

        // Nếu model chưa có updateRequest (lần đầu vào trang hoặc không phải do redirect lỗi)
        if (!model.containsAttribute("updateRequest")) {
            ProfileUpdateRequest request = ProfileUpdateRequest.builder()
                    .fullName(profile.getFullName())
                    .phone(profile.getPhone())
                    .address(profile.getAddress())
                    .build();
            model.addAttribute("updateRequest", request);
        }

        model.addAttribute("profile", profile);
        return "client/profile/edit";
    }

    /**
     * 3. Xử lý submit form cập nhật
     * Bỏ hoàn toàn try-catch, để BusinessException tự bay lên Global Handler
     */
    @PostMapping("/update")
    public String updateProfile(
            Principal principal,
            @Valid @ModelAttribute("updateRequest") ProfileUpdateRequest request,
            BindingResult bindingResult,
            RedirectAttributes redirectAttributes,
            Model model) {

        // 1. Kiểm tra lỗi Validation (trống field, sai định dạng số điện thoại...)
        if (bindingResult.hasErrors()) {
            // Phải lấy lại thông tin profile để hiện username/email (readonly) trên form
            model.addAttribute("profile", profileService.getProfile(principal.getName()));
            return "client/profile/edit";
        }

        // 2. Gọi Service thực hiện cập nhật
        // Nếu có lỗi Business logic bên trong, nó sẽ văng Exception và GlobalHandler sẽ bắt.
        profileService.updateProfile(principal.getName(), request);

        // 3. Nếu thành công, thông báo qua FlashAttributes (chỉ tồn tại trong 1 lần redirect)
        redirectAttributes.addFlashAttribute("success", "Cập nhật hồ sơ thành công!");

        return "redirect:/profile";
    }
}