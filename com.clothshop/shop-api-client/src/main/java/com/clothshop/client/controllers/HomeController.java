package com.clothshop.client.controllers;

import com.clothshop.client.dtos.response.CollectionResponse;
import com.clothshop.client.dtos.response.ProductListResponse;
import com.clothshop.client.dtos.response.RmaListResponse;
import com.clothshop.client.services.BannerClientService;
import com.clothshop.client.services.CollectionClientService;
import com.clothshop.client.services.ProductClientService;
import com.clothshop.client.services.RmaClientService;
import com.clothshop.domain.models.cms.Banner;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.security.Principal;
import java.util.List;

/**
 * Controller for Client Home page.
 * Handles public-facing home page display and user RMA management.
 */
@Controller
@RequiredArgsConstructor
public class HomeController {

    private final ProductClientService productClientService;
    private final CollectionClientService collectionClientService;
    private final BannerClientService bannerClientService;
    private final RmaClientService rmaClientService;

    /**
     * @param model Thymeleaf model
     * @return home view
     */
    @GetMapping("/")
    public String home(Model model) {
        // 1. Fetch featured products (limit to 8)
        List<ProductListResponse> featuredProducts = productClientService.getFeaturedProducts(8);
        model.addAttribute("featuredProducts", featuredProducts);

        // 2. Fetch all active collections để hiển thị ở mục "Shop by Collection"
        List<CollectionResponse> homeCollections = collectionClientService.getAllActiveCollections();
        model.addAttribute("homeCollections", homeCollections);

        // 3. Fetch active banners
        List<Banner> banners = bannerClientService.getActiveBanners();
        model.addAttribute("banners", banners);

        return "client/home/home";
    }

    /**
     * Footer endpoints
     * */
    @GetMapping("/size-guide")
    public String sizeGuide() {
        return "client/pages/size-guide";
    }

    @GetMapping("/return-policy")
    public String returnPolicy() {
        return "client/pages/return-policy";
    }

    @GetMapping("/shipping")
    public String shipping() {
        return "client/pages/shipping";
    }

    /**
     * Get list of RMA requests for the current user
     */
    @GetMapping("/rma-management")
    @PreAuthorize("hasRole('CUSTOMER')")
    public String listMyRmaRequests(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            Principal principal,
            Model model) {

        Page<RmaListResponse> rmaRequests = rmaClientService.getMyRmaRequests(principal.getName(), page, size);
        model.addAttribute("rmaRequests", rmaRequests);
        return "client/rma/management";
    }

    /**
     * Get detail of an RMA request
     */
    @GetMapping("/rma-management/{rmaId}")
    @PreAuthorize("hasRole('CUSTOMER')")
    public String viewRmaRequestDetail(@PathVariable Long rmaId, Principal principal, Model model) {
        RmaListResponse rmaRequest = rmaClientService.getRmaRequestDetail(principal.getName(), rmaId);
        model.addAttribute("rmaRequest", rmaRequest);
        return "client/rma/detail";
    }

    /**
     * Cancel an RMA request (only if status is PENDING)
     */
    @PostMapping("/rma-management/{rmaId}/cancel")
    @PreAuthorize("hasRole('CUSTOMER')")
    public String cancelRmaRequest(@PathVariable Long rmaId, Principal principal, RedirectAttributes redirectAttributes) {
        try {
            rmaClientService.cancelRmaRequest(principal.getName(), rmaId);
            redirectAttributes.addFlashAttribute("successMessage", "Đã hủy yêu cầu đổi trả thành công.");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("errorMessage", "Không thể hủy yêu cầu: " + e.getMessage());
        }
        return "redirect:/rma-management";
    }
}