package com.clothshop.admin.controllers;

import com.clothshop.admin.dtos.request.products.StockUpdateRequest;
import com.clothshop.admin.dtos.request.products.VariantCreateRequest;
import com.clothshop.admin.services.ProductVariantService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.security.Principal;

/**
 * ProductVariantAdminController - Quản lý API cho biến thể và kho hàng.
 */
@Controller
@RequestMapping("/admin")
@RequiredArgsConstructor
@Slf4j
@PreAuthorize("hasAnyRole('SUPER_ADMIN', 'SALE_PRODUCT_STAFF')")
public class ProductVariantAdminController {

    private final ProductVariantService variantService;

    @PostMapping("/products/{productId}/variants")
    public String addVariant(
            @PathVariable Long productId,
            @Valid @ModelAttribute VariantCreateRequest request,
            Principal principal,
            RedirectAttributes redirectAttributes) {

        log.info("Admin {} is adding variant for product ID: {}", principal.getName(), productId);

        // Đảm bảo productId trong Request khớp với PathVariable
        request.setProductId(productId);

        variantService.addVariant(request, principal.getName());

        redirectAttributes.addFlashAttribute("successMessage", "Đã thêm biến thể mới thành công");
        return "redirect:/admin/products/" + productId;
    }


    @PostMapping("/variants/{variantId}/stock")
    public String updateStock(
            @PathVariable Long variantId,
            @Valid @ModelAttribute StockUpdateRequest request,
            Principal principal,
            RedirectAttributes redirectAttributes) {

        log.info("Admin {} is updating stock for variant ID: {}", principal.getName(), variantId);

        variantService.updateStock(variantId, request, principal.getName());

        redirectAttributes.addFlashAttribute("successMessage", "Cập nhật tồn kho thành công");

        // Quay lại trang chi tiết sản phẩm cha (cần logic tìm productId từ variantId nếu muốn redirect chính xác)
        // Ở đây tạm redirect về danh sách sản phẩm hoặc dùng Referer header.
        return "redirect:/admin/products";
    }
}