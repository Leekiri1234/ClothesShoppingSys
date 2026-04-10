package com.clothshop.admin.controllers;

import com.clothshop.admin.dtos.BannerFormDTO;
import com.clothshop.admin.dtos.request.banner.BannerRequest;
import com.clothshop.admin.dtos.response.banner.BannerResponse;
import com.clothshop.admin.mappers.BannerMapper;
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
    private final BannerMapper bannerMapper;

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
            BannerResponse response = bannerService.getById(id);
            BannerFormDTO formDTO = bannerMapper.toFormDTO(
                bannerMapper.toEntity(new BannerRequest() {{
                    setTitle(response.getTitle());
                    setLinkUrl(response.getLinkUrl());
                    setDisplayOrder(response.getDisplayOrder());
                    setStatus(response.getStatus());
                    setStartDate(response.getStartDate());
                    setEndDate(response.getEndDate());
                }})
            );
            formDTO.setId(response.getId());
            formDTO.setImageUrl(response.getImageUrl());
            model.addAttribute("banner", formDTO);
        } else {
            model.addAttribute("banner", new BannerFormDTO());
        }
        return "admin/banners/form";
    }

    // =========================
    // 📌 3. CREATE FORM (LEGACY)
    // =========================
    @GetMapping("/create")
    public String showCreateForm(Model model) {
        model.addAttribute("banner", new BannerFormDTO());
        return "admin/banners/form";
    }

    // =========================
    // 📌 4. CREATE
    // =========================
    @PostMapping
    public String create(
            @ModelAttribute("banner") BannerFormDTO formDTO,
            @RequestParam(value = "file", required = false) MultipartFile file
    ) {
        BannerRequest request = new BannerRequest();
        request.setTitle(formDTO.getTitle());
        request.setLinkUrl(formDTO.getLinkUrl());
        request.setDisplayOrder(formDTO.getDisplayOrder());
        request.setStatus(formDTO.getStatus());
        request.setStartDate(formDTO.getStartDate());
        request.setEndDate(formDTO.getEndDate());
        
        bannerService.create(request, file);
        return "redirect:/admin/banners";
    }

    // =========================
    // 📌 5. SHOW UPDATE FORM (LEGACY)
    // =========================
    @GetMapping("/edit/{id}")
    public String showUpdateForm(@PathVariable Long id, Model model) {
        BannerResponse response = bannerService.getById(id);
        BannerFormDTO formDTO = BannerFormDTO.builder()
                .id(response.getId())
                .title(response.getTitle())
                .imageUrl(response.getImageUrl())
                .linkUrl(response.getLinkUrl())
                .displayOrder(response.getDisplayOrder())
                .status(response.getStatus())
                .startDate(response.getStartDate())
                .endDate(response.getEndDate())
                .build();
        model.addAttribute("banner", formDTO);
        return "admin/banners/form";
    }

    // =========================
    // 📌 6. UPDATE
    // =========================
    @PostMapping("/{id}")
    public String update(
            @PathVariable Long id,
            @ModelAttribute("banner") BannerFormDTO formDTO,
            @RequestParam(value = "file", required = false) MultipartFile file
    ) {
        BannerRequest request = new BannerRequest();
        request.setTitle(formDTO.getTitle());
        request.setLinkUrl(formDTO.getLinkUrl());
        request.setDisplayOrder(formDTO.getDisplayOrder());
        request.setStatus(formDTO.getStatus());
        request.setStartDate(formDTO.getStartDate());
        request.setEndDate(formDTO.getEndDate());
        
        bannerService.update(id, request, file);
        return "redirect:/admin/banners";
    }

    // =========================
    // 📌 7. DELETE FROM FORM -> CHANGE TO TOGGLE STATUS
    // =========================
    @PostMapping("/form")
    public String toggleFromForm(@RequestParam(required = false) Long id) {
        if (id != null) {
            bannerService.toggleStatus(id);
        }
        return "redirect:/admin/banners";
    }

    // =========================
    // 📌 8. TOGGLE STATUS VIA PATH VAR (LEGACY)
    // =========================
    @GetMapping("/delete/{id}")
    public String toggleViaPath(@PathVariable Long id) {
        bannerService.toggleStatus(id);
        return "redirect:/admin/banners";
    }
}