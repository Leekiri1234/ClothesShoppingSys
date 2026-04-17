package com.clothshop.admin.controllers;

import com.clothshop.admin.dtos.response.marketing.WishlistInsightsResponse;
import com.clothshop.admin.dtos.response.marketing.WishlistProductDetailResponse;
import com.clothshop.admin.services.WishlistInsightsService;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ContentDisposition;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.nio.charset.StandardCharsets;
import java.time.LocalDate;

@Controller
@RequestMapping("/admin/marketing/wishlist-insights")
@RequiredArgsConstructor
@PreAuthorize("hasAnyRole('SUPER_ADMIN', 'MARKETING_STAFF')")
public class WishlistInsightsController {

    private final WishlistInsightsService wishlistInsightsService;

    @GetMapping
    public String viewInsights(
            @RequestParam(value = "search", required = false) String search,
            @RequestParam(value = "startDate", required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
            @RequestParam(value = "endDate", required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate,
            @RequestParam(value = "categoryId", required = false) Long categoryId,
            @RequestParam(value = "productId", required = false) Long productId,
            Model model
    ) {
        WishlistInsightsResponse insights = wishlistInsightsService.getInsights(search, startDate, endDate, categoryId, productId);
        model.addAttribute("insights", insights);
        return "admin/marketing/wishlist-insights";
    }

    @GetMapping("/products/{productId}")
    public String viewProductDetail(
            @PathVariable Long productId,
            @RequestParam(value = "startDate", required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
            @RequestParam(value = "endDate", required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate,
            Model model
    ) {
        WishlistProductDetailResponse detail = wishlistInsightsService.getProductDetail(productId, startDate, endDate);
        model.addAttribute("detail", detail);
        return "admin/marketing/wishlist-product-detail";
    }

    @GetMapping("/export")
    public ResponseEntity<byte[]> exportCsv(
            @RequestParam(value = "search", required = false) String search,
            @RequestParam(value = "startDate", required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
            @RequestParam(value = "endDate", required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate,
            @RequestParam(value = "categoryId", required = false) Long categoryId,
            @RequestParam(value = "productId", required = false) Long productId
    ) {
        String csv = wishlistInsightsService.exportTopProductsCsv(search, startDate, endDate, categoryId, productId);
        byte[] payload = csv.getBytes(StandardCharsets.UTF_8);

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(new MediaType("text", "csv"));
        headers.setContentDisposition(ContentDisposition.attachment().filename("wishlist-insights.csv").build());

        return ResponseEntity.ok().headers(headers).body(payload);
    }
}
