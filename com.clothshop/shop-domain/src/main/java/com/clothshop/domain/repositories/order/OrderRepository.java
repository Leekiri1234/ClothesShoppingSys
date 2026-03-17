package com.clothshop.domain.repositories.order;

import com.clothshop.domain.entities.order.Order;
import com.clothshop.domain.enums.OrderStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface OrderRepository extends JpaRepository<Order, Long> {
    List<Order> findByCustomerId(Long customerId);
    List<Order> findByStatus(OrderStatus status);

    // 1. Phân trang danh sách đơn hàng của khách
    Page<Order> findByCustomerIdOrderByCreatedAtDesc(Long customerId, Pageable pageable);

    // 2. Fetch toàn bộ chi tiết đơn hàng (Tránh N+1)
    @Query("SELECT DISTINCT o FROM Order o " +
            "LEFT JOIN FETCH o.orderItems oi " +
            "LEFT JOIN FETCH oi.variant v " +
            "LEFT JOIN FETCH v.product p " +
            "WHERE o.id = :orderId")
    Optional<Order> findByIdWithDetails(@Param("orderId") Long orderId);

    // 3. Fetch toàn bộ chi tiết đơn hàng bằng orderInvoice (Tránh N+1)
    @Query("SELECT DISTINCT o FROM Order o " +
            "LEFT JOIN FETCH o.orderItems oi " +
            "LEFT JOIN FETCH oi.variant v " +
            "LEFT JOIN FETCH v.product p " +
            "WHERE o.orderInvoice = :orderInvoice")
    Optional<Order> findByOrderInvoiceWithDetails(@Param("orderInvoice") String orderInvoice);

    Optional<Order> findByOrderInvoice(String orderInvoice);
}
