package com.clothshop.domain.repositories.auth;

import com.clothshop.domain.entities.auth.Customer;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
public interface CustomerRepository extends JpaRepository<Customer, Long> {

	@Query("SELECT c FROM Customer c LEFT JOIN FETCH c.account a ORDER BY c.createdAt DESC")
	List<Customer> findAllWithAccount();

	@Query("SELECT c FROM Customer c LEFT JOIN FETCH c.account a WHERE LOWER(c.email) = LOWER(:email) OR LOWER(a.email) = LOWER(:email)")
	Optional<Customer> findByAnyEmail(@Param("email") String email);

	long countByCreatedAtBetween(LocalDateTime start, LocalDateTime end);
}
