package com.clothshop.admin.controllers;

import com.clothshop.admin.dtos.request.marketing.FeaturedProductListRequest;
import com.clothshop.admin.services.FeaturedProductService;
import com.clothshop.domain.models.marketing.FeaturedProduct;
import com.clothshop.domain.models.product.Product;
import com.clothshop.domain.repositories.product.ProductRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Controller;
import org.springframework.transaction.annotation.Transactional; // Import thêm cái này
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.security.Principal;
import java.util.List;

@Controller
@RequestMapping("/admin/settings/featured")
@RequiredArgsConstructor
@PreAuthorize("hasAnyRole('SUPER_ADMIN', 'MARKETING_STAFF')")
public class FeaturedSettingsController {

    private final FeaturedProductService featuredProductService;
    private final ProductRepository productRepository;

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

        // 2. Lấy danh sách khả dụng (gợi ý)
        List<Product> availableProducts = productRepository.findAllActive(PageRequest.of(0, 50)).getContent();

        // Ép tải hình ảnh cho danh sách khả dụng
        availableProducts.forEach(product -> {
            if (product.getImages() != null) {
                product.getImages().size(); // Đánh thức Lazy load
            }
        });
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