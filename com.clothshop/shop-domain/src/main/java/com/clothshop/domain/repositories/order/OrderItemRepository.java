//package com.clothshop.domain.repositories.order;
//
//import com.clothshop.domain.models.auth.Customer;
//import com.clothshop.domain.models.order.OrderItem;
//import org.springframework.data.jpa.repository.JpaRepository;
//import org.springframework.stereotype.Repository;
//
//@Repository
//public interface OrderItemRepository extends JpaRepository<OrderItem, Long> {
//    boolean existsByOrderCustomerAndVariantProductId(Customer customer, Long productId);
//}
package com.clothshop.domain.repositories.order;

import com.clothshop.domain.enums.OrderStatus;
import com.clothshop.domain.models.auth.Customer;
import com.clothshop.domain.models.order.OrderItem;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface OrderItemRepository extends JpaRepository<OrderItem, Long> {

    // Method cũ — giữ nguyên để không break code khác
    boolean existsByOrderCustomerAndVariantProductId(Customer customer, Long productId);

    /**
     * Method mới — check đã mua VÀ đơn hàng đang ở status cho phép review
     * (DELIVERED hoặc COMPLETED).
     */
    @Query("""
        SELECT COUNT(oi) > 0
        FROM OrderItem oi
        WHERE oi.order.customer = :customer
          AND oi.variant.product.id = :productId
          AND oi.order.status IN :statuses
    """)
    boolean existsByOrderCustomerAndVariantProductIdAndOrderStatusIn(
            @Param("customer") Customer customer,
            @Param("productId") Long productId,
            @Param("statuses") List<OrderStatus> statuses);
}