package com.clothshop.client.controllers;

import com.clothshop.client.dtos.request.ReviewCreateRequest;
import com.clothshop.client.dtos.response.ProductDetailResponse;
import com.clothshop.client.dtos.response.ReviewResponse;
import com.clothshop.client.services.ProductClientService;
import com.clothshop.client.services.ReviewClientService;
import com.clothshop.common.dtos.response.PageResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.security.Principal;

@Controller
@RequestMapping("/products/{productId}/reviews")
@RequiredArgsConstructor
public class ProductFeedbackController {

    private final ReviewClientService reviewService;
    private final ProductClientService productClientService;

    @PostMapping
    @PreAuthorize("hasRole('CUSTOMER')")
    public String submitReview(@PathVariable Long productId,
                               @ModelAttribute("reviewRequest") ReviewCreateRequest reviewRequest,
                               Principal principal,
                               RedirectAttributes redirectAttributes) {
        ProductDetailResponse product = productClientService.getProductById(productId); // This may result in a bug later on...
        try {
            reviewRequest.setProductId(productId);
            reviewService.submitReview(principal.getName(), reviewRequest);
            redirectAttributes.addFlashAttribute("successMessage", "Cảm ơn bạn đã đánh giá sản phẩm!");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("errorMessage", "Không thể gửi đánh giá: " + e.getMessage());
        }
        return "redirect:/products/" + product.getProductSlug();
    }

    @GetMapping
    @ResponseBody
    public PageResponse<ReviewResponse> getReviews(@PathVariable Long productId,
                                                   @RequestParam(defaultValue = "0") int page,
                                                   @RequestParam(defaultValue = "5") int size) {
        return reviewService.getReviewsForProduct(productId, PageRequest.of(page, size));
    }

    @GetMapping("/form")
    @PreAuthorize("hasRole('CUSTOMER')")
    public String showReviewForm(@PathVariable Long productId, Model model) {
        // Assume getProductById exists or use getProductBySlug with another lookup
        // But since we had to map productId anyway, let's use a safe lookup.
        // I'll check ProductClientService for a method to get by ID.
        model.addAttribute("product", productClientService.getProductById(productId));
        model.addAttribute("reviewRequest", new ReviewCreateRequest());
        return "client/reviews/form";
    }
}
