package com.clothshop.admin.controllers;

import com.clothshop.admin.dtos.request.order.OrderFilterRequest;
import com.clothshop.admin.dtos.response.order.OrderAdminResponse;
import com.clothshop.admin.services.OrderAdminService;
import com.clothshop.domain.enums.OrderStatus;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
@RequestMapping("/admin/orders")
@RequiredArgsConstructor
@PreAuthorize("hasAnyRole('SUPER_ADMIN', 'SALE_PRODUCT_STAFF')") // Bảo mật đa tầng
public class OrderAdminController {

    private final OrderAdminService orderService;

    /**
     * GET /admin/orders
     * Hiển thị danh sách đơn hàng kèm bộ lọc và phân trang.
     */
    @GetMapping
    public String listOrders(@ModelAttribute("filter") OrderFilterRequest filter,
                             @PageableDefault(size = 10, sort = "id", direction = org.springframework.data.domain.Sort.Direction.DESC) Pageable pageable,
                             Model model) {

        Page<OrderAdminResponse> orderPage = orderService.getOrders(filter, pageable);

        model.addAttribute("orders", orderPage);
        model.addAttribute("allStatuses", OrderStatus.values()); // Để đổ vào dropdown filter

        return "admin/orders/list";
    }

    /**
     * GET /admin/orders/{id}
     * Xem chi tiết 1 đơn hàng và dòng thời gian (Timeline).
     */
    @GetMapping("/{id}")
    public String orderDetail(@PathVariable Long id, Model model) {
        model.addAttribute("order", orderService.getOrderDetail(id));
        model.addAttribute("allStatuses", OrderStatus.values()); // Để hiện nút chuyển trạng thái

        return "admin/orders/detail";
    }

    /**
     * POST /admin/orders/{id}/status
     * Xử lý cập nhật trạng thái đơn hàng.
     */
    @PostMapping("/{id}/status")
    public String updateStatus(@PathVariable Long id,
                               @RequestParam OrderStatus newStatus,
                               @RequestParam(required = false) String note,
                               RedirectAttributes redirectAttributes) {
        try {
            orderService.updateOrderStatus(id, newStatus, note);
            redirectAttributes.addFlashAttribute("success", "Cập nhật trạng thái thành công!");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", "Có lỗi xảy ra: " + e.getMessage());
        }

        return "redirect:/admin/orders/" + id;
    }
}