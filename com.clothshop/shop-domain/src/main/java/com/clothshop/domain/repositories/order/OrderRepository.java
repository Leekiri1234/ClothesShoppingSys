package com.clothshop.domain.repositories.order;

import com.clothshop.domain.entities.order.Order;
import com.clothshop.domain.enums.OrderStatus;
import com.clothshop.domain.projections.ProductSalesSummary;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
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

    @EntityGraph(attributePaths = {"customer"})
    Page<Order> findByStatusIn(List<OrderStatus> statuses, Pageable pageable);

    @Query("SELECT DISTINCT o FROM Order o " +
            "LEFT JOIN FETCH o.orderItems oi " +
            "LEFT JOIN FETCH oi.variant v " +
            "LEFT JOIN FETCH v.product p " +
            "LEFT JOIN FETCH o.customer c " +
            "WHERE o.status IN :statuses " +
            "AND o.createdAt BETWEEN :start AND :end")
    List<Order> findSalesOrders(@Param("statuses") List<OrderStatus> statuses,
                               @Param("start") LocalDateTime start,
                               @Param("end") LocalDateTime end);

    @Query("SELECT o FROM Order o WHERE o.status IN :statuses ORDER BY o.createdAt DESC")
    List<Order> findTopNByStatusIn(@Param("statuses") List<OrderStatus> statuses, Pageable pageable);

    @Query("SELECT COUNT(DISTINCT o.customer.id) FROM Order o WHERE o.customer.id IS NOT NULL")
    Long countDistinctCustomers();

    @Query("SELECT COALESCE(SUM(oi.quantity), 0) FROM OrderItem oi WHERE oi.order.status IN :statuses")
    Long sumOrderItemQuantities(@Param("statuses") List<OrderStatus> statuses);

    @Query("SELECT p.id AS productId, p.productName AS productName, SUM(oi.quantity) AS quantity, SUM(oi.unitPrice * oi.quantity) AS revenue " +
            "FROM OrderItem oi " +
            "JOIN oi.order o " +
            "JOIN oi.variant v " +
            "JOIN v.product p " +
            "WHERE o.status IN :statuses " +
            "GROUP BY p.id, p.productName " +
            "ORDER BY SUM(oi.quantity) DESC")
    List<ProductSalesSummary> findTopSellingProducts(@Param("statuses") List<OrderStatus> statuses, Pageable pageable);
}
