package com.clothshop.client.controllers;

import com.clothshop.client.dtos.request.AddToCartRequest;
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
            return "redirect:/login"; // Chưa đăng nhập thì đá về trang login
        }
        model.addAttribute("cart", cartService.getCartSummary(principal.getName()));
        return "client/cart/view";
    }

    // 2. Thêm vào giỏ (Dành cho AJAX từ file main.js)
    @PostMapping("/add")
    @PreAuthorize("hasRole('CUSTOMER')")
    @ResponseBody
    public ResponseEntity<?> addToCart(@RequestBody AddToCartRequest request, Principal principal) {
        cartService.addToCart(principal.getName(), request);
        return ResponseEntity.ok(Map.of("success", true, "message", "Đã thêm vào giỏ hàng"));
    }

    // 3. Cập nhật số lượng (Dành cho AJAX hoặc form submit)
    @PostMapping("/update/{itemId}")
    @PreAuthorize("hasRole('CUSTOMER')")
    public String updateQuantity(
            @PathVariable Long itemId,
            @RequestParam Integer quantity,
            Principal principal) {
        cartService.updateQuantity(principal.getName(), itemId, quantity);
        return "redirect:/cart";
    }

    // 4. Xóa khỏi giỏ
    @PostMapping("/remove/{itemId}")
    @PreAuthorize("hasRole('CUSTOMER')")
    public String removeFromCart(@PathVariable Long itemId, Principal principal) {
        cartService.removeFromCart(principal.getName(), itemId);
        return "redirect:/cart";
    }

    // 5. Đếm số lượng để hiện lên Badge Header (AJAX)
    @GetMapping("/count")
    @ResponseBody
    public ResponseEntity<?> getCartCount(Principal principal) {
        if (principal == null) return ResponseEntity.ok(Map.of("count", 0)); // Thêm dòng này
        int count = cartService.getCartItemCount(principal.getName());
        return ResponseEntity.ok(Map.of("count", count));
    }
}