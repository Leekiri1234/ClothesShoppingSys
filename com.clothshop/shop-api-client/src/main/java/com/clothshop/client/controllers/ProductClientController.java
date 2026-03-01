package com.clothshop.client.controllers;

import com.clothshop.client.dtos.response.ProductDetailResponse;
import com.clothshop.client.dtos.response.ProductListResponse;
import com.clothshop.client.services.ProductClientService;
import com.clothshop.common.dtos.request.PagingRequest;
import com.clothshop.common.dtos.response.PageResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

@Controller
@RequestMapping("/products")
@RequiredArgsConstructor
@Slf4j
public class ProductClientController {

    private final ProductClientService productClientService;

    /**
     * Danh sách sản phẩm chung.
     */
    @GetMapping
    public String listProducts(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size,
            @RequestParam(defaultValue = "createdAt") String sortBy,
            @RequestParam(defaultValue = "DESC") String direction,
            Model model) {

        // Build PagingRequest khớp với các field mới em đã sửa
        PagingRequest pagingRequest = PagingRequest.builder()
                .pageNumber(page)
                .pageSize(size)
                .sortBy(sortBy)
                .sortDirection(direction)
                .build();

        PageResponse<ProductListResponse> products = productClientService.getAllActiveProducts(pagingRequest);

        model.addAttribute("products", products);
        model.addAttribute("pageTitle", "Tất cả sản phẩm");

        return "client/products/list";
    }

    /**
     * Chi tiết sản phẩm qua Slug (SEO Friendly).
     */
    @GetMapping("/{slug}")
    public String productDetail(@PathVariable String slug, Model model) {
        ProductDetailResponse product = productClientService.getProductBySlug(slug);

        model.addAttribute("product", product);
        model.addAttribute("pageTitle", product.getProductName());

        return "client/products/detail";
    }

    /**
     * Lọc sản phẩm theo Category.
     */
    @GetMapping("/category/{categorySlug}")
    public String productsByCategory(
            @PathVariable String categorySlug,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size,
            Model model) {

        PagingRequest pagingRequest = PagingRequest.builder()
                .pageNumber(page)
                .pageSize(size)
                .sortBy("createdAt")
                .sortDirection("DESC")
                .build();

        PageResponse<ProductListResponse> products =
                productClientService.getProductsByCategory(categorySlug, pagingRequest);

        model.addAttribute("products", products);
        model.addAttribute("categorySlug", categorySlug);
        model.addAttribute("pageTitle", "Danh mục: " + categorySlug);

        return "client/products/category";
    }

    /**
     * Tìm kiếm sản phẩm.
     */
    @GetMapping("/search")
    public String searchProducts(
            @RequestParam(name = "q") String keyword,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size,
            Model model) {

        PagingRequest pagingRequest = PagingRequest.builder()
                .pageNumber(page)
                .pageSize(size)
                .sortBy("createdAt")
                .sortDirection("DESC")
                .build();

        PageResponse<ProductListResponse> products =
                productClientService.searchProducts(keyword, pagingRequest);

        model.addAttribute("products", products);
        model.addAttribute("keyword", keyword);
        model.addAttribute("pageTitle", "Kết quả tìm kiếm: " + keyword);

        return "client/products/search";
    }
}