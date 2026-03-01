package com.clothshop.domain.repositories.customer;

import com.clothshop.domain.entities.customer.Wishlist;
import com.clothshop.domain.entities.auth.Customer;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface WishlistRepository extends JpaRepository<Wishlist, Long> {
    Optional<Wishlist> findByCustomerId(Long customerId);
}
