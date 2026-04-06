package com.clothshop.admin.controllers;

import com.clothshop.admin.dtos.request.order.RmaStatusUpdateRequest;
import com.clothshop.admin.services.RmaAdminService;
import com.clothshop.common.dtos.request.PagingRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
@RequestMapping("/admin/rma")
@RequiredArgsConstructor
@Slf4j
@PreAuthorize("hasAnyRole('SUPER_ADMIN','CUSTOMER_SERVICE')")
public class RmaAdminController {

    private final RmaAdminService rmaService;

    /**
     * Hiển thị danh sách yêu cầu RMA
     * URL: GET /admin/rma?pageNumber=0&pageSize=10
     */
    @GetMapping
    public String listRmaRequests(PagingRequest pagingRequest, Model model) {
        model.addAttribute("rmaPage", rmaService.getAllRmaRequests(pagingRequest));
        // Thêm pagingRequest vào model để giữ trạng thái sort/size trên giao diện
        model.addAttribute("pagingRequest", pagingRequest);
        return "admin/rma/list";
    }

    /**
     * Xem chi tiết một yêu cầu RMA
     * URL: GET /admin/rma/1
     */
    @GetMapping("/{id}")
    public String viewRmaDetail(@PathVariable Long id, Model model) {
        model.addAttribute("rma", rmaService.getRmaById(id));
        return "admin/rma/detail";
    }

    /**
     * Xử lý cập nhật trạng thái RMA (Approve, Reject, Received, Completed)
     * URL: POST /admin/rma/1/status
     */
    @PostMapping("/{id}/status")
    public String updateRmaStatus(@PathVariable Long id,
                                  @ModelAttribute RmaStatusUpdateRequest request,
                                  RedirectAttributes ra) {
        try {
            rmaService.updateRmaStatus(id, request);
            ra.addFlashAttribute("successMessage", "Cập nhật trạng thái yêu cầu thành công!");
        } catch (Exception e) {
            log.error("Lỗi khi cập nhật RMA: ", e);
            ra.addFlashAttribute("errorMessage", "Có lỗi xảy ra: " + e.getMessage());
        }
        return "redirect:/admin/rma/" + id;
    }
}