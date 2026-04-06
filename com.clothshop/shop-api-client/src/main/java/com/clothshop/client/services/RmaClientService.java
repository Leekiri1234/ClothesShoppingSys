package com.clothshop.client.services;

import com.clothshop.client.dtos.request.RmaCreateRequest;
import com.clothshop.common.exceptions.BusinessException;
import com.clothshop.common.exceptions.ErrorCode;
import com.clothshop.domain.entities.auth.Account;
import com.clothshop.domain.entities.auth.Customer;
import com.clothshop.domain.entities.order.Order;
import com.clothshop.domain.entities.order.RmaRequest;
import com.clothshop.domain.enums.OrderStatus;
import com.clothshop.domain.enums.RmaStatus;
import com.clothshop.domain.enums.RmaType; // Quan trọng: Import Enum này
import com.clothshop.domain.repositories.auth.AccountRepository;
import com.clothshop.domain.repositories.order.OrderRepository;
import com.clothshop.domain.repositories.order.RmaRequestRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

@Service
@RequiredArgsConstructor
public class RmaClientService {

    private final RmaRequestRepository rmaRequestRepository;
    private final OrderRepository orderRepository;
    private final AccountRepository accountRepository;

    private Customer getCustomer(String username) {
        Account account = accountRepository.findByUsernameWithCustomer(username)
                .orElseThrow(() -> new BusinessException(ErrorCode.USER_NOT_EXISTED));
        return account.getCustomer();
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
                    // FIX 1: Chuyển String từ request sang Enum RmaType
                    .rmaType(RmaType.valueOf(request.getType().toUpperCase()))
                    .reason(request.getReason())
                    // FIX 2: Đổi .rmaStatus() thành .status() cho khớp Entity đã sửa ở bước trước
                    .status(RmaStatus.PENDING)
                    .build();

            // 5. Save
            rmaRequestRepository.save(rmaRequest);
        } catch (IllegalArgumentException e) {
            // Trường hợp khách gửi type bậy bạ không khớp với Enum RETURN hay EXCHANGE
            throw new BusinessException(ErrorCode.VALIDATION_ERROR, "Loại yêu cầu không hợp lệ");
        }
    }
}