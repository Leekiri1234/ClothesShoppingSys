package com.clothshop.admin.controllers;

import com.clothshop.admin.dtos.request.banner.BannerRequest;
import com.clothshop.admin.services.BannerService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

@Controller
@RequestMapping("/admin/banners")
@RequiredArgsConstructor
public class BannerController {

    private final BannerService bannerService;

    // =========================
    // 📌 1. LIST ALL
    // =========================
    @GetMapping
    public String list(Model model) {
        model.addAttribute("banners", bannerService.getAll());
        return "admin/banners/list";
    }

    // =========================
    // 📌 2. SHOW CREATE/EDIT FORM
    // =========================
    @GetMapping("/form")
    public String showForm(@RequestParam(required = false) Long id, Model model) {
        if (id != null) {
            model.addAttribute("banner", bannerService.getById(id));
        } else {
            model.addAttribute("banner", new BannerRequest());
        }
        return "admin/banners/form";
    }

    // =========================
    // 📌 3. CREATE FORM (LEGACY)
    // =========================
    @GetMapping("/create")
    public String showCreateForm(Model model) {
        model.addAttribute("banner", new BannerRequest());
        return "admin/banners/form";
    }

    // =========================
    // 📌 4. CREATE
    // =========================
    @PostMapping
    public String create(
            @ModelAttribute("banner") BannerRequest request,
            @RequestParam("file") MultipartFile file
    ) {
        bannerService.create(request, file);
        return "admin/banners/form";
    }

    // =========================
    // 📌 5. SHOW UPDATE FORM (LEGACY)
    // =========================
    @GetMapping("/edit/{id}")
    public String showUpdateForm(@PathVariable Long id, Model model) {
        model.addAttribute("banner", bannerService.getById(id));
        return "admin/banners/form";
    }

    // =========================
    // 📌 6. UPDATE
    // =========================
    @PostMapping("/{id}")
    public String update(
            @PathVariable Long id,
            @ModelAttribute("banner") BannerRequest request,
            @RequestParam(value = "file", required = false) MultipartFile file
    ) {
        bannerService.update(id, request, file);
        return "admin/banners/form";
    }

    // =========================
    // 📌 7. DELETE FROM FORM
    // =========================
    @PostMapping("/form")
    public String deleteFromForm(@RequestParam(required = false) Long id) {
        if (id != null) {
            bannerService.delete(id);
        }
        return "admin/banners/form";
    }

    // =========================
    // 📌 8. DELETE (LEGACY)
    // =========================
    @GetMapping("/delete/{id}")
    public String delete(@PathVariable Long id) {
        bannerService.delete(id);
        return "admin/banners/form";
    }
}