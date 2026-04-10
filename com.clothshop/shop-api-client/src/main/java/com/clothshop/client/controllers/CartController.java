package com.clothshop.client.controllers;

import com.clothshop.client.dtos.request.AddToCartRequest;
import com.clothshop.client.dtos.response.CartSummaryResponse;
import com.clothshop.client.services.CartClientService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.security.Principal;
import java.util.Map;

@Controller
@RequestMapping("/cart")
@RequiredArgsConstructor
@Slf4j
public class CartController {

    private final CartClientService cartService;

    // ── 1. Xem giỏ hàng ──────────────────────────────────────────
    @GetMapping
    public String viewCart(Principal principal, Model model) {
        if (principal == null) return "redirect:/login";

        CartSummaryResponse cart = cartService.getCartSummary(principal.getName());
        model.addAttribute("cart", cart);
        model.addAttribute("cartCount", cartService.getCartItemCount(principal.getName()));
        return "client/cart/view";
    }

    // ── 2. Cập nhật số lượng (form POST → redirect) ───────────────
    // cart.html dùng <form method="post"> bình thường → phải redirect, KHÔNG dùng @ResponseBody
    @PostMapping("/update/{itemId}")
    public String updateQuantity(
            @PathVariable Long itemId,
            @RequestParam Integer quantity,
            Principal principal,
            RedirectAttributes redirectAttributes) {

        if (principal == null) return "redirect:/login";

        try {
            cartService.updateQuantity(principal.getName(), itemId, quantity);
        } catch (Exception ex) {
            redirectAttributes.addFlashAttribute("cartError", ex.getMessage());
        }
        return "redirect:/cart";
    }

    // ── 3. Xóa khỏi giỏ (form POST → redirect) ───────────────────
    @PostMapping("/remove/{itemId}")
    public String removeFromCart(
            @PathVariable Long itemId,
            Principal principal,
            RedirectAttributes redirectAttributes) {

        if (principal == null) return "redirect:/login";

        try {
            cartService.removeFromCart(principal.getName(), itemId);
        } catch (Exception ex) {
            redirectAttributes.addFlashAttribute("cartError", ex.getMessage());
        }
        return "redirect:/cart";
    }

    // ── 4. Đếm badge (AJAX) ───────────────────────────────────────
    @GetMapping("/count")
    @ResponseBody
    public ResponseEntity<?> getCartCount(Principal principal) {
        if (principal == null) return ResponseEntity.ok(Map.of("count", 0));
        int count = cartService.getCartItemCount(principal.getName());
        return ResponseEntity.ok(Map.of("count", count));
    }

    // ── 5. Summary cho sidebar (AJAX) ────────────────────────────
    @GetMapping("/summary")
    @ResponseBody
    public ResponseEntity<?> getCartSummary(Principal principal) {
        if (principal == null) {
            return ResponseEntity.ok(Map.of(
                    "items", java.util.List.of(),
                    "totalItems", 0,
                    "totalAmount", 0
            ));
        }
        CartSummaryResponse summary = cartService.getCartSummary(principal.getName());
        return ResponseEntity.ok(summary);
    }
}