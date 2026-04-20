package com.clothshop.admin.controllers;

import com.clothshop.admin.dtos.response.review.ReviewModerationResponse;
import com.clothshop.admin.services.ReviewModerationService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
@RequestMapping("/admin/reviews")
@RequiredArgsConstructor
@PreAuthorize("hasAnyRole('SUPER_ADMIN', 'CUSTOMER_SERVICE', 'SALE_PRODUCT_STAFF')")
public class ReviewModerationController {

    private final ReviewModerationService reviewModerationService;

    @GetMapping
    public String listReviews(@RequestParam(required = false) String status,
                              @RequestParam(required = false) Long productId,
                              @RequestParam(required = false) Integer rating,
                              @RequestParam(required = false) String keyword,
                              @PageableDefault(size = 10, sort = "createdAt", direction = org.springframework.data.domain.Sort.Direction.DESC) Pageable pageable,
                              Model model) {
        Page<ReviewModerationResponse> reviewPage = reviewModerationService.getReviews(status, productId, rating, keyword, pageable);

        model.addAttribute("reviews", reviewPage);
        model.addAttribute("selectedStatus", status);
        model.addAttribute("selectedProductId", productId);
        model.addAttribute("selectedRating", rating);
        model.addAttribute("selectedKeyword", keyword);
        model.addAttribute("reviewStatuses", new String[]{
                ReviewModerationService.STATUS_APPROVED,
                ReviewModerationService.STATUS_HIDDEN
        });
        return "admin/reviews/list";
    }

    @PostMapping("/{id}/approve")
    public String approveReview(@PathVariable Long id,
                                @RequestParam(required = false) String status,
                                @RequestParam(required = false) Long productId,
                                @RequestParam(required = false) Integer rating,
                                @RequestParam(required = false) String keyword,
                                @RequestParam(required = false) Integer page,
                                Authentication authentication,
                                RedirectAttributes redirectAttributes) {
        try {
            reviewModerationService.approveReview(id, authentication.getName());
            redirectAttributes.addFlashAttribute("success", "Đã hiển thị đánh giá thành công");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", e.getMessage());
        }
        return buildRedirect(status, productId, rating, keyword, page);
    }

    @PostMapping("/{id}/hide")
    public String hideReview(@PathVariable Long id,
                             @RequestParam String reason,
                             @RequestParam(required = false) String status,
                             @RequestParam(required = false) Long productId,
                             @RequestParam(required = false) Integer rating,
                             @RequestParam(required = false) String keyword,
                             @RequestParam(required = false) Integer page,
                             Authentication authentication,
                             RedirectAttributes redirectAttributes) {
        try {
            reviewModerationService.hideReview(id, reason, authentication.getName());
            redirectAttributes.addFlashAttribute("success", "Đã ẩn đánh giá thành công");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", e.getMessage());
        }
        return buildRedirect(status, productId, rating, keyword, page);
    }

    private String buildRedirect(String status, Long productId, Integer rating, String keyword, Integer page) {
        StringBuilder redirect = new StringBuilder("redirect:/admin/reviews");
        boolean hasQuery = false;

        if (productId != null) {
            redirect.append("?productId=").append(productId);
            hasQuery = true;
        }

        if (status != null && !status.isBlank()) {
            redirect.append(hasQuery ? "&" : "?").append("status=").append(status);
            hasQuery = true;
        }

        if (keyword != null && !keyword.isBlank()) {
            redirect.append(hasQuery ? "&" : "?").append("keyword=").append(keyword);
            hasQuery = true;
        }

        if (rating != null) {
            redirect.append(hasQuery ? "&" : "?").append("rating=").append(rating);
            hasQuery = true;
        }

        if (page != null && page >= 0) {
            redirect.append(hasQuery ? "&" : "?").append("page=").append(page);
        }

        return redirect.toString();
    }
}
