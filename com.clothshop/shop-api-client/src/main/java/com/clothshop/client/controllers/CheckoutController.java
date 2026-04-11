package com.clothshop.client.controllers;

import com.clothshop.client.dtos.request.OrderCreateRequest;
import com.clothshop.client.dtos.response.CheckoutSummaryResponse;
import com.clothshop.client.services.CartClientService;
import com.clothshop.client.services.CheckoutClientService;
import com.clothshop.client.services.ClientVoucherService;
import com.clothshop.common.exceptions.BusinessException;
import com.clothshop.domain.models.auth.Account;
import com.clothshop.domain.enums.PaymentMethod;
import com.clothshop.domain.repositories.auth.AccountRepository;
import jakarta.servlet.http.HttpSession;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.security.Principal;
import java.util.Map;

@Controller
@RequestMapping("/checkout")
@RequiredArgsConstructor
@PreAuthorize("hasRole('CUSTOMER')")
public class CheckoutController {

    private final CheckoutClientService checkoutService;
    private final CartClientService cartService;
    private final AccountRepository accountRepository;
    private final ClientVoucherService clientVoucherService;

    @GetMapping
    public String showCheckoutForm(Principal principal, Model model) {
        if (principal == null) return "redirect:/login";

        Account account = accountRepository.findByUsernameWithCustomer(principal.getName()).orElse(null);
        OrderCreateRequest request = new OrderCreateRequest();

        if (account != null && account.getCustomer() != null) {
            request.setFullName(account.getCustomer().getFullName());
            request.setShippingAddress(account.getCustomer().getAddress());
            request.setPhoneNumber(account.getCustomer().getPhoneNumber());
            String email = account.getCustomer().getEmail();
            if (email == null || email.isBlank()) {
                email = account.getEmail();
            }
            request.setEmail(email);
        }

        model.addAttribute("isDirectPurchase", false); // ← THÊM
        model.addAttribute("cart", cartService.getCartSummary(principal.getName()));
        model.addAttribute("orderRequest", request);
        model.addAttribute("paymentMethods", PaymentMethod.values());
        model.addAttribute("vouchers", clientVoucherService.getAvailableVouchers());

        return "client/checkout/form";
    }

    @PostMapping("/calculate")
    @ResponseBody
    public ResponseEntity<?> calculateTotal(
            @RequestParam(required = false) String voucherCode,
            Principal principal) {
        try {
            CheckoutSummaryResponse summary = checkoutService.calculateTotal(
                    principal.getName(), voucherCode);
            return ResponseEntity.ok(summary);
        } catch (BusinessException e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }

    @PostMapping("/confirm")
    public String confirmOrder(
            @Valid @ModelAttribute("orderRequest") OrderCreateRequest request,
            BindingResult bindingResult,
            Principal principal,
            Model model,
            RedirectAttributes redirectAttributes) {

        if (bindingResult.hasErrors()) {
            model.addAttribute("isDirectPurchase", false); // ← THÊM
            model.addAttribute("cart", cartService.getCartSummary(principal.getName()));
            model.addAttribute("paymentMethods", PaymentMethod.values());
            return "client/checkout/form";
        }

        try {
            String orderInvoice = checkoutService.placeOrder(principal.getName(), request);
            redirectAttributes.addFlashAttribute("success",
                    "Đặt hàng thành công! Đơn hàng của bạn đang chờ xác nhận.");
            return "redirect:/orders/" + orderInvoice;
        } catch (BusinessException e) {
            model.addAttribute("isDirectPurchase", false); // ← THÊM
            model.addAttribute("error", e.getMessage());
            model.addAttribute("cart", cartService.getCartSummary(principal.getName()));
            model.addAttribute("paymentMethods", PaymentMethod.values());
            return "client/checkout/form";
        }
    }

    @PostMapping("/direct")
    @ResponseBody
    public ResponseEntity<?> initDirectCheckout(
            @RequestBody Map<String, Object> body,
            HttpSession session,
            Principal principal) {

        if (principal == null)
            return ResponseEntity.status(401).body(Map.of("success", false));

        try {
            int variantId = Integer.parseInt(body.get("variantId").toString());
            int quantity  = Integer.parseInt(body.get("quantity").toString());
            session.setAttribute("directPurchase", Map.of("variantId", variantId, "quantity", quantity));
            return ResponseEntity.ok(Map.of("success", true));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of("success", false, "message", e.getMessage()));
        }
    }

    @GetMapping("/direct")
    public String showDirectCheckoutForm(HttpSession session, Principal principal, Model model) {
        if (principal == null) return "redirect:/login";

        @SuppressWarnings("unchecked")
        Map<String, Object> dp = (Map<String, Object>) session.getAttribute("directPurchase");
        if (dp == null) return "redirect:/products";

        int variantId = (int) dp.get("variantId");
        int quantity  = (int) dp.get("quantity");

        Account account = accountRepository.findByUsernameWithCustomer(principal.getName()).orElse(null);
        OrderCreateRequest request = new OrderCreateRequest();
        if (account != null && account.getCustomer() != null) {
            request.setFullName(account.getCustomer().getFullName());
            request.setShippingAddress(account.getCustomer().getAddress());
            request.setPhoneNumber(account.getCustomer().getPhoneNumber());
            String email = account.getCustomer().getEmail();
            if (email == null || email.isBlank()) email = account.getEmail();
            request.setEmail(email);
        }

        model.addAttribute("isDirectPurchase", true); // ← THÊM
        model.addAttribute("directItem", checkoutService.getDirectPurchaseItem(variantId, quantity));
        model.addAttribute("orderRequest", request);
        model.addAttribute("paymentMethods", PaymentMethod.values());
        model.addAttribute("vouchers", clientVoucherService.getAvailableVouchers());
        return "client/checkout/form";
    }

    @PostMapping("/direct/confirm")
    public String confirmDirectOrder(
            @Valid @ModelAttribute("orderRequest") OrderCreateRequest request,
            BindingResult bindingResult,
            HttpSession session,
            Principal principal,
            Model model,
            RedirectAttributes redirectAttributes) {

        @SuppressWarnings("unchecked")
        Map<String, Object> dp = (Map<String, Object>) session.getAttribute("directPurchase");
        if (dp == null) return "redirect:/products";

        if (bindingResult.hasErrors()) {
            model.addAttribute("isDirectPurchase", true); // ← THÊM
            model.addAttribute("directItem",
                    checkoutService.getDirectPurchaseItem((int) dp.get("variantId"), (int) dp.get("quantity")));
            model.addAttribute("paymentMethods", PaymentMethod.values());
            return "client/checkout/form";
        }

        try {
            request.setDirectVariantId((int) dp.get("variantId"));
            request.setDirectQuantity((int) dp.get("quantity"));

            String invoice = checkoutService.placeDirectOrder(principal.getName(), request);
            session.removeAttribute("directPurchase");

            redirectAttributes.addFlashAttribute("success", "Đặt hàng thành công!");
            return "redirect:/orders/" + invoice;
        } catch (BusinessException e) {
            model.addAttribute("isDirectPurchase", true); // ← THÊM
            model.addAttribute("error", e.getMessage());
            model.addAttribute("directItem",
                    checkoutService.getDirectPurchaseItem((int) dp.get("variantId"), (int) dp.get("quantity")));
            model.addAttribute("paymentMethods", PaymentMethod.values());
            return "client/checkout/form";
        }
    }
}