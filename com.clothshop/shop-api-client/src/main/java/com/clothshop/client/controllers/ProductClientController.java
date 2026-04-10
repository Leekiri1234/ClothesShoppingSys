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
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.security.Principal;
import java.util.List;
import java.util.Collections;

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
        return categoryClientService.getCategoryTree();
    }

    @GetMapping
    public String listProducts(
            @ModelAttribute ProductSearchRequest searchRequest,
            @RequestParam(required = false, name = "categoryId") Long categoryId,
            Model model) {

        // Backward compatibility for single categoryId
        if (categoryId != null && (searchRequest.getCategoryIds() == null || searchRequest.getCategoryIds().isEmpty())) {
            searchRequest.setCategoryIds(Collections.singletonList(categoryId));
        }

        Page<ProductListResponse> productPage = productSearchService.search(searchRequest);

        model.addAttribute("products", productPage);
        model.addAttribute("keyword", searchRequest.getKeyword());
        model.addAttribute("currentCategoryIds", searchRequest.getCategoryIds());
        model.addAttribute("searchRequest", searchRequest);
        model.addAttribute("pageTitle", resolvePageTitle(searchRequest.getKeyword(), categoryId));

        return "client/products/list";
    }

    @GetMapping("/filter-ajax")
    public String listProductsAjax(
            @ModelAttribute ProductSearchRequest searchRequest,
            Model model) {

        Page<ProductListResponse> productPage = productSearchService.search(searchRequest);

        model.addAttribute("products", productPage);
        return "client/products/list :: product-grid-fragment";
    }

    @GetMapping("/category/{id}")
    public String productsByCategoryId(@PathVariable Long id) {
        return "redirect:/products?categoryIds=" + id;
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

    // Quick View Modal Endpoint (AJAX) - Returns JSON for modal popup
    @GetMapping("/{slug}/quick-view")
    @ResponseBody
    public ResponseEntity<?> quickView(@PathVariable String slug) {
        ProductDetailResponse product = productClientService.getProductBySlug(slug);
        return ResponseEntity.ok(product);
    }

    private String resolvePageTitle(String keyword, Long categoryId) {
        if (keyword != null && !keyword.isBlank()) return "Kết quả tìm kiếm: " + keyword;
        if (categoryId != null) return "Danh mục sản phẩm";
        return "Tất cả sản phẩm";
    }
}