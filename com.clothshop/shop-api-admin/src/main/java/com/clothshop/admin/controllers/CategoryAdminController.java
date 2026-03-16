package com.clothshop.admin.controllers;

import com.clothshop.admin.dtos.request.products.CategoryCreateRequest;
import com.clothshop.admin.dtos.request.products.CategoryUpdateRequest;
import com.clothshop.admin.dtos.response.products.CategoryAdminResponse;
import com.clothshop.admin.services.CategoryService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.List;

@Controller
@RequestMapping("/admin/categories")
@RequiredArgsConstructor
@PreAuthorize("hasAnyRole('SUPER_ADMIN', 'SALE_PRODUCT_STAFF')")
public class CategoryAdminController {

    private final CategoryService categoryService;

    @GetMapping
    public String listCategories(Model model) {
        List<CategoryAdminResponse> categories = categoryService.getAllCategoriesIncludingInactive();

        long countActive   = categories.stream().filter(c -> Boolean.TRUE.equals(c.getIsActive()) && "ACTIVE".equals(c.getCatStatus())).count();
        long countInactive = categories.stream().filter(c -> Boolean.TRUE.equals(c.getIsActive()) && "INACTIVE".equals(c.getCatStatus())).count();
        long countDeleted  = categories.stream().filter(c -> !Boolean.TRUE.equals(c.getIsActive())).count();
        long countRoot     = categories.stream().filter(c -> c.getParentId() == null).count();
        long countChild    = categories.stream().filter(c -> c.getParentId() != null).count();

        model.addAttribute("categories", categories);
        model.addAttribute("countActive", countActive);
        model.addAttribute("countInactive", countInactive);
        model.addAttribute("countDeleted", countDeleted);
        model.addAttribute("countRoot", countRoot);
        model.addAttribute("countChild", countChild);
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

    @PostMapping("/{id}/toggle-status")
    public String toggleCategoryStatus(@PathVariable Long id, RedirectAttributes redirectAttributes) {
        String message = categoryService.toggleCategoryStatus(id);
        redirectAttributes.addFlashAttribute("successMessage", message);
        return "redirect:/admin/categories";
    }
}