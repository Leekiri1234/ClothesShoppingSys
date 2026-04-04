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

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
public interface OrderRepository extends JpaRepository<Order, Long>, JpaSpecificationExecutor<Order> {

    List<Order> findByCustomerId(Long customerId);
    List<Order> findByStatus(OrderStatus status);
    Page<Order> findByCustomerIdOrderByCreatedAtDesc(Long customerId, Pageable pageable);
    Optional<Order> findByOrderInvoice(String orderInvoice);
        boolean existsByOrderInvoiceStartingWith(String orderInvoicePrefix);
        long countByStatus(OrderStatus status);
        Optional<Order> findTopByCustomerIdOrderByCreatedAtDesc(Long customerId);
        long countByCreatedAtBetween(LocalDateTime start, LocalDateTime end);
        List<Order> findByCreatedAtBetweenAndStatusIn(LocalDateTime start, LocalDateTime end, List<OrderStatus> statuses);

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

    @Query("SELECT o.customer.id, COUNT(o), COALESCE(SUM(o.totalPrice), 0) " +
            "FROM Order o " +
            "WHERE o.customer.id IN :customerIds " +
            "GROUP BY o.customer.id")
    List<Object[]> summarizeByCustomerIds(@Param("customerIds") List<Long> customerIds);

    @Query("SELECT COALESCE(SUM(o.totalPrice), 0) " +
            "FROM Order o " +
            "WHERE o.createdAt BETWEEN :start AND :end " +
            "AND o.status IN :statuses")
    BigDecimal sumRevenueByCreatedAtBetweenAndStatusIn(@Param("start") LocalDateTime start,
                                                       @Param("end") LocalDateTime end,
                                                       @Param("statuses") List<OrderStatus> statuses);

    @Query("SELECT o FROM Order o " +
            "LEFT JOIN FETCH o.customer c " +
            "ORDER BY o.createdAt DESC")
    List<Order> findRecentWithCustomer(Pageable pageable);

    @Query("SELECT o.customer.id, MAX(o.createdAt) " +
            "FROM Order o " +
            "WHERE o.customer.id IN :customerIds " +
            "GROUP BY o.customer.id")
    List<Object[]> findLastOrderAtByCustomerIds(@Param("customerIds") List<Long> customerIds);

    @Query("SELECT FUNCTION('YEAR', o.createdAt), FUNCTION('MONTH', o.createdAt), COUNT(o), COALESCE(SUM(o.totalPrice), 0) " +
            "FROM Order o " +
            "WHERE o.createdAt BETWEEN :start AND :end " +
            "AND o.status IN :statuses " +
            "GROUP BY FUNCTION('YEAR', o.createdAt), FUNCTION('MONTH', o.createdAt)")
    List<Object[]> summarizeMonthlyRevenue(@Param("start") LocalDateTime start,
                                           @Param("end") LocalDateTime end,
                                           @Param("statuses") List<OrderStatus> statuses);
}