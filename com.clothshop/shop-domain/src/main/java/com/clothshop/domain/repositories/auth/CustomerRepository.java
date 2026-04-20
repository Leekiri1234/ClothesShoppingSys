package com.clothshop.domain.repositories.auth;

import com.clothshop.domain.models.auth.Customer;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.Optional;

@Repository
public interface CustomerRepository extends JpaRepository<Customer, Long> {

    long countByIsActiveTrue();

    // Tìm kiếm khách hàng theo Email hoặc Tên đăng nhập của Account
    @Query("SELECT c FROM Customer c JOIN c.account a WHERE a.email = :email OR a.username = :username")
    Optional<Customer> findByEmailOrUsername(@Param("email") String email, @Param("username") String username);

    // Tìm khách hàng bằng ID, kể cả khi is_active = false
    // Dùng Native Query để bypass cái @SQLRestriction của Hibernate
    @Query(value = "SELECT * FROM customers WHERE customer_id = :id", nativeQuery = true)
    Optional<Customer> findByIdIncludeDeleted(@Param("id") Long id);

    @Query("SELECT c FROM Customer c " +
            "LEFT JOIN c.account a " +
            "WHERE (:keyword IS NULL OR LOWER(c.fullName) LIKE LOWER(CONCAT('%', :keyword, '%')) " +
            "   OR LOWER(c.email) LIKE LOWER(CONCAT('%', :keyword, '%')) " +
            "   OR LOWER(COALESCE(a.email, '')) LIKE LOWER(CONCAT('%', :keyword, '%')) " +
            "   OR LOWER(COALESCE(a.username, '')) LIKE LOWER(CONCAT('%', :keyword, '%'))) " +
            "AND (:isActive IS NULL OR c.isActive = :isActive) " +
            "AND (:createdFrom IS NULL OR c.createdAt >= :createdFrom) " +
            "AND (:createdTo IS NULL OR c.createdAt <= :createdTo)")
    Page<Customer> findAllWithFilter(@Param("keyword") String keyword,
                                     @Param("isActive") Boolean isActive,
                                     @Param("createdFrom") LocalDateTime createdFrom,
                                     @Param("createdTo") LocalDateTime createdTo,
                                     Pageable pageable);
}