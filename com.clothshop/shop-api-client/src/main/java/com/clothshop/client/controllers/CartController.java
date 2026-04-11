package com.clothshop.client.controllers;

import com.clothshop.client.dtos.request.AddToCartRequest;
import com.clothshop.client.dtos.response.CartSummaryResponse;
import com.clothshop.client.services.CartClientService;
import com.clothshop.client.services.CheckoutClientService;
import jakarta.servlet.http.HttpSession;
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
    private final CheckoutClientService checkoutService; // thêm dòng này

    // 1. Xem giỏ hàng
    @GetMapping
    public String viewCart(Principal principal, Model model) {
        if (principal == null) return "redirect:/login";
        model.addAttribute("cart", cartService.getCartSummary(principal.getName()));
        model.addAttribute("cartCount", cartService.getCartItemCount(principal.getName()));
        return "client/cart/view";
    }

    // 2. Xem giỏ hàng mua ngay (Direct Purchase)
    @GetMapping("/direct")
    public String viewDirectCart(Principal principal, HttpSession session, Model model) {
        if (principal == null) return "redirect:/login";

        @SuppressWarnings("unchecked")
        Map<String, Object> dp = (Map<String, Object>) session.getAttribute("directPurchase");
        if (dp == null) return "redirect:/products";

        model.addAttribute("directItem",
                checkoutService.getDirectPurchaseItem((int) dp.get("variantId"), (int) dp.get("quantity")));
        return "client/cart/direct";
    }

    // 3. Thêm vào giỏ (AJAX)
    @PostMapping("/add")
    @PreAuthorize("hasRole('CUSTOMER')")
    @ResponseBody
    public ResponseEntity<?> addToCart(@RequestBody AddToCartRequest request, Principal principal) {
        cartService.addToCart(principal.getName(), request);
        return ResponseEntity.ok(Map.of("success", true, "message", "Đã thêm vào giỏ hàng"));
    }

    // 4. Cập nhật số lượng (AJAX)
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

    // 5. Xóa khỏi giỏ (AJAX)
    @PostMapping("/remove/{itemId}")
    @PreAuthorize("hasRole('CUSTOMER')")
    @ResponseBody
    public ResponseEntity<?> removeFromCart(@PathVariable Long itemId, Principal principal) {
        cartService.removeFromCart(principal.getName(), itemId);
        return ResponseEntity.ok(Map.of("success", true));
    }

    // 6. Đếm số lượng (AJAX - Badge header)
    @GetMapping("/count")
    @ResponseBody
    public ResponseEntity<?> getCartCount(Principal principal) {
        if (principal == null) return ResponseEntity.ok(Map.of("count", 0));
        return ResponseEntity.ok(Map.of("count", cartService.getCartItemCount(principal.getName())));
    }

    // 7. Lấy toàn bộ thông tin giỏ hàng (AJAX - sidebar)
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