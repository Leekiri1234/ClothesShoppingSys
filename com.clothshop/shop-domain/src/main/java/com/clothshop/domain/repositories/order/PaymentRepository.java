package com.clothshop.domain.repositories.order;

import com.clothshop.domain.entities.order.Payment;
import com.clothshop.domain.entities.order.Order;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface PaymentRepository extends JpaRepository<Payment, Long> {}
