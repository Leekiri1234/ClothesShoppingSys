package com.clothshop.client.services;

import com.clothshop.client.dtos.request.RmaCreateRequest;
import com.clothshop.client.dtos.response.RmaListResponse;
import com.clothshop.common.exceptions.BusinessException;
import com.clothshop.common.exceptions.ErrorCode;
import com.clothshop.common.utils.FileUploadUtil;
import com.clothshop.domain.models.auth.Account;
import com.clothshop.domain.models.auth.Customer;
import com.clothshop.domain.models.order.Order;
import com.clothshop.domain.models.order.RmaRequest;
import com.clothshop.domain.enums.OrderStatus;
import com.clothshop.domain.enums.RmaStatus;
import com.clothshop.domain.enums.RmaType;
import com.clothshop.domain.repositories.auth.AccountRepository;
import com.clothshop.domain.repositories.order.OrderRepository;
import com.clothshop.domain.repositories.order.RmaRequestRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;
import org.springframework.web.multipart.MultipartFile;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Service
@RequiredArgsConstructor
public class RmaClientService {

    private final RmaRequestRepository rmaRequestRepository;
    private final OrderRepository orderRepository;
    private final AccountRepository accountRepository;
    private final FileUploadUtil fileUploadUtil;

    private Customer getCustomer(String username) {
        Account account = accountRepository.findByUsernameWithCustomer(username)
                .orElseThrow(() -> new BusinessException(ErrorCode.USER_NOT_EXISTED));
        return account.getCustomer();
    }

    @Transactional
    public boolean hasRmaRequest(String orderInvoice, String username) {
        Order order = orderRepository.findByOrderInvoice(orderInvoice)
                .orElseThrow(() -> new BusinessException(ErrorCode.RESOURCE_NOT_FOUND, "Không tìm thấy đơn hàng"));

        Customer customer = getCustomer(username);
        return rmaRequestRepository.existsByOrderIdAndCustomerId(order.getId(), customer.getId());
    }

    @Transactional
    public void submitRequest(String username, RmaCreateRequest request) {
        Customer customer = getCustomer(username);

        Order order = orderRepository.findByOrderInvoice(request.getOrderInvoice())
                .orElseThrow(() -> new BusinessException(ErrorCode.RESOURCE_NOT_FOUND, "Không tìm thấy đơn hàng"));

        // 1. Validate belongs to customer
        if (!order.getCustomer().getId().equals(customer.getId())) {
            throw new BusinessException(ErrorCode.UNAUTHORIZED, "Bạn không có quyền thực hiện yêu cầu này");
        }

        // 2. Validate order status (Thường là DELIVERED hoặc COMPLETED tùy logic shop bạn)
        if (!OrderStatus.DELIVERED.equals(order.getStatus()) && !OrderStatus.COMPLETED.equals(order.getStatus())) {
            throw new BusinessException(ErrorCode.VALIDATION_ERROR, "Chỉ có thể đổi trả cho đơn hàng đã giao thành công");
        }

        // 3. Validate createdAt within 7 days
        LocalDateTime sevenDaysAgo = LocalDateTime.now().minusDays(7);
        if (order.getCreatedAt().isBefore(sevenDaysAgo)) {
            throw new BusinessException(ErrorCode.VALIDATION_ERROR, "Đã quá thời hạn 7 ngày để thực hiện đổi trả");
        }

        // 4. Create RmaRequest
        try {
            RmaRequest rmaRequest = RmaRequest.builder()
                    .order(order)
                    .customer(customer)
                    .rmaType(RmaType.valueOf(request.getType().toUpperCase()))
                    .reason(request.getReason())
                    .status(RmaStatus.PENDING)
                    .build();

            // 5. Upload evidence images (if any)
            if (request.getEvidenceImages() != null && !request.getEvidenceImages().isEmpty()) {
                List<String> paths = new ArrayList<>();
                for (MultipartFile file : request.getEvidenceImages()) {
                    if (!file.isEmpty()) {
                        // ✅ FileUploadUtil now returns complete path like /uploads/rma/filename.webp
                        String savedPath = fileUploadUtil.upload(file, "rma");
                        paths.add(savedPath);  // No need to add /uploads/ anymore
                    }
                }
                if (!paths.isEmpty()) {
                    rmaRequest.setEvidenceImages(String.join(",", paths));
                }
            }

            // 6. Save
            rmaRequestRepository.save(rmaRequest);
        } catch (IllegalArgumentException e) {
            throw new BusinessException(ErrorCode.VALIDATION_ERROR, "Loại yêu cầu không hợp lệ");
        }
    }

    /**
     * Get all RMA requests for the current customer
     */
    @Transactional(readOnly = true)
    public Page<RmaListResponse> getMyRmaRequests(String username, int page, int size) {
        Customer customer = getCustomer(username);
        
        Pageable pageable = PageRequest.of(page, size, Sort.by(Sort.Direction.DESC, "createdAt"));
        Page<RmaRequest> rmaRequests = rmaRequestRepository.findByCustomerId(customer.getId(), pageable);
        
        return rmaRequests.map(this::mapToRmaListResponse);
    }

    /**
     * Get RMA request detail
     */
    @Transactional(readOnly = true)
    public RmaListResponse getRmaRequestDetail(String username, Long rmaId) {
        Customer customer = getCustomer(username);
        
        RmaRequest rmaRequest = rmaRequestRepository.findByIdAndCustomerId(rmaId, customer.getId())
                .orElseThrow(() -> new BusinessException(ErrorCode.RESOURCE_NOT_FOUND, "Không tìm thấy yêu cầu đổi trả"));
        
        return mapToRmaListResponse(rmaRequest);
    }

    /**
     * Cancel RMA request (only if status is PENDING)
     */
    @Transactional
    public void cancelRmaRequest(String username, Long rmaId) {
        Customer customer = getCustomer(username);
        
        RmaRequest rmaRequest = rmaRequestRepository.findByIdAndCustomerId(rmaId, customer.getId())
                .orElseThrow(() -> new BusinessException(ErrorCode.RESOURCE_NOT_FOUND, "Không tìm thấy yêu cầu đổi trả"));
        
        if (!RmaStatus.PENDING.equals(rmaRequest.getStatus())) {
            throw new BusinessException(ErrorCode.VALIDATION_ERROR, "Chỉ có thể hủy yêu cầu đang chờ xử lý");
        }
        
        rmaRequestRepository.delete(rmaRequest);
    }

    /**
     * Map RmaRequest entity to DTO
     */
    private RmaListResponse mapToRmaListResponse(RmaRequest rmaRequest) {
        return RmaListResponse.builder()
                .rmaId(rmaRequest.getId())
                .orderInvoice(rmaRequest.getOrder().getOrderInvoice())
                .rmaType(rmaRequest.getRmaType().toString())
                .reason(rmaRequest.getReason())
                .status(rmaRequest.getStatus().toString())
                .processedAt(rmaRequest.getProcessedAt())
                .adminNote(rmaRequest.getAdminNote())
                .refundAmount(rmaRequest.getRefundAmount())
                .createdAt(rmaRequest.getCreatedAt())
                .evidenceImages(normalizeEvidenceImages(rmaRequest.getEvidenceImages()))
                .build();
    }

    private String normalizeEvidenceImages(String raw) {
        if (!StringUtils.hasText(raw)) {
            return raw;
        }

        List<String> normalized = new ArrayList<>();
        for (String part : raw.split(",")) {
            String value = part.trim();
            if (!StringUtils.hasText(value)) {
                continue;
            }

            if (value.startsWith("http://") || value.startsWith("https://") || value.startsWith("/")) {
                normalized.add(value);
            } else {
                // Backward compatibility: old data might store only filename.
                normalized.add("/uploads/rma/" + value);
            }
        }

        return String.join(",", normalized);
    }
}
