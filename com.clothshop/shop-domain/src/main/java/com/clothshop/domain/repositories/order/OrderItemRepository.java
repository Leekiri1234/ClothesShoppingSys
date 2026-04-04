package com.clothshop.domain.repositories.order;

import com.clothshop.domain.entities.auth.Customer;
import com.clothshop.domain.entities.order.OrderItem;
import com.clothshop.domain.enums.OrderStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface OrderItemRepository extends JpaRepository<OrderItem, Long> {

    @Query("SELECT p.id, p.productName, SUM(oi.quantity), COALESCE(SUM(oi.unitPrice * oi.quantity), 0) " +
	    "FROM OrderItem oi " +
	    "JOIN oi.variant v " +
	    "JOIN v.product p " +
	    "JOIN oi.order o " +
	    "WHERE o.createdAt BETWEEN :start AND :end " +
	    "AND o.status IN :statuses " +
	    "GROUP BY p.id, p.productName " +
	    "ORDER BY SUM(oi.quantity) DESC")
    List<Object[]> summarizeTopProducts(@Param("start") LocalDateTime start,
					@Param("end") LocalDateTime end,
					@Param("statuses") List<OrderStatus> statuses,
					org.springframework.data.domain.Pageable pageable);

    boolean existsByOrderCustomerAndVariantProductId(Customer customer, Long productId);
}
