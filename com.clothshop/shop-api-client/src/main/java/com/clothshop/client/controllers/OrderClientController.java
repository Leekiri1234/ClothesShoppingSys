package com.clothshop.client.controllers;

import com.clothshop.client.dtos.request.OrderCancellationRequest;
import com.clothshop.client.dtos.response.OrderDetailResponse;
import com.clothshop.client.dtos.response.OrderListClientResponse;
import com.clothshop.client.services.OrderClientService;
import com.clothshop.common.dtos.request.PagingRequest;
import com.clothshop.common.dtos.response.PageResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.security.Principal;

@Controller
@RequestMapping("/orders")
@RequiredArgsConstructor
@PreAuthorize("hasRole('CUSTOMER')")
public class OrderClientController {

    private final OrderClientService orderService;

    @GetMapping
    public String listMyOrders(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            Principal principal,
            Model model) {

        PagingRequest pagingRequest = PagingRequest.builder().pageNumber(page).pageSize(size).build();
        PageResponse<OrderListClientResponse> orders = orderService.getMyOrders(principal.getName(), pagingRequest);

        model.addAttribute("orders", orders);
        return "client/orders/list";
    }

    @GetMapping("/{orderInvoice}")
    public String viewOrderDetail(@PathVariable String orderInvoice, Principal principal, Model model) {
        OrderDetailResponse order = orderService.getOrderDetail(principal.getName(), orderInvoice);
        model.addAttribute("order", order);
        model.addAttribute("cancelRequest", new OrderCancellationRequest());
        return "client/orders/detail";
    }

    @PostMapping("/{orderInvoice}/cancel")
    public String cancelOrder(@PathVariable String orderInvoice,
                              @ModelAttribute("cancelRequest") OrderCancellationRequest cancelRequest,
                              Principal principal,
                              RedirectAttributes redirectAttributes) {
        try {
            orderService.cancelOrder(principal.getName(), orderInvoice, cancelRequest.getReason());
            redirectAttributes.addFlashAttribute("successMessage", "Đã hủy đơn hàng thành công.");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("errorMessage", "Không thể hủy đơn hàng: " + e.getMessage());
        }
        return "redirect:/orders/" + orderInvoice;
    }

    @PostMapping("/{orderInvoice}/reorder")
    public String reorderCancelled(@PathVariable String orderInvoice, Principal principal, RedirectAttributes redirectAttributes) {
        try {
            orderService.reorderCancelled(principal.getName(), orderInvoice);
            redirectAttributes.addFlashAttribute("successMessage", "Đã đặt lại đơn hàng. Trạng thái chuyển về Chờ xác nhận.");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("errorMessage", "Không thể đặt lại: " + e.getMessage());
        }
        return "redirect:/orders/" + orderInvoice;
    }
}