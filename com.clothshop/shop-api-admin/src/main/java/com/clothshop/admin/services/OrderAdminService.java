package com.clothshop.admin.services;

import com.clothshop.admin.dtos.request.order.OrderFilterRequest;
import com.clothshop.admin.dtos.response.order.OrderAdminResponse;
import com.clothshop.admin.dtos.response.order.OrderDetailResponse;
import com.clothshop.admin.mappers.OrderAdminMapper;
import com.clothshop.common.exceptions.BusinessException;
import com.clothshop.common.exceptions.ErrorCode;
import com.clothshop.domain.entities.order.Order;
import com.clothshop.domain.entities.order.OrderItem;
import com.clothshop.domain.entities.order.OrderStatusHistory;
import com.clothshop.domain.entities.product.ProductVariant;
import com.clothshop.domain.enums.OrderStatus;
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
        // 1. Lấy đơn hàng kèm theo Items (phải fetch items để có thông tin variant)
        Order order = orderRepository.findByIdWithDetails(orderId)
                .orElseThrow(() -> new BusinessException(ErrorCode.RESOURCE_NOT_FOUND));

        OrderStatus oldStatus = order.getStatus();

        // 2. Kiểm tra nếu trạng thái không đổi thì không làm gì cả
        if (oldStatus == newStatus) return;

        // 3. LOGIC HOÀN KHO: Chỉ chạy khi trạng thái mới là CANCELLED và trạng thái cũ KHÔNG PHẢI là CANCELLED
        if (newStatus == OrderStatus.CANCELLED && oldStatus != OrderStatus.CANCELLED) {
            handleRestocking(order);
        }

        // 4. Cập nhật trạng thái và lưu lịch sử (như cũ)
        order.setStatus(newStatus);
        orderRepository.save(order);

        OrderStatusHistory history = OrderStatusHistory.builder()
                .order(order)
                .oldStatus(oldStatus)
                .newStatus(newStatus)
                .note(note)
                .changedAt(java.time.LocalDateTime.now())
                .build();

        historyRepository.save(history);
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
}