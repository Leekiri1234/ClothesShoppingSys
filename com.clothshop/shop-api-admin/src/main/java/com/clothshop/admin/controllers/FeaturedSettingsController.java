package com.clothshop.admin.controllers;

import com.clothshop.admin.dtos.request.marketing.FeaturedProductListRequest;
import com.clothshop.admin.services.AdminExperienceService;
import com.clothshop.admin.services.FeaturedProductService;
import com.clothshop.domain.entities.marketing.FeaturedProduct;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Controller;
import org.springframework.transaction.annotation.Transactional; // Import thêm cái này
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.security.Principal;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

@Controller
@RequestMapping("/admin/settings/featured")
@RequiredArgsConstructor
@PreAuthorize("hasAnyRole('SUPER_ADMIN', 'MARKETING_STAFF')")
public class FeaturedSettingsController {

    private final FeaturedProductService featuredProductService;
    private final AdminExperienceService adminExperienceService;

    @GetMapping
    @Transactional(readOnly = true) // THÊM DÒNG NÀY ĐỂ GIỮ SESSION MỞ
    public String showFeaturedSettings(Model model) {

        // 1. Lấy danh sách đang được ghim
        List<FeaturedProduct> featuredProducts = featuredProductService.getActiveFeaturedProducts();

        // Ép tải hình ảnh cho danh sách đã ghim
        featuredProducts.forEach(fp -> {
            if (fp.getProduct() != null && fp.getProduct().getImages() != null) {
                fp.getProduct().getImages().size(); // Đánh thức Lazy load
            }
        });
        model.addAttribute("featuredProducts", featuredProducts);

        // 2. Lấy danh sách khả dụng từ dữ liệu kho (đảm bảo ảnh + tên + SKU đồng bộ màn quản lý kho)
        Set<Long> featuredIds = featuredProducts.stream()
                .map(fp -> fp.getProduct() != null ? fp.getProduct().getId() : null)
                .filter(id -> id != null)
                .collect(Collectors.toCollection(HashSet::new));

        Map<String, Object> inventory = adminExperienceService.getInventoryOverview();
        @SuppressWarnings("unchecked")
        List<Map<String, Object>> inventoryItems = (List<Map<String, Object>>) inventory.getOrDefault("items", List.of());

        List<Map<String, Object>> availableProducts = inventoryItems.stream()
                .filter(item -> {
                    Object rawId = item.get("productId");
                    return rawId instanceof Number && !featuredIds.contains(((Number) rawId).longValue());
                })
                .limit(120)
                .collect(Collectors.toList());

        model.addAttribute("availableProducts", availableProducts);

        return "admin/settings/featured";
    }

    @PostMapping
    public String updateFeaturedList(@ModelAttribute FeaturedProductListRequest request,
                                     Principal principal,
                                     RedirectAttributes redirectAttributes) {

        featuredProductService.updateFeaturedList(request.getFeaturedProducts(), principal.getName());

        redirectAttributes.addFlashAttribute("successMessage", "Cập nhật sản phẩm nổi bật thành công!");
        return "redirect:/admin/settings/featured";
    }
}