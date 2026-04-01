package com.clothshop.domain.repositories.order;

import com.clothshop.domain.entities.auth.Customer;
import com.clothshop.domain.entities.order.OrderItem;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface OrderItemRepository extends JpaRepository<OrderItem, Long> {
    boolean existsByOrderCustomerAndVariantProductId(Customer customer, Long productId);
}
