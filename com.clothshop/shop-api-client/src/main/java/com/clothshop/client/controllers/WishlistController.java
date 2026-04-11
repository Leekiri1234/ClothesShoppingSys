package com.clothshop.client.controllers;

import com.clothshop.client.services.WishlistClientService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.security.Principal;
import java.util.Map;

@Controller
@RequestMapping("/wishlist")
@RequiredArgsConstructor
public class WishlistController {

    private final WishlistClientService wishlistService;


    // 1. Xem wishlist (Trả về View HTML)
    @GetMapping
    public String viewWishlist(Principal principal, Model model) {
        if (principal == null) {
            return "redirect:/login";
        }
        model.addAttribute("wishlist", wishlistService.getWishlistItems(principal.getName()));
        return "client/wishlist/list";
    }

    // 2. Toggle wishlist (AJAX) - Thêm hoặc xóa từ wishlist
    @PostMapping("/toggle/{productId}")
    @PreAuthorize("hasRole('CUSTOMER')")
    @ResponseBody
    public ResponseEntity<?> toggleWishlist(@PathVariable Long productId, Principal principal) {
        System.out.print("Principallll: " + principal.getName());
        boolean isAdded = wishlistService.toggleWishlist(principal.getName(), productId);
        return ResponseEntity.ok(Map.of(
                "success", true,
                "isAdded", isAdded,
                "message", isAdded ? "Đã thêm vào wishlist" : "Đã xóa khỏi wishlist"
        ));
    }

    // 3. Đếm số lượng wishlist (AJAX) - Hiện lên header badge
    @GetMapping("/count")
    @ResponseBody
    public ResponseEntity<?> getWishlistCount(Principal principal) {
        if (principal == null) return ResponseEntity.ok(Map.of("count", 0));
        int count = wishlistService.getWishlistCount(principal.getName());
        return ResponseEntity.ok(Map.of("count", count));
    }

    // 4. Xóa khỏi wishlist (AJAX)
    @PostMapping("/remove/{productId}")
    @PreAuthorize("hasRole('CUSTOMER')")
    @ResponseBody
    public ResponseEntity<?> removeFromWishlist(@PathVariable Long productId, Principal principal) {
        wishlistService.removeFromWishlist(principal.getName(), productId);
        return ResponseEntity.ok(Map.of("success", true, "message", "Đã xóa khỏi wishlist"));
    }

    // 5. Kiểm tra xem sản phẩm có trong wishlist không (AJAX)
    @GetMapping("/check/{productId}")
    @ResponseBody
    public ResponseEntity<?> checkProductInWishlist(@PathVariable Long productId, Principal principal) {
        if (principal == null) return ResponseEntity.ok(Map.of("inWishlist", false));
        boolean inWishlist = wishlistService.isProductInWishlist(principal.getName(), productId);
        return ResponseEntity.ok(Map.of("inWishlist", inWishlist));
    }

    // 6. Lấy danh sách ID sản phẩm trong wishlist (AJAX)
    @GetMapping("/ids")
    @ResponseBody
    public ResponseEntity<?> getWishlistIds(Principal principal) {
        if (principal == null) {
            return ResponseEntity.ok(java.util.Collections.emptyList());
        }
        return ResponseEntity.ok(wishlistService.getWishlistProductIds(principal.getName()));
    }

}
