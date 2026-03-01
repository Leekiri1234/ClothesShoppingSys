package com.clothshop.client.controllers;

import com.clothshop.client.dtos.response.ProductListResponse;
import com.clothshop.client.services.ProductClientService;
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

    /**
     * Display home page with featured products.
     *
     * @param model Thymeleaf model
     * @return home view
     */
    @GetMapping("/")
    public String home(Model model) {
        // Fetch featured products (limit to 8)
        List<ProductListResponse> featuredProducts = productClientService.getFeaturedProducts(8);
        model.addAttribute("featuredProducts", featuredProducts);

        return "client/home";
    }
}
