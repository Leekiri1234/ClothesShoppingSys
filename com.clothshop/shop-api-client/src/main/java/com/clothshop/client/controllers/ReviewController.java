package com.clothshop.client.controllers;

import com.clothshop.client.dtos.request.ReviewCreateRequest;
import com.clothshop.client.services.ReviewClientService;
import com.clothshop.common.exceptions.BusinessException;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.security.Principal;

@Controller
@RequestMapping("/reviews")
@RequiredArgsConstructor
@PreAuthorize("hasRole('CUSTOMER')")
public class ReviewController {

    private final ReviewClientService reviewClientService;

    /**
     * POST /reviews/submit
     * Nhận form đánh giá, validate, lưu, redirect về trang sản phẩm.
     * productSlug dùng để redirect về đúng trang sau khi submit.
     */
    @PostMapping("/submit")
    public String submitReview(
            @Valid @ModelAttribute ReviewCreateRequest request,
            @RequestParam("productSlug") String productSlug,
            Principal principal,
            RedirectAttributes redirectAttributes) {

        if (principal == null) return "redirect:/login";

        try {
            reviewClientService.submitReview(principal.getName(), request);
            redirectAttributes.addFlashAttribute("reviewSuccess",
                    "Cảm ơn bạn đã đánh giá! Đánh giá của bạn đang chờ duyệt.");
        } catch (BusinessException e) {
            redirectAttributes.addFlashAttribute("reviewError", e.getMessage());
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("reviewError", "Có lỗi xảy ra, vui lòng thử lại.");
        }

        return "redirect:/products/" + productSlug;
    }
}