package com.clothshop.client.controllers;

import com.clothshop.client.dtos.request.OrderCreateRequest;
import com.clothshop.client.dtos.response.CheckoutSummaryResponse;
import com.clothshop.client.services.CartClientService;
import com.clothshop.client.services.CheckoutClientService;
import com.clothshop.client.services.ClientVoucherService;
import com.clothshop.common.exceptions.BusinessException;
import com.clothshop.domain.entities.auth.Account;
import com.clothshop.domain.enums.PaymentMethod;
import com.clothshop.domain.repositories.auth.AccountRepository;
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

       if (cartService.getCartItemCount(principal.getName()) == 0) {
            return "redirect:/cart";
        }

        Account account = accountRepository.findByUsernameWithCustomer(principal.getName()).orElse(null);
        OrderCreateRequest request = new OrderCreateRequest();

        // Pre-fill từ thông tin tài khoản
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

        model.addAttribute("cart", cartService.getCartSummary(principal.getName()));
        model.addAttribute("orderRequest", request);
        model.addAttribute("paymentMethods", PaymentMethod.values());
        model.addAttribute("vouchers", clientVoucherService.getAvailableVouchers());

        return "client/checkout/form";
    }

    /**
     * API tính toán Voucher và phí ship — dùng cho AJAX từ trang checkout.
     */
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

    /**
     * Xử lý đặt hàng — POST từ form checkout.
     */
    @PostMapping("/confirm")
    public String confirmOrder(
            @Valid @ModelAttribute("orderRequest") OrderCreateRequest request,
            BindingResult bindingResult,
            Principal principal,
            Model model,
            RedirectAttributes redirectAttributes) {

        if (bindingResult.hasErrors()) {
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
            model.addAttribute("error", e.getMessage());
            model.addAttribute("cart", cartService.getCartSummary(principal.getName()));
            model.addAttribute("paymentMethods", PaymentMethod.values());
            return "client/checkout/form";
        }
    }
}