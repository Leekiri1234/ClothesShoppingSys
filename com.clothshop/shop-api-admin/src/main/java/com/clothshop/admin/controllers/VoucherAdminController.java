package com.clothshop.admin.controllers;

import com.clothshop.admin.dtos.request.marketing.VoucherCreateRequest;
import com.clothshop.admin.dtos.request.marketing.VoucherUpdateRequest;
import com.clothshop.admin.dtos.response.marketing.VoucherResponse;
import com.clothshop.admin.services.VoucherService;
import com.clothshop.common.exceptions.BusinessException;
import com.clothshop.domain.enums.DiscountType;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.security.Principal;

@Controller
@RequestMapping("/admin/vouchers")
@RequiredArgsConstructor
@PreAuthorize("hasAnyRole('SUPER_ADMIN', 'MARKETING_STAFF')")
public class VoucherAdminController {

    private final VoucherService voucherService;

    @GetMapping
    public String listVouchers(
            @PageableDefault(size = 10, sort = "createdAt", direction = Sort.Direction.DESC) Pageable pageable,
            Model model) {
        Page<VoucherResponse> vouchers = voucherService.getAllVouchers(pageable);
        model.addAttribute("vouchers", vouchers);
        model.addAttribute("currentPage", pageable.getPageNumber());
        model.addAttribute("totalPages", vouchers.getTotalPages());
        return "admin/vouchers/list";
    }

    @GetMapping("/create")
    public String showCreateForm(Model model) {
        if (!model.containsAttribute("voucherReq")) {
            model.addAttribute("voucherReq", new VoucherCreateRequest());
        }
        model.addAttribute("discountTypes", DiscountType.values());
        model.addAttribute("isEdit", false);
        return "admin/vouchers/form";
    }

    @PostMapping("/create")
    public String createVoucher(@Valid @ModelAttribute("voucherReq") VoucherCreateRequest request,
                                BindingResult result,
                                Principal principal,
                                RedirectAttributes redirectAttributes,
                                Model model) {
        if (result.hasErrors()) {
            model.addAttribute("discountTypes", DiscountType.values());
            model.addAttribute("isEdit", false);
            return "admin/vouchers/form";
        }
        try {
            voucherService.createVoucher(request, principal.getName());
            redirectAttributes.addFlashAttribute("successMessage", "Tạo voucher thành công!");
            return "redirect:/admin/vouchers";
        } catch (Exception e) {
            model.addAttribute("discountTypes", DiscountType.values());
            model.addAttribute("isEdit", false);
            model.addAttribute("errorMessage", e.getMessage());
            return "admin/vouchers/form";
        }
    }

    @GetMapping("/edit/{id}")
    public String showEditForm(@PathVariable Long id, Model model) {
        VoucherResponse voucher = voucherService.getVoucherById(id);
        if (!model.containsAttribute("voucherReq")) {
            VoucherUpdateRequest request = VoucherUpdateRequest.builder()
                    .discountType(voucher.getDiscountType())
                    .discountValue(voucher.getDiscountValue())
                    .minOrderValue(voucher.getMinOrderAmount())
                    .maxDiscount(voucher.getMaxDiscount())
                    .validFrom(voucher.getValidFrom())
                    .validTo(voucher.getValidTo())
                    .usageLimit(voucher.getUsageLimit())
                    .build();
            model.addAttribute("voucherReq", request);
        }
        model.addAttribute("discountTypes", DiscountType.values());
        model.addAttribute("isEdit", true);
        model.addAttribute("voucherId", id);
        model.addAttribute("voucherCode", voucher.getCode());
        return "admin/vouchers/form";
    }

    @PostMapping("/edit/{id}")
    public String updateVoucher(@PathVariable Long id,
                                @Valid @ModelAttribute("voucherReq") VoucherUpdateRequest request,
                                BindingResult result,
                                Principal principal,
                                RedirectAttributes redirectAttributes,
                                Model model) {
        if (result.hasErrors()) {
            model.addAttribute("discountTypes", DiscountType.values());
            model.addAttribute("isEdit", true);
            model.addAttribute("voucherId", id);
            // voucher code is display only; fetch to keep UI consistent
            model.addAttribute("voucherCode", voucherService.getVoucherById(id).getCode());
            return "admin/vouchers/form";
        }
        try {
            voucherService.updateVoucher(id, request, principal.getName());
            redirectAttributes.addFlashAttribute("successMessage", "Cập nhật voucher thành công!");
            return "redirect:/admin/vouchers";
        } catch (BusinessException ex) {
            model.addAttribute("discountTypes", DiscountType.values());
            model.addAttribute("isEdit", true);
            model.addAttribute("voucherId", id);
            model.addAttribute("voucherCode", voucherService.getVoucherById(id).getCode());
            model.addAttribute("errorMessage", ex.getMessage());
            return "admin/vouchers/form";
        }
    }

    // Vì Thymeleaf form thông thường khó mapping chuẩn PUT/DELETE nếu không bật filter,
    // một Best Practice là bọc trong POST actions.
    @PostMapping("/{id}/disable")
    public String disableVoucher(@PathVariable Long id, Principal principal, RedirectAttributes redirectAttributes) {
        voucherService.disableVoucher(id, principal.getName());
        redirectAttributes.addFlashAttribute("successMessage", "Đã vô hiệu hóa voucher!");
        return "redirect:/admin/vouchers";
    }

    @PostMapping("/{id}/enable")
    public String enableVoucher(@PathVariable Long id, Principal principal, RedirectAttributes redirectAttributes) {
        try {
            voucherService.reactivateVoucher(id, principal.getName());
            redirectAttributes.addFlashAttribute("successMessage", "Đã kích hoạt lại voucher!");
        } catch (BusinessException ex) {
            redirectAttributes.addFlashAttribute("errorMessage", ex.getMessage());
        }
        return "redirect:/admin/vouchers";
    }
}
