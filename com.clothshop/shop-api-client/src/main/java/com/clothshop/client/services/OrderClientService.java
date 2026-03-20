package com.clothshop.client.services;

import com.clothshop.client.dtos.response.OrderDetailResponse;
import com.clothshop.client.dtos.response.OrderListClientResponse;
import com.clothshop.client.mappers.OrderClientMapper;
import com.clothshop.common.dtos.request.PagingRequest;
import com.clothshop.common.dtos.response.PageResponse;
import com.clothshop.common.exceptions.BusinessException;
import com.clothshop.common.exceptions.ErrorCode;
import com.clothshop.domain.entities.auth.Account;
import com.clothshop.domain.entities.order.Order;
import com.clothshop.domain.entities.order.OrderStatusHistory;
import com.clothshop.domain.enums.OrderStatus;
import com.clothshop.domain.repositories.auth.AccountRepository;
import com.clothshop.domain.repositories.order.OrderRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class OrderClientService {

    private final OrderRepository orderRepository;
    private final AccountRepository accountRepository;
    private final OrderClientMapper orderMapper;

    private Long getCustomerId(String username) {
        Account account = accountRepository.findByUsernameWithCustomer(username)
                .orElseThrow(() -> new BusinessException(ErrorCode.USER_NOT_EXISTED));
        return account.getCustomer().getId();
    }

    @Transactional(readOnly = true)
    public PageResponse<OrderListClientResponse> getMyOrders(String username, PagingRequest pagingRequest) {
        pagingRequest.validate();
        Long customerId = getCustomerId(username);

        Pageable pageable = PageRequest.of(pagingRequest.getPageNumber(), pagingRequest.getPageSize());
        Page<Order> orderPage = orderRepository.findByCustomerIdOrderByCreatedAtDesc(customerId, pageable);

        List<OrderListClientResponse> content = orderPage.getContent().stream()
                .map(orderMapper::toListResponse)
                .collect(Collectors.toList());

        return PageResponse.<OrderListClientResponse>builder()
                .content(content)
                .pageNumber(orderPage.getNumber())
                .pageSize(orderPage.getSize())
                .totalElements(orderPage.getTotalElements())
                .totalPages(orderPage.getTotalPages())
                .build();
    }

    @Transactional(readOnly = true)
    public OrderDetailResponse getOrderDetail(String username, String orderInvoice) {
        Long customerId = getCustomerId(username);

        Order order = orderRepository.findByOrderInvoiceWithDetails(orderInvoice)
                .orElseThrow(() -> new BusinessException(ErrorCode.RESOURCE_NOT_FOUND, "Không tìm thấy đơn hàng"));

        if (!order.getCustomer().getId().equals(customerId)) {
            throw new BusinessException(ErrorCode.UNAUTHORIZED, "Bạn không có quyền xem đơn hàng này");
        }

        OrderDetailResponse response = orderMapper.toDetailResponse(order);
        if (response.getHistory() != null) {
            response.getHistory().sort(Comparator.comparing(OrderDetailResponse.OrderHistoryClientResponse::getChangedAt).reversed());
        }
        return response;
    }

    @Transactional
    public void cancelOrder(String username, String orderInvoice, String reason) {
        Long customerId = getCustomerId(username);
        Order order = orderRepository.findByOrderInvoiceWithDetails(orderInvoice)
                .orElseThrow(() -> new BusinessException(ErrorCode.RESOURCE_NOT_FOUND, "Không tìm thấy đơn hàng"));

        if (!order.getCustomer().getId().equals(customerId)) {
            throw new BusinessException(ErrorCode.UNAUTHORIZED, "Bạn không có quyền hủy đơn hàng này");
        }

        if (!OrderStatus.PENDING.equals(order.getStatus())) {
            throw new BusinessException(ErrorCode.VALIDATION_ERROR, "Cannot cancel order in " + order.getStatus() + " status");
        }

        order.setStatus(OrderStatus.CANCELLED);

        OrderStatusHistory history = new OrderStatusHistory();
        history.setOrder(order);
        history.setStatusId(OrderStatus.CANCELLED);
        history.setChangedAt(java.time.LocalDateTime.now());
        history.setNote(reason != null ? reason : "Customer cancelled order");

        if (order.getStatusHistory() == null) {
            order.setStatusHistory(new java.util.ArrayList<>());
        }
        order.getStatusHistory().add(history);

        orderRepository.save(order);
        // TODO: send notification to admin (e.g., event or email)
    }

    @Transactional
    public void reorderCancelled(String username, String orderInvoice) {
        Long customerId = getCustomerId(username);
        Order order = orderRepository.findByOrderInvoice(orderInvoice)
                .orElseThrow(() -> new BusinessException(ErrorCode.RESOURCE_NOT_FOUND, "Không tìm thấy đơn hàng"));

        if (!order.getCustomer().getId().equals(customerId)) {
            throw new BusinessException(ErrorCode.UNAUTHORIZED, "Bạn không có quyền thao tác đơn hàng này");
        }

        if (!OrderStatus.CANCELLED.equals(order.getStatus())) {
            throw new BusinessException(ErrorCode.VALIDATION_ERROR, "Chỉ có thể đặt lại đơn hàng đã hủy");
        }

        order.setStatus(OrderStatus.PENDING);

        OrderStatusHistory history = new OrderStatusHistory();
        history.setOrder(order);
        history.setStatusId(OrderStatus.PENDING);
        history.setChangedAt(java.time.LocalDateTime.now());
        history.setNote("Khách hàng đặt lại đơn hàng đã hủy");

        if (order.getStatusHistory() == null) {
            order.setStatusHistory(new java.util.ArrayList<>());
        }
        order.getStatusHistory().add(history);

        orderRepository.save(order);
    }
}

