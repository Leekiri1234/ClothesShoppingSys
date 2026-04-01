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

        // 2. Validate order status = COMPLETED
        if (!OrderStatus.COMPLETED.equals(order.getStatus())) {
            throw new BusinessException(ErrorCode.VALIDATION_ERROR, "Chỉ có thể đổi trả cho đơn hàng đã hoàn thành");
        }

        // 3. Validate createdAt within 7 days
        LocalDateTime sevenDaysAgo = LocalDateTime.now().minusDays(7);
        if (order.getCreatedAt().isBefore(sevenDaysAgo)) {
            throw new BusinessException(ErrorCode.VALIDATION_ERROR, "RMA period expired");
        }

        // 4. Create RmaRequest
        RmaRequest rmaRequest = RmaRequest.builder()
                .order(order)
                .customer(customer)
                .rmaType(request.getType())
                .reason(request.getReason())
                .rmaStatus(RmaStatus.PENDING)
                .build();

        // 5. Save
        rmaRequestRepository.save(rmaRequest);
    }
}
