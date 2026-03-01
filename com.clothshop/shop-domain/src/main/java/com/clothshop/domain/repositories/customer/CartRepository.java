package com.clothshop.domain.repositories.customer;

import com.clothshop.domain.entities.customer.Cart;
import com.clothshop.domain.entities.auth.Account;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface CartRepository extends JpaRepository<Cart, Long> {
    // Lấy giỏ hàng theo Customer ID (Dùng khi user login)
    Optional<Cart> findByCustomerId(Long customerId);
}
