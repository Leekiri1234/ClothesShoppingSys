package com.clothshop.client.controllers;

import com.clothshop.client.dtos.response.CollectionResponse;
import com.clothshop.client.dtos.response.ProductListResponse;
import com.clothshop.client.services.CollectionClientService;
import com.clothshop.client.services.ProductClientService;
import com.clothshop.client.services.BannerClientService;
import com.clothshop.domain.entities.cms.Banner;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

import java.util.List;

/**
 * Controller for Client Home page.
 * Handles public-facing home page display.
 */
@Controller
@RequiredArgsConstructor
public class HomeController {

    private final ProductClientService productClientService;
    private final CollectionClientService collectionClientService; // Thêm service này
    private final BannerClientService bannerClientService;

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
}