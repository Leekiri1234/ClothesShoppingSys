package com.clothshop.client.controllers;

import com.clothshop.client.dtos.request.ProductSearchRequest;
import com.clothshop.client.dtos.response.ProductListResponse;
import com.clothshop.client.services.ProductSearchService;
import com.clothshop.client.services.TryOnService;
import com.clothshop.client.services.CategoryClientService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MaxUploadSizeExceededException;
import org.springframework.web.multipart.MultipartFile;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

@Controller
@RequestMapping("/try-on")
@RequiredArgsConstructor
@Slf4j
public class TryOnController {

    private final ProductSearchService productSearchService;
    private final TryOnService tryOnService;
    private final CategoryClientService categoryClientService;

    @GetMapping
    public String showTryOnPage(
            @ModelAttribute ProductSearchRequest searchRequest,
            @RequestParam(required = false) Long categoryId,
            @RequestParam(required = false) Long productId,
            Model model) {

        if (categoryId != null) {
            searchRequest.setCategoryIds(Collections.singletonList(categoryId));
        }

        if (searchRequest.getSize() == null || searchRequest.getSize() < 1) {
            searchRequest.setSize(12);
        }

        Page<ProductListResponse> productPage = productSearchService.search(searchRequest);

        model.addAttribute("tryOnProducts", productPage.getContent());
        model.addAttribute("productPage", productPage);
        model.addAttribute("categories", categoryClientService.getCategoryTree());
        model.addAttribute("searchRequest", searchRequest);
        model.addAttribute("selectedCategoryId", categoryId);
        model.addAttribute("preselectedProductId", productId);
        model.addAttribute("pageTitle", "Virtual Try-On");
        return "client/try-on/try-on";
    }

    @PostMapping("/generate")
    @ResponseBody
    public ResponseEntity<Map<String, Object>> generateTryOn(
            @RequestParam("image") MultipartFile userImage,
            @RequestParam("productId") Long productId) {

        Map<String, Object> response = new HashMap<>();
        try {
            String generatedImageUrl = tryOnService.generateTryOnImage(userImage, productId);

            response.put("success", true);
            response.put("imageUrl", generatedImageUrl);
            return ResponseEntity.ok(response);

        } catch (IllegalArgumentException e) {
            // Validation errors (bad input from user)
            log.warn("Try-On validation error: {}", e.getMessage());
            response.put("success", false);
            response.put("message", e.getMessage());
            return ResponseEntity.badRequest().body(response);

        } catch (IllegalStateException e) {
            // Configuration/data errors (missing API key, product has no image)
            log.error("Try-On state error: {}", e.getMessage());
            response.put("success", false);
            response.put("message", e.getMessage());
            return ResponseEntity.unprocessableEntity().body(response);

        } catch (Exception e) {
            // AI/network errors
            log.error("Try-On generation error: ", e);
            response.put("success", false);
            response.put("message", e.getMessage() != null ? e.getMessage() : "Đã xảy ra lỗi không xác định. Vui lòng thử lại.");
            return ResponseEntity.internalServerError().body(response);
        }
    }

    /**
     * Handle file upload size exceeded globally for this controller.
     */
    @ExceptionHandler(MaxUploadSizeExceededException.class)
    @ResponseBody
    public ResponseEntity<Map<String, Object>> handleMaxUploadSize(MaxUploadSizeExceededException e) {
        log.warn("Upload size exceeded: {}", e.getMessage());
        Map<String, Object> response = new HashMap<>();
        response.put("success", false);
        response.put("message", "Ảnh quá lớn. Vui lòng sử dụng ảnh dưới 10MB.");
        return ResponseEntity.badRequest().body(response);
    }
}
