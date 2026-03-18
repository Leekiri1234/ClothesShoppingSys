package com.clothshop.client.controllers;

import com.clothshop.client.dtos.request.OrderCreateRequest;
import com.clothshop.client.dtos.response.CheckoutSummaryResponse;
import com.clothshop.client.services.CartClientService;
import com.clothshop.client.services.CheckoutClientService;
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

    @GetMapping
    public String showCheckoutForm(Principal principal, Model model) {
        if (principal == null) return "redirect:/login";

        // Check nếu giỏ hàng rỗng thì đá về trang giỏ hàng
        if (cartService.getCartItemCount(principal.getName()) == 0) {
            return "redirect:/cart";
        }

        // Lấy thông tin user để fill sẵn vào form
        Account account = accountRepository.findByUsernameWithCustomer(principal.getName()).orElse(null);
        OrderCreateRequest request = new OrderCreateRequest();
        if (account != null && account.getCustomer() != null) {
            request.setShippingAddress(account.getCustomer().getAddress());
            request.setPhoneNumber(account.getCustomer().getPhoneNumber());
        }

        model.addAttribute("cart", cartService.getCartSummary(principal.getName()));
        model.addAttribute("orderRequest", request);
        model.addAttribute("paymentMethods", PaymentMethod.values());

        return "client/checkout/form";
    }

    // API tính toán Voucher dùng cho AJAX
    @PostMapping("/calculate")
    @ResponseBody
    public ResponseEntity<?> calculateTotal(@RequestParam(required = false) String voucherCode, Principal principal) {
        try {
            CheckoutSummaryResponse summary = checkoutService.calculateTotal(principal.getName(), voucherCode);
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
            model.addAttribute("cart", cartService.getCartSummary(principal.getName()));
            model.addAttribute("paymentMethods", PaymentMethod.values());
            return "client/checkout/form";
        }

        try {
            String orderInvoice = checkoutService.placeOrder(principal.getName(), request);
            redirectAttributes.addFlashAttribute("success", "Đặt hàng thành công! Đơn hàng của bạn đang chờ xác nhận.");
            return "redirect:/orders/" + orderInvoice;
        } catch (BusinessException e) {
            model.addAttribute("error", e.getMessage());
            model.addAttribute("cart", cartService.getCartSummary(principal.getName()));
            model.addAttribute("paymentMethods", PaymentMethod.values());
            return "client/checkout/form";
        }
    }
}

