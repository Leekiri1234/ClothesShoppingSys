    package com.clothshop.client.services;

    import com.clothshop.client.dtos.response.OrderDetailResponse;
    import com.clothshop.client.dtos.response.OrderListClientResponse;
    import com.clothshop.client.mappers.OrderClientMapper;
    import com.clothshop.common.dtos.request.PagingRequest;
    import com.clothshop.common.dtos.response.PageResponse;
    import com.clothshop.common.exceptions.BusinessException;
    import com.clothshop.common.exceptions.ErrorCode;
    import com.clothshop.domain.models.auth.Account;
    import com.clothshop.domain.models.marketing.Voucher;
    import com.clothshop.domain.models.order.Order;
    import com.clothshop.domain.models.order.OrderStatusHistory;
    import com.clothshop.domain.enums.OrderStatus;
    import com.clothshop.domain.repositories.auth.AccountRepository;
    import com.clothshop.domain.repositories.marketing.VoucherRedemptionRepository;
    import com.clothshop.domain.repositories.marketing.VoucherRepository;
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
        private final VoucherRedemptionRepository voucherRedemptionRepository;
        private final VoucherRepository voucherRepository;

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

            // Restore voucher usage if applied
            voucherRedemptionRepository.findByOrderId(order.getId()).ifPresent(redemption -> {
                Voucher voucher = redemption.getVoucher();
                Integer currentUsage = voucher.getCurrentUsage() == null ? 0 : voucher.getCurrentUsage();
                voucher.setCurrentUsage(Math.max(0, currentUsage - 1));
                voucherRepository.save(voucher);
                voucherRedemptionRepository.delete(redemption);
            });

            OrderStatus oldStatus = order.getStatus();
            order.setStatus(OrderStatus.CANCELLED);

            OrderStatusHistory history = OrderStatusHistory.builder()
                    .order(order)
                    .oldStatus(oldStatus)
                    .newStatus(OrderStatus.CANCELLED)
                    .changedAt(java.time.LocalDateTime.now())
                    .note(reason != null ? reason : "Customer cancelled order")
                    .build();

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

            OrderStatus oldStatus = order.getStatus();
            order.setStatus(OrderStatus.PENDING);

            OrderStatusHistory history = OrderStatusHistory.builder()
                    .order(order)
                    .oldStatus(oldStatus)
                    .newStatus(OrderStatus.PENDING)
                    .changedAt(java.time.LocalDateTime.now())
                    .note("Khách hàng đặt lại đơn hàng đã hủy")
                    .build();

            if (order.getStatusHistory() == null) {
                order.setStatusHistory(new java.util.ArrayList<>());
            }
            order.getStatusHistory().add(history);

            orderRepository.save(order);
        }
    }

