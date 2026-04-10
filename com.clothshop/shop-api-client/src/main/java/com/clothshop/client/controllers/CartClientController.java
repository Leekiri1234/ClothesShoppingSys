package com.clothshop.client.controllers;

import com.clothshop.client.dtos.request.AddToCartRequest;
import com.clothshop.client.dtos.response.CartSummaryResponse;
import com.clothshop.client.services.CartClientService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

/**
 * CartClientController
 * Đặt tại: com.clothshop.client.controllers
 *
 * Endpoints:
 *   POST /api/cart/add    — thêm variant vào giỏ
 *   GET  /api/cart/count  — trả số lượng item trong giỏ (cho badge)
 *   GET  /api/cart        — trả CartSummaryResponse (nếu cần)
 */
@RestController
@RequestMapping("/api/cart")
@RequiredArgsConstructor
@Slf4j
public class CartClientController {

    private final CartClientService cartClientService;

    /* ──────────────────────────────────────────────
       POST /api/cart/add
       Body: { "variantId": 123, "quantity": 2 }
    ────────────────────────────────────────────── */
    @PostMapping("/add")
    public ResponseEntity<?> addToCart(
            @AuthenticationPrincipal UserDetails userDetails,
            @RequestBody AddToCartRequest request) {

        if (userDetails == null) {
            return ResponseEntity.status(401)
                    .body(Map.of("message", "Vui lòng đăng nhập để thêm vào giỏ hàng."));
        }

        if (request.getVariantId() == null) {
            return ResponseEntity.badRequest()
                    .body(Map.of("message", "Thiếu thông tin sản phẩm."));
        }

        if (request.getQuantity() == null || request.getQuantity() < 1) {
            request.setQuantity(1);
        }

        try {
            cartClientService.addToCart(userDetails.getUsername(), request);

            int newCount = cartClientService.getCartItemCount(userDetails.getUsername());

            return ResponseEntity.ok(Map.of(
                    "success", true,
                    "message", "Đã thêm vào giỏ hàng.",
                    "cartCount", newCount
            ));

        } catch (com.clothshop.common.exceptions.BusinessException ex) {
            return ResponseEntity.badRequest()
                    .body(Map.of("message", ex.getMessage()));
        } catch (Exception ex) {
            log.error("Error adding to cart for user {}: {}", userDetails.getUsername(), ex.getMessage(), ex);
            return ResponseEntity.internalServerError()
                    .body(Map.of("message", "Có lỗi xảy ra, vui lòng thử lại."));
        }
    }

    /* ──────────────────────────────────────────────
       GET /api/cart/count
       Trả số lượng item để cập nhật badge
    ────────────────────────────────────────────── */
    @GetMapping("/count")
    public ResponseEntity<?> getCartCount(
            @AuthenticationPrincipal UserDetails userDetails) {

        if (userDetails == null) {
            return ResponseEntity.ok(Map.of("count", 0));
        }

        try {
            int count = cartClientService.getCartItemCount(userDetails.getUsername());
            return ResponseEntity.ok(Map.of("count", count));
        } catch (Exception ex) {
            return ResponseEntity.ok(Map.of("count", 0));
        }
    }

    /* ──────────────────────────────────────────────
       GET /api/cart
       Trả CartSummaryResponse đầy đủ
    ────────────────────────────────────────────── */
    @GetMapping
    public ResponseEntity<?> getCart(
            @AuthenticationPrincipal UserDetails userDetails) {

        if (userDetails == null) {
            return ResponseEntity.status(401)
                    .body(Map.of("message", "Chưa đăng nhập."));
        }

        try {
            CartSummaryResponse summary = cartClientService.getCartSummary(userDetails.getUsername());
            return ResponseEntity.ok(summary);
        } catch (Exception ex) {
            log.error("Error fetching cart: {}", ex.getMessage(), ex);
            return ResponseEntity.internalServerError()
                    .body(Map.of("message", "Có lỗi xảy ra."));
        }
    }
}