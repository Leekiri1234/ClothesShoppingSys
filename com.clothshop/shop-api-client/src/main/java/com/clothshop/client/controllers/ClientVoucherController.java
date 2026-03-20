package com.clothshop.client.controllers;

import com.clothshop.client.dtos.response.VoucherClientResponse;
import com.clothshop.client.services.ClientVoucherService;
import com.clothshop.common.exceptions.BusinessException;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.util.List;

@Controller
@RequestMapping("/vouchers")
@RequiredArgsConstructor
@PreAuthorize("hasRole('CUSTOMER')")
public class ClientVoucherController {

    private final ClientVoucherService clientVoucherService;

    /**
     * TRANG KHO VOUCHER (Inventory)
     * Hiển thị tất cả voucher đang khả dụng cho khách hàng
     */
    @GetMapping
    public String showVoucherInventory(Model model) {
        List<VoucherClientResponse> availableVouchers = clientVoucherService.getAvailableVouchers();
        model.addAttribute("vouchers", availableVouchers);
        return "client/vouchers/inventory";
    }

    /**
     * API CHECK VOUCHER (Dùng cho AJAX ở trang Checkout)
     * Nhận vào mã code và tổng tiền giỏ hàng, trả về thông tin giảm giá nếu hợp lệ
     */
    @GetMapping("/api/validate")
    @ResponseBody // Để trả về JSON thay vì tìm file HTML
    public ResponseEntity<?> validateVoucher(
            @RequestParam String code,
            @RequestParam BigDecimal cartTotal) {
        try {
            VoucherClientResponse voucher = clientVoucherService.validateVoucher(code, cartTotal);
            return ResponseEntity.ok(voucher);
        } catch (BusinessException e) {
            // Trả về lỗi kèm message để hiển thị lên UI
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }
}