package com.clothshop.client.controllers;

import com.clothshop.client.dtos.request.AddToCartRequest;
import com.clothshop.client.dtos.response.CartSummaryResponse;
import com.clothshop.client.services.CartClientService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.security.Principal;
import java.util.Map;

@Controller
@RequestMapping("/cart")
@RequiredArgsConstructor
public class CartController {

    private final CartClientService cartService;

    // 1. Xem giỏ hàng (Trả về View HTML)
    @GetMapping
    public String viewCart(Principal principal, Model model) {
        if (principal == null) {
            return "redirect:/login";
        }
        model.addAttribute("cart", cartService.getCartSummary(principal.getName()));
        model.addAttribute("cartCount", cartService.getCartItemCount(principal.getName()));
        return "client/cart/view";
    }

    // 2. Thêm vào giỏ (AJAX)
    @PostMapping("/add")
    @PreAuthorize("hasRole('CUSTOMER')")
    @ResponseBody
    public ResponseEntity<?> addToCart(@RequestBody AddToCartRequest request, Principal principal) {
        cartService.addToCart(principal.getName(), request);
        return ResponseEntity.ok(Map.of("success", true, "message", "Đã thêm vào giỏ hàng"));
    }

    // 3. Cập nhật số lượng (AJAX từ sidebar - không redirect)
    @PostMapping("/update/{itemId}")
    @PreAuthorize("hasRole('CUSTOMER')")
    @ResponseBody
    public ResponseEntity<?> updateQuantity(
            @PathVariable Long itemId,
            @RequestParam Integer quantity,
            Principal principal) {
        cartService.updateQuantity(principal.getName(), itemId, quantity);
        return ResponseEntity.ok(Map.of("success", true));
    }

    // 4. Xóa khỏi giỏ (AJAX từ sidebar - không redirect)
    @PostMapping("/remove/{itemId}")
    @PreAuthorize("hasRole('CUSTOMER')")
    @ResponseBody
    public ResponseEntity<?> removeFromCart(@PathVariable Long itemId, Principal principal) {
        cartService.removeFromCart(principal.getName(), itemId);
        return ResponseEntity.ok(Map.of("success", true));
    }

    // 5. Đếm số lượng (AJAX - Badge trên header)
    @GetMapping("/count")
    @ResponseBody
    public ResponseEntity<?> getCartCount(Principal principal) {
        if (principal == null) return ResponseEntity.ok(Map.of("count", 0));
        int count = cartService.getCartItemCount(principal.getName());
        return ResponseEntity.ok(Map.of("count", count));
    }

    // 6. Lấy toàn bộ thông tin giỏ hàng cho sidebar (AJAX)
    @GetMapping("/summary")
    @ResponseBody
    public ResponseEntity<?> getCartSummary(Principal principal) {
        if (principal == null) {
            return ResponseEntity.ok(Map.of("items", java.util.List.of(), "totalItems", 0, "totalAmount", 0));
        }
        CartSummaryResponse summary = cartService.getCartSummary(principal.getName());
        return ResponseEntity.ok(summary);
    }
}