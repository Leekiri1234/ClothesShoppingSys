package com.clothshop.admin.services;

import com.clothshop.admin.dtos.request.order.RmaStatusUpdateRequest;
import com.clothshop.admin.dtos.response.order.RmaAdminResponse;
import com.clothshop.admin.mappers.RmaAdminMapper;
import com.clothshop.common.dtos.request.PagingRequest;
import com.clothshop.common.dtos.response.PageResponse;
import com.clothshop.common.exceptions.BusinessException;
import com.clothshop.common.exceptions.ErrorCode;
import com.clothshop.domain.models.order.Order;
import com.clothshop.domain.models.order.OrderItem;
import com.clothshop.domain.models.order.RmaRequest;
import com.clothshop.domain.models.product.InventoryLog;
import com.clothshop.domain.models.product.ProductVariant;
import com.clothshop.domain.enums.NotificationType;
import com.clothshop.domain.enums.RmaStatus;
import com.clothshop.domain.models.auth.Account;
import com.clothshop.domain.repositories.order.RmaRequestRepository;
import com.clothshop.domain.repositories.product.InventoryLogRepository;
import com.clothshop.domain.repositories.product.ProductVariantRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
public class RmaAdminService {

    private final RmaRequestRepository rmaRepository;
    private final RmaAdminMapper rmaMapper;
    private final ProductVariantRepository variantRepository; // Inject thêm
    private final InventoryLogRepository inventoryLogRepository; // Inject thêm
    private final AdminNotificationService notificationService;

    @Transactional(readOnly = true)
    public PageResponse<RmaAdminResponse> getAllRmaRequests(String searchKeyword, RmaStatus status, PagingRequest pagingRequest) {
        pagingRequest.validate();

        Sort sort = Sort.by(Sort.Direction.fromString(pagingRequest.getSortDirection()),
                pagingRequest.getSortBy() != null ? pagingRequest.getSortBy() : "createdAt");
        Pageable pageable = PageRequest.of(pagingRequest.getPageNumber(), pagingRequest.getPageSize(), sort);

        Page<RmaRequest> rmaPage;
        if ((searchKeyword != null && !searchKeyword.isBlank()) || status != null) {
            rmaPage = rmaRepository.findWithFilters(
                    searchKeyword != null && !searchKeyword.isBlank() ? "%" + searchKeyword.toLowerCase() + "%" : null,
                    status,
                    pageable);
        } else {
            rmaPage = rmaRepository.findAll(pageable);
        }

        List<RmaAdminResponse> content = rmaMapper.toResponseList(rmaPage.getContent());

        return PageResponse.<RmaAdminResponse>builder()
                .content(content)
                .pageNumber(rmaPage.getNumber())
                .pageSize(rmaPage.getSize())
                .totalElements(rmaPage.getTotalElements())
                .totalPages(rmaPage.getTotalPages())
                .build();
    }

    @Transactional
    public RmaAdminResponse updateRmaStatus(Long rmaId, RmaStatusUpdateRequest request) {
        RmaRequest rmaRequest = rmaRepository.findById(rmaId)
                .orElseThrow(() -> new BusinessException(ErrorCode.RESOURCE_NOT_FOUND, "Không tìm thấy yêu cầu RMA"));

        RmaStatus oldStatus = rmaRequest.getStatus();
        RmaStatus newStatus = request.getStatus();

        log.info("Updating RMA status: ID={}, Old={}, New={}", rmaId, oldStatus, newStatus);

        // 1. Cập nhật các thông tin từ Admin
        rmaRequest.setStatus(newStatus);
        if (request.getAdminNote() != null) {
            rmaRequest.setAdminNote(request.getAdminNote());
        }
        if (request.getRefundAmount() != null) {
            rmaRequest.setRefundAmount(request.getRefundAmount());
        }

        // 2. Logic xử lý KHO: Chỉ chạy khi chuyển sang COMPLETED lần đầu tiên
        if (newStatus == RmaStatus.COMPLETED && oldStatus != RmaStatus.COMPLETED) {
            handleInventoryRestock(rmaRequest);
            rmaRequest.setProcessedAt(LocalDateTime.now());
        }

        RmaRequest savedRma = rmaRepository.save(rmaRequest);

        // Gửi thông báo tự động cho khách hàng
        Order rmaOrder = savedRma.getOrder();
        if (rmaOrder != null && rmaOrder.getCustomer() != null && rmaOrder.getCustomer().getAccount() != null) {
            Account account = rmaOrder.getCustomer().getAccount();
            String title = "Cập nhật yêu cầu trả hàng: #" + savedRma.getId();
            String content = String.format("Yêu cầu trả hàng cho đơn %s đã được cập nhật thành: %s.%s",
                    rmaOrder.getOrderInvoice(), newStatus.getDisplayName(),
                    (request.getAdminNote() != null && !request.getAdminNote().trim().isEmpty() ? " Ghi chú: " + request.getAdminNote() : ""));
            String actionUrl = "/rma-management/" + savedRma.getId();

            try {
                notificationService.sendUserNotification(account, title, content, NotificationType.ORDER_UPDATE, actionUrl);
            } catch (Exception e) {
                log.error("Failed to send notification for RMA {}: {}", savedRma.getId(), e.getMessage());
            }
        }

        return rmaMapper.toResponse(savedRma);
    }

    /**
     * Hàm hỗ trợ duyệt danh sách sản phẩm trong đơn và cộng lại kho
     */
    private void handleInventoryRestock(RmaRequest rmaRequest) {
        Order order = rmaRequest.getOrder();
        log.info("Bắt đầu hoàn kho cho đơn hàng: {}", order.getOrderInvoice());

        for (OrderItem item : order.getOrderItems()) {
            ProductVariant variant = item.getVariant();
            int quantityToReturn = item.getQuantity();

            // 1. Tính toán số lượng tồn kho mới
            int oldStock = variant.getStockQuantity() != null ? variant.getStockQuantity() : 0;
            int updatedStock = oldStock + quantityToReturn;

            // 2. Cập nhật ProductVariant
            variant.setStockQuantity(updatedStock);
            variantRepository.save(variant);

            // 3. Ghi InventoryLog
            InventoryLog logEntry = InventoryLog.builder()
                    .productVariant(variant)
                    .changeQty(quantityToReturn)
                    .newStock(updatedStock)
                    .reason("RETURN")
                    .note("Hoàn kho từ RMA #" + rmaRequest.getId() + " - Đơn: " + order.getOrderInvoice())
                    .createdBy("ADMIN_SYSTEM")
                    .build();

            inventoryLogRepository.save(logEntry);

            log.info("Đã hoàn {} sản phẩm cho SKU: {}. Tồn mới: {}", quantityToReturn, variant.getSku(), updatedStock);
        }
    }
    @Transactional(readOnly = true)
    public RmaAdminResponse getRmaById(Long rmaId) {
        return rmaRepository.findById(rmaId)
                .map(rmaMapper::toResponse)
                .orElseThrow(() -> new BusinessException(ErrorCode.RESOURCE_NOT_FOUND));
    }
}