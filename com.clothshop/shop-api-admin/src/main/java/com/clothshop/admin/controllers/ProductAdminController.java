package com.clothshop.admin.controllers;

import com.clothshop.admin.dtos.request.products.ProductCreateRequest;
import com.clothshop.admin.dtos.request.products.ProductUpdateRequest;
import com.clothshop.admin.dtos.response.products.ProductAdminResponse;
import com.clothshop.admin.services.CategoryService;
import com.clothshop.admin.services.ProductAdminService;
import com.clothshop.common.dtos.request.PagingRequest;
import com.clothshop.common.dtos.response.PageResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
@RequestMapping("/admin/products")
@RequiredArgsConstructor
@Slf4j
//Đặt PreAuthorize ở class-level để bảo vệ toàn bộ API bên trong, tránh rủi ro rò rỉ khi mở rộng sau này
@PreAuthorize("hasAnyRole('SUPER_ADMIN', 'SALE_PRODUCT_STAFF')")
public class ProductAdminController {

    private final ProductAdminService productAdminService;
    private final CategoryService categoryService;

    @GetMapping
    public String listProducts(
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

        PageResponse<ProductAdminResponse> products = productAdminService.getAllProducts(pagingRequest);

        model.addAttribute("products", products);
        // Lưu ý: Dùng products.pageNumber trong Thymeleaf sẽ tốt hơn
        model.addAttribute("currentPage", page);

        return "admin/products/list";
    }

    @GetMapping("/create")
    public String showCreateForm(Model model) {
        model.addAttribute("product", new ProductCreateRequest());
        model.addAttribute("categories", categoryService.getAllCategoriesForDropdown());
        return "admin/products/create";
    }

    @PostMapping("/create")
    public String createProduct(
            @Valid @ModelAttribute("product") ProductCreateRequest request,
            BindingResult bindingResult,
            RedirectAttributes redirectAttributes,
            Model model) {

        if (bindingResult.hasErrors()) {
            // Cần add lại categories nếu form lỗi để dropdown không bị trống
            model.addAttribute("categories", categoryService.getAllCategoriesForDropdown());
            return "admin/products/create";
        }

        ProductAdminResponse createdProduct = productAdminService.createProduct(request);
        redirectAttributes.addFlashAttribute("successMessage",
                "Đã tạo sản phẩm thành công: " + createdProduct.getProductName());
        return "redirect:/admin/products";
    }

    @GetMapping("/{id}/edit")
    public String showEditForm(@PathVariable Long id, Model model) {
        ProductAdminResponse product = productAdminService.getProductById(id);
        model.addAttribute("product", product);
        model.addAttribute("categories", categoryService.getAllCategoriesForDropdown());
        return "admin/products/edit";
    }

    @PostMapping("/{id}/edit")
    public String updateProduct(
            @PathVariable Long id,
            @Valid @ModelAttribute("product") ProductUpdateRequest request,
            BindingResult bindingResult,
            RedirectAttributes redirectAttributes,
            Model model) {

        if (bindingResult.hasErrors()) {
            model.addAttribute("categories", categoryService.getAllCategoriesForDropdown());
            return "admin/products/edit";
        }

        ProductAdminResponse updatedProduct = productAdminService.updateProduct(id, request);
        redirectAttributes.addFlashAttribute("successMessage",
                "Đã cập nhật sản phẩm thành công: " + updatedProduct.getProductName());
        return "redirect:/admin/products";
    }

    @PostMapping("/{id}/delete")
    public String deleteProduct(@PathVariable Long id, RedirectAttributes redirectAttributes) {
        productAdminService.deleteProduct(id);
        redirectAttributes.addFlashAttribute("successMessage", "Đã xóa sản phẩm thành công");
        return "redirect:/admin/products";
    }

    @GetMapping("/{id}")
    public String viewProduct(@PathVariable Long id, Model model) {
        ProductAdminResponse product = productAdminService.getProductById(id);
        model.addAttribute("product", product);
        return "admin/products/detail";
    }
}