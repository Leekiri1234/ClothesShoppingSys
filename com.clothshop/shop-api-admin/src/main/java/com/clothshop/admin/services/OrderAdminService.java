package com.clothshop.admin.services;

import com.clothshop.admin.dtos.request.order.OrderFilterRequest;
import com.clothshop.admin.dtos.response.order.OrderAdminResponse;
import com.clothshop.admin.dtos.response.order.OrderDetailResponse;
import com.clothshop.admin.mappers.OrderAdminMapper;
import com.clothshop.common.exceptions.BusinessException;
import com.clothshop.common.exceptions.ErrorCode;
import com.clothshop.domain.models.order.Order;
import com.clothshop.domain.models.order.OrderItem;
import com.clothshop.domain.models.order.OrderStatusHistory;
import com.clothshop.domain.models.product.ProductVariant;
import com.clothshop.domain.enums.NotificationType;
import com.clothshop.domain.enums.OrderStatus;
import com.clothshop.domain.models.auth.Account;
import com.clothshop.domain.repositories.order.OrderRepository;
import com.clothshop.domain.repositories.order.OrderStatusHistoryRepository;
import com.clothshop.domain.repositories.product.ProductVariantRepository;
import jakarta.persistence.criteria.Predicate;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import lombok.extern.slf4j.Slf4j;
import java.util.ArrayList;
import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class OrderAdminService {

    private final ProductVariantRepository variantRepository;
    private final OrderRepository orderRepository;
    private final OrderStatusHistoryRepository historyRepository;
    private final OrderAdminMapper orderMapper;
    private final AdminNotificationService notificationService;

    /**
     * Lấy danh sách đơn hàng có phân trang và lọc động.
     */
    @Transactional(readOnly = true)
    public Page<OrderAdminResponse> getOrders(OrderFilterRequest filter, Pageable pageable) {
        // Viết Specification trực tiếp tại đây để sử dụng OrderFilterRequest của Admin module
        Specification<Order> spec = (root, query, cb) -> {
            List<Predicate> predicates = new ArrayList<>();

            if (filter.getStatus() != null) {
                predicates.add(cb.equal(root.get("status"), filter.getStatus()));
            }

            if (filter.getStartDate() != null) {
                predicates.add(cb.greaterThanOrEqualTo(root.get("createdAt"), filter.getStartDate()));
            }

            if (filter.getEndDate() != null) {
                predicates.add(cb.lessThanOrEqualTo(root.get("createdAt"), filter.getEndDate()));
            }

            if (filter.getKeyword() != null && !filter.getKeyword().isBlank()) {
                String pattern = "%" + filter.getKeyword().toLowerCase() + "%";
                // Lưu ý: customer và orderInvoice phải khớp với tên field trong Entity Order
                Predicate invoicePredicate = cb.like(cb.lower(root.get("orderInvoice")), pattern);
                Predicate customerPredicate = cb.like(cb.lower(root.join("customer").get("fullName")), pattern);
                predicates.add(cb.or(invoicePredicate, customerPredicate));
            }

            return cb.and(predicates.toArray(new Predicate[0]));
        };

        return orderRepository.findAll(spec, pageable)
                .map(orderMapper::toListResponse);
    }

    /**
     * Lấy chi tiết đơn hàng kèm theo lịch sử trạng thái.
     */
    @Transactional(readOnly = true)
    public OrderDetailResponse getOrderDetail(Long id) {
        Order order = orderRepository.findByIdWithDetails(id)
                .orElseThrow(() -> new BusinessException(ErrorCode.RESOURCE_NOT_FOUND));

        return orderMapper.toDetailResponse(order);
    }

    /**
     * Cập nhật trạng thái đơn hàng và ghi nhật ký (Audit Log).
     * Đây là phần quan trọng nhất của task S2_ADM_01.
     */
    @Transactional
    public void updateOrderStatus(Long orderId, OrderStatus newStatus, String note) {
        Order order = orderRepository.findByIdWithDetails(orderId)
                .orElseThrow(() -> new BusinessException(ErrorCode.RESOURCE_NOT_FOUND));

        OrderStatus oldStatus = order.getStatus();
        if (oldStatus == newStatus) return;

        // 1. QUAN TRỌNG: Phải gọi hàm validate này để chặn các bước nhảy sai
        validateStatusTransition(oldStatus, newStatus);

        // 2. Logic KHO HÀNG
        // Hoàn kho nếu Hủy đơn (CANCELLED) hoặc Trả hàng (RETURNED)
        if ((newStatus == OrderStatus.CANCELLED || newStatus == OrderStatus.RETURNED)
                && oldStatus != OrderStatus.CANCELLED && oldStatus != OrderStatus.RETURNED) {
            handleRestocking(order);
        }
        // Trừ kho nếu Xác nhận (CONFIRMED) hoặc bắt đầu Giao (SHIPPING)
        else if ((newStatus == OrderStatus.CONFIRMED || newStatus == OrderStatus.SHIPPING)
                && oldStatus == OrderStatus.PENDING) {
            handleDeductStock(order);
        }

        // 3. Cập nhật
        order.setStatus(newStatus);
        orderRepository.save(order);

        // Lưu lịch sử kèm ghi chú (Ghi chú này lấy từ Modal trên giao diện)
        OrderStatusHistory history = OrderStatusHistory.builder()
                .order(order).oldStatus(oldStatus).newStatus(newStatus)
                .note(note).changedAt(java.time.LocalDateTime.now())
                .build();
        historyRepository.save(history);

        // Gửi thông báo tự động cho khách hàng
        if (order.getCustomer() != null && order.getCustomer().getAccount() != null) {
            Account account = order.getCustomer().getAccount();
            String title = "Cập nhật trạng thái đơn hàng: " + order.getOrderInvoice();
            String content = String.format("Đơn hàng %s của bạn đã được cập nhật thành: %s.%s",
                    order.getOrderInvoice(), newStatus.getDisplayName(),
                    (note != null && !note.trim().isEmpty() ? " Ghi chú: " + note : ""));
            String actionUrl = "/orders/" + order.getOrderInvoice();

            try {
                notificationService.sendUserNotification(account, title, content, NotificationType.ORDER_UPDATE, actionUrl);
            } catch (Exception e) {
                log.error("Failed to send notification for order {}: {}", order.getOrderInvoice(), e.getMessage());
            }
        }
    }

    private void validateStatusTransition(OrderStatus current, OrderStatus next) {
        boolean isValid = switch (current) {
            // 1. Chờ xác nhận -> Có thể Xác nhận hoặc Hủy
            case PENDING -> (next == OrderStatus.CONFIRMED || next == OrderStatus.CANCELLED);

            // 2. Đã xác nhận -> Có thể Giao hàng hoặc Hủy
            case CONFIRMED -> (next == OrderStatus.SHIPPING || next == OrderStatus.CANCELLED);

            // 3. Đang giao -> Có thể Đã giao (thành công), Hủy (giữa chừng) hoặc Trả hàng (khách từ chối)
            case SHIPPING -> (next == OrderStatus.DELIVERED || next == OrderStatus.CANCELLED || next == OrderStatus.RETURNED);

            // 4. Đã giao -> Có thể bấm Hoàn thành (kết thúc đơn) hoặc Trả hàng (nếu khách khiếu nại)
            case DELIVERED -> (next == OrderStatus.COMPLETED || next == OrderStatus.RETURNED);

            // 5. Các trạng thái cuối -> Không thể đi đâu tiếp theo
            case COMPLETED, CANCELLED, RETURNED -> false;
        };

        if (!isValid) {
            throw new BusinessException(ErrorCode.VALIDATION_ERROR,
                    "Không thể chuyển trạng thái từ " + current.getDisplayName() + " sang " + next.getDisplayName());
        }
    }

    // Hàm phụ trách hoàn kho
    private void handleRestocking(Order order) {
        if (order.getOrderItems() == null) return;

        for (OrderItem item : order.getOrderItems()) {
            ProductVariant variant = item.getVariant();
            if (variant != null) {
                int currentStock = variant.getStockQuantity() != null ? variant.getStockQuantity() : 0;
                // Cộng ngược lại số lượng khách đã đặt vào kho
                variant.setStockQuantity(currentStock + item.getQuantity());
                variantRepository.save(variant);

                log.info("Restocked: Product {} - Variant {} | +{} items",
                        variant.getProduct().getProductName(), variant.getSku(), item.getQuantity());
            }
        }
    }

    private void handleDeductStock(Order order) {
        if (order.getOrderItems() == null) return;

        for (OrderItem item : order.getOrderItems()) {
            ProductVariant variant = item.getVariant();
            if (variant != null) {
                int currentStock = variant.getStockQuantity() != null ? variant.getStockQuantity() : 0;

                // Kiểm tra xem kho còn đủ hàng không trước khi trừ
                if (currentStock < item.getQuantity()) {
                    throw new BusinessException(ErrorCode.INSUFFICIENT_STOCK,
                            "Sản phẩm " + variant.getProduct().getProductName() + " không đủ tồn kho!");
                }

                variant.setStockQuantity(currentStock - item.getQuantity());
                variantRepository.save(variant);
                log.info("Deducted: Variant {} | -{} items", variant.getSku(), item.getQuantity());
            }
        }
    }
}

