package com.clothshop.client.controllers;

import com.clothshop.client.dtos.request.ProductSearchRequest;
import com.clothshop.client.dtos.response.CategoryResponse;
import com.clothshop.client.dtos.response.ProductDetailResponse;
import com.clothshop.client.dtos.response.ProductListResponse;
import com.clothshop.client.services.CategoryClientService;
import com.clothshop.client.services.ProductClientService;
import com.clothshop.client.services.ProductSearchService;
import com.clothshop.client.services.ReviewClientService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.security.Principal;
import java.util.List;

@Controller
@RequestMapping("/products")
@RequiredArgsConstructor
@Slf4j
public class ProductClientController {

    private final ProductClientService productClientService;
    private final ProductSearchService productSearchService;
    private final CategoryClientService categoryClientService;
    private final ReviewClientService reviewService;

    @ModelAttribute("allCategories")
    public List<CategoryResponse> populateCategories() {
        return categoryClientService.getAllActiveCategories();
    }

    @GetMapping
    public String listProducts(
            @RequestParam(required = false) String q,
            @RequestParam(required = false) Long categoryId,
            @RequestParam(required = false) String collection,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "12") int size,
            Model model) {

        ProductSearchRequest searchRequest = ProductSearchRequest.builder()
                .keyword(q != null && !q.isBlank() ? q.trim() : null)
                .categoryId(categoryId)
                .collectionSlug(collection)
                .page(page)
                .size(size)
                .build();

        Page<ProductListResponse> productPage = productSearchService.search(searchRequest);

        model.addAttribute("products", productPage);
        model.addAttribute("keyword", q);
        model.addAttribute("currentCategoryId", categoryId);
        model.addAttribute("pageTitle", resolvePageTitle(q, categoryId));

        return "client/products/list";
    }

    @GetMapping("/category/{id}")
    public String productsByCategoryId(@PathVariable Long id) {
        return "redirect:/products?categoryId=" + id;
    }

    // Trang chi tiết sản phẩm (Giữ nguyên vì đã chuẩn)
    @GetMapping("/{slug}")
    public String productDetail(@PathVariable String slug, Model model, Principal principal) {
        ProductDetailResponse product = productClientService.getProductBySlug(slug);
        model.addAttribute("product", product);
        model.addAttribute("pageTitle", product.getProductName());

        // Review logic
        if (principal != null) {
            model.addAttribute("canReview", reviewService.canReview(principal.getName(), product.getProductId()));
            log.debug("User {} viewing product {}", principal.getName(), product.getProductId());
        } else {
            model.addAttribute("canReview", false);
        }

        model.addAttribute("reviews", reviewService.getReviewsForProduct(product.getProductId(), PageRequest.of(0, 5)));

        return "client/products/detail";
    }

    private String resolvePageTitle(String q, Long categoryId) {
        if (q != null && !q.isBlank()) return "Kết quả tìm kiếm: " + q;
        if (categoryId != null) return "Danh mục sản phẩm";
        return "Tất cả sản phẩm";
    }
}