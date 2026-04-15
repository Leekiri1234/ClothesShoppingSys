package com.clothshop.admin.controllers;

import com.clothshop.admin.dtos.request.products.ProductCreateRequest;
import com.clothshop.admin.dtos.request.products.ProductUpdateRequest;
import com.clothshop.admin.dtos.response.products.ProductAdminResponse;
import com.clothshop.admin.dtos.response.products.VariantResponse;
import com.clothshop.admin.services.CategoryService;
import com.clothshop.admin.services.ProductAdminService;
import com.clothshop.admin.services.FeaturedCollectionService;
import com.clothshop.admin.services.ProductVariantService;
import com.clothshop.admin.services.ReviewModerationService;
import com.clothshop.common.exceptions.BusinessException;
import com.clothshop.common.dtos.request.PagingRequest;
import com.clothshop.common.dtos.response.PageResponse;
import com.clothshop.domain.enums.ProductStatus;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.security.Principal;
import java.util.List;

@Controller
@RequestMapping("/admin/products")
@RequiredArgsConstructor
@Slf4j
// Đặt PreAuthorize ở class-level để bảo vệ toàn bộ API bên trong, tránh rủi ro rò rỉ khi mở rộng sau này.
@PreAuthorize("hasAnyRole('SUPER_ADMIN', 'SALE_PRODUCT_STAFF')")
public class ProductAdminController {

    private final ProductAdminService productAdminService;
    private final CategoryService categoryService;
    private final FeaturedCollectionService featuredCollectionService;
    private final ProductVariantService variantService;
    private final ReviewModerationService reviewModerationService;

    @GetMapping
    public String listProducts(
            @RequestParam(required = false) String keyword,
            @RequestParam(required = false) Long categoryId,
            @RequestParam(required = false) ProductStatus prodStatus,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size,
            @RequestParam(required = false, defaultValue = "createdAt") String sortBy,
            @RequestParam(required = false, defaultValue = "DESC") String direction,
            Model model) {

        // Sửa lại Builder cho khớp với PagingRequest mới
        PagingRequest pagingRequest = PagingRequest.builder()
                .pageNumber(page)
                .pageSize(size)
                .sortBy(sortBy)
                .sortDirection(direction)
                .build();

        PageResponse<ProductAdminResponse> products = productAdminService.getAllProducts(keyword, categoryId, prodStatus, pagingRequest);

        model.addAttribute("products", products);
        model.addAttribute("categories", categoryService.getAllCategories());

        // Pass filter values to the view
        model.addAttribute("keyword", keyword);
        model.addAttribute("categoryId", categoryId);
        model.addAttribute("prodStatus", prodStatus);

        // Lưu ý: Dùng products.pageNumber trong Thymeleaf sẽ tốt hơn
        model.addAttribute("currentPage", page);

        return "admin/products/list";
    }

    @GetMapping("/create")
    public String showCreateForm(Model model) {
        model.addAttribute("product", new ProductCreateRequest());
        model.addAttribute("categories", categoryService.getAllCategories());
        return "admin/products/create";
    }

    @PostMapping("/create")
    public String createProduct(
            @Valid @ModelAttribute("product") ProductCreateRequest request,
            BindingResult bindingResult,
            @RequestParam(value = "imageFile", required = false) MultipartFile imageFile,
            RedirectAttributes redirectAttributes,
            Model model) {

        if (bindingResult.hasErrors()) {
            // Cn add li categories nu form l—i ‘ƒ dropdown khng b‹ tr‘ng
            model.addAttribute("categories", categoryService.getAllCategories());
            return "admin/products/create";
        }

        ProductAdminResponse createdProduct = productAdminService.createProduct(request, imageFile);
        redirectAttributes.addFlashAttribute("successMessage",
                " Đã tạo sản phẩm thành công: " + createdProduct.getProductName());
        return "redirect:/admin/products";
    }

    @GetMapping("/{id}/edit")
    public String showEditForm(@PathVariable Long id, Model model) {
        ProductAdminResponse product = productAdminService.getProductById(id);
        model.addAttribute("product", product);
        model.addAttribute("categories", categoryService.getAllCategories());
        return "admin/products/edit";
    }

    @PostMapping("/{id}/edit")
    public String updateProduct(
            @PathVariable Long id,
            @Valid @ModelAttribute("product") ProductUpdateRequest request,
            BindingResult bindingResult,
            @RequestParam(value = "imageFile", required = false) MultipartFile imageFile,
            RedirectAttributes redirectAttributes,
            Model model) {

        if (bindingResult.hasErrors()) {
            model.addAttribute("categories", categoryService.getAllCategories());
            return "admin/products/edit";
        }

        ProductAdminResponse updatedProduct = productAdminService.updateProduct(id, request, imageFile);
        redirectAttributes.addFlashAttribute("successMessage",
                " Cập nhật sản phẩm thành công: " + updatedProduct.getProductName());
        return "redirect:/admin/products";
    }

    @PostMapping("/{id}/toggle-status")
    public String toggleProductStatus(@PathVariable Long id, RedirectAttributes redirectAttributes) {
        String message = productAdminService.toggleProductStatus(id);
        redirectAttributes.addFlashAttribute("successMessage", message);
        return "redirect:/admin/products";
    }

    @GetMapping("/{id}")
    public String viewProduct(@PathVariable Long id, Model model) {
        ProductAdminResponse product = productAdminService.getProductById(id);
        List<VariantResponse> variants = variantService.getVariantsByProductId(id);
        model.addAttribute("product", product);
        model.addAttribute("variants", variants);
        model.addAttribute("reviews", reviewModerationService.getReviewsByProductId(id));
        return "admin/products/detail";
    }

    @PostMapping("/{id}/add-to-collection")
    public String addProductToCollection(
            @PathVariable("id") Long productId,
            @RequestParam("collectionId") Long collectionId,
            Principal principal,
            RedirectAttributes redirectAttributes) {

        featuredCollectionService.addProductToCollection(collectionId, productId, principal.getName());

        redirectAttributes.addFlashAttribute("successMessage", "Sản phẩm đã được thêm vào bộ sưu tập thành công");
        return "redirect:/admin/products/" + productId;
    }

    @PostMapping("/{productId}/reviews/{reviewId}/hide")
    public String hideReviewFromProduct(
            @PathVariable Long productId,
            @PathVariable Long reviewId,
            @RequestParam String reason,
            Principal principal,
            RedirectAttributes redirectAttributes) {
        try {
            reviewModerationService.hideReviewForProduct(productId, reviewId, reason, principal.getName());
            redirectAttributes.addFlashAttribute("successMessage", "Đã ẩn đánh giá");
        } catch (BusinessException ex) {
            redirectAttributes.addFlashAttribute("errorMessage", ex.getMessage());
        }
        return "redirect:/admin/products/" + productId + "#reviews";
    }

    @PostMapping("/{productId}/reviews/{reviewId}/show")
    public String showReviewFromProduct(
            @PathVariable Long productId,
            @PathVariable Long reviewId,
            Principal principal,
            RedirectAttributes redirectAttributes) {
        try {
            reviewModerationService.approveReviewForProduct(productId, reviewId, principal.getName());
            redirectAttributes.addFlashAttribute("successMessage", "Đã hiển thị lại đánh giá");
        } catch (BusinessException ex) {
            redirectAttributes.addFlashAttribute("errorMessage", ex.getMessage());
        }
        return "redirect:/admin/products/" + productId + "#reviews";
    }

}
