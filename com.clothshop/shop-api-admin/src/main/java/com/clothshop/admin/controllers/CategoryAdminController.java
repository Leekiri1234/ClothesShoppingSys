package com.clothshop.admin.controllers;

import com.clothshop.admin.dtos.request.products.CategoryCreateRequest;
import com.clothshop.admin.dtos.request.products.CategoryUpdateRequest;
import com.clothshop.admin.services.CategoryService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
@RequestMapping("/admin/categories")
@RequiredArgsConstructor
@PreAuthorize("hasAnyRole('SUPER_ADMIN', 'SALE_PRODUCT_STAFF')")
public class CategoryAdminController {

    private final CategoryService categoryService;

    @GetMapping
    public String listCategories(Model model) {
        model.addAttribute("categories", categoryService.getAllCategories());
        return "admin/categories/list";
    }

    @GetMapping("/create")
    public String showCreateForm(Model model) {
        model.addAttribute("categoryRequest", new CategoryCreateRequest());
        model.addAttribute("parentCategories", categoryService.getAllCategories());
        return "admin/categories/create";
    }

    @PostMapping("/create")
    public String createCategory(@Valid @ModelAttribute("categoryRequest") CategoryCreateRequest request,
                                 BindingResult result, RedirectAttributes redirectAttributes, Model model) {
        if (result.hasErrors()) {
            model.addAttribute("parentCategories", categoryService.getAllCategories());
            return "admin/categories/create";
        }
        categoryService.createCategory(request);
        redirectAttributes.addFlashAttribute("successMessage", "Tạo danh mục thành công!");
        return "redirect:/admin/categories";
    }

    @GetMapping("/{id}/edit")
    public String showEditForm(@PathVariable Long id, Model model) {
        model.addAttribute("categoryRequest", categoryService.getCategoryById(id));
        model.addAttribute("parentCategories", categoryService.getAllCategories());
        model.addAttribute("categoryId", id);
        return "admin/categories/edit";
    }

    @PostMapping("/{id}/edit")
    public String updateCategory(@PathVariable Long id,
                                 @Valid @ModelAttribute("categoryRequest") CategoryUpdateRequest request,
                                 BindingResult result, RedirectAttributes redirectAttributes, Model model) {
        if (result.hasErrors()) {
            model.addAttribute("parentCategories", categoryService.getAllCategories());
            model.addAttribute("categoryId", id);
            return "admin/categories/edit";
        }
        categoryService.updateCategory(id, request);
        redirectAttributes.addFlashAttribute("successMessage", "Cập nhật danh mục thành công!");
        return "redirect:/admin/categories";
    }

    @PostMapping("/{id}/delete")
    public String deleteCategory(@PathVariable Long id, RedirectAttributes redirectAttributes) {
        categoryService.deleteCategory(id);
        redirectAttributes.addFlashAttribute("successMessage", "Đã xóa danh mục!");
        return "redirect:/admin/categories";
    }
}