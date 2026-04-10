package com.clothshop.admin.controllers;

import com.clothshop.admin.dtos.request.products.VariantPriceUpdateRequest;
import com.clothshop.admin.dtos.request.products.StockUpdateRequest;
import com.clothshop.admin.dtos.request.products.VariantCreateRequest;
import com.clothshop.admin.dtos.response.products.VariantResponse;
import com.clothshop.admin.services.ProductVariantService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.security.Principal;

@Controller
@RequestMapping("/admin/variants")
@RequiredArgsConstructor
@Slf4j
@PreAuthorize("hasAnyRole('SUPER_ADMIN', 'SALE_PRODUCT_STAFF')")
public class ProductVariantController {

    private final ProductVariantService variantService;

    /**
     * Show form to add a new variant for a product.
     */
    @GetMapping("/create")
    public String showCreateForm(@RequestParam Long productId, Model model) {
        VariantCreateRequest request = new VariantCreateRequest();
        request.setProductId(productId);
        model.addAttribute("variant", request);
        model.addAttribute("productId", productId);
        return "admin/products/variants/create";
    }

    /**
     * Create a new variant.
     */
    @PostMapping("/create")
    public String createVariant(
            @Valid @ModelAttribute("variant") VariantCreateRequest request,
            BindingResult bindingResult,
            Principal principal,
            RedirectAttributes redirectAttributes,
            Model model) {

        if (bindingResult.hasErrors()) {
            model.addAttribute("productId", request.getProductId());
            return "admin/products/variants/create";
        }

        variantService.addVariant(request, principal.getName());
        redirectAttributes.addFlashAttribute("successMessage", "Đã thêm variant thành công");
        return "redirect:/admin/products/" + request.getProductId();
    }

    /**
     * Show form to update variant stock.
     */
    @GetMapping("/{id}/update-stock")
    public String showUpdateStockForm(@PathVariable Long id, Model model) {
        VariantResponse variant = variantService.getVariantById(id);
        model.addAttribute("variant", variant);
        model.addAttribute("stockRequest", new StockUpdateRequest());
        return "admin/products/variants/update-stock";
    }

    /**
     * Update variant stock.
     */
    @PostMapping("/{id}/update-stock")
    public String updateStock(
            @PathVariable Long id,
            @Valid @ModelAttribute("stockRequest") StockUpdateRequest request,
            BindingResult bindingResult,
            Principal principal,
            RedirectAttributes redirectAttributes,
            Model model) {

        if (bindingResult.hasErrors()) {
            VariantResponse variant = variantService.getVariantById(id);
            model.addAttribute("variant", variant);
            return "admin/products/variants/update-stock";
        }

        variantService.updateStock(id, request, principal.getName());

        VariantResponse variant = variantService.getVariantById(id);
        redirectAttributes.addFlashAttribute("successMessage", "Đã cập nhật số lượng tồn kho thành công");
        return "redirect:/admin/products/" + variant.getProductId();
    }

    /**
     * Show form to update variant price.
     */
    @GetMapping("/{id}/update-price")
    public String showUpdatePriceForm(@PathVariable Long id, Model model) {
        VariantResponse variant = variantService.getVariantById(id);

        // Create VariantPriceUpdateRequest with current price
        VariantPriceUpdateRequest priceRequest = new VariantPriceUpdateRequest();
        priceRequest.setPrice(variant.getRetailPrice());

        model.addAttribute("variant", variant);
        model.addAttribute("priceRequest", priceRequest);
        return "admin/products/variants/update-price";
    }

    /**
     * Update variant price.
     */
    @PostMapping("/{id}/update-price")
    public String updatePrice(
            @PathVariable Long id,
            @Valid @ModelAttribute("priceRequest") VariantPriceUpdateRequest request,
            BindingResult bindingResult,
            Principal principal,
            RedirectAttributes redirectAttributes,
            Model model) {

        if (bindingResult.hasErrors()) {
            VariantResponse variant = variantService.getVariantById(id);
            model.addAttribute("variant", variant);
            return "admin/products/variants/update-price";
        }

        variantService.updatePrice(id, request, principal.getName());

        VariantResponse variant = variantService.getVariantById(id);
        redirectAttributes.addFlashAttribute("successMessage", "Đã cập nhật giá thành công");
        return "redirect:/admin/products/" + variant.getProductId();
    }
}






