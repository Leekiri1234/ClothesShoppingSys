package com.clothshop.domain.repositories.order;

import com.clothshop.domain.entities.order.Order;
import com.clothshop.domain.enums.OrderStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor; // THÊM MỚI
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface OrderRepository extends JpaRepository<Order, Long>, JpaSpecificationExecutor<Order> {

    List<Order> findByCustomerId(Long customerId);
    List<Order> findByStatus(OrderStatus status);
    Page<Order> findByCustomerIdOrderByCreatedAtDesc(Long customerId, Pageable pageable);
    Optional<Order> findByOrderInvoice(String orderInvoice);

    // --- Tối ưu cho Admin: Xem chi tiết đơn hàng (Dùng Fetch Join sâu) ---
    @Query("SELECT DISTINCT o FROM Order o " +
            "LEFT JOIN FETCH o.orderItems oi " +
            "LEFT JOIN FETCH oi.variant v " +
            "LEFT JOIN FETCH v.product p " +
            "LEFT JOIN FETCH o.customer c " + // Fetch luôn customer để hiện tên ở Detail
            "WHERE o.id = :orderId")
    Optional<Order> findByIdWithDetails(@Param("orderId") Long orderId);

    @Query("SELECT DISTINCT o FROM Order o " +
            "LEFT JOIN FETCH o.orderItems oi " +
            "LEFT JOIN FETCH oi.variant v " +
            "LEFT JOIN FETCH v.product p " +
            "LEFT JOIN FETCH o.customer c " +
            "WHERE o.orderInvoice = :orderInvoice")
    Optional<Order> findByOrderInvoiceWithDetails(@Param("orderInvoice") String orderInvoice);
}