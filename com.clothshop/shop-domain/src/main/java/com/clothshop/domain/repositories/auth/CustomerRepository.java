package com.clothshop.domain.repositories.auth;

import com.clothshop.domain.entities.auth.Customer;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface CustomerRepository extends JpaRepository<Customer, Long> {
}
