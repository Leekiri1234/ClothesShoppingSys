package com.clothshop.domain.repositories.auth;

import com.clothshop.domain.models.auth.Customer;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.Optional;

@Repository
public interface CustomerRepository extends JpaRepository<Customer, Long> {

    // Tìm kiếm khách hàng theo Email hoặc Tên đăng nhập của Account
    @Query("SELECT c FROM Customer c JOIN c.account a WHERE a.email = :email OR a.username = :username")
    Optional<Customer> findByEmailOrUsername(@Param("email") String email, @Param("username") String username);

    // Tìm khách hàng bằng ID, kể cả khi is_active = false
    // Dùng Native Query để bypass cái @SQLRestriction của Hibernate
    @Query(value = "SELECT * FROM customers WHERE customer_id = :id", nativeQuery = true)
    Optional<Customer> findByIdIncludeDeleted(@Param("id") Long id);

    @Query(value = """
            SELECT c.*
            FROM customers c
            LEFT JOIN accounts a ON a.account_id = c.account_id
            WHERE (:activeFilter IS NULL OR c.is_active = :activeFilter)
              AND (:createdFrom IS NULL OR DATE(c.created_at) >= :createdFrom)
              AND (:createdTo IS NULL OR DATE(c.created_at) <= :createdTo)
              AND (
                    :keyword IS NULL OR :keyword = ''
                    OR LOWER(c.full_name) LIKE LOWER(CONCAT('%', :keyword, '%'))
                    OR LOWER(COALESCE(c.email, '')) LIKE LOWER(CONCAT('%', :keyword, '%'))
                    OR LOWER(COALESCE(a.email, '')) LIKE LOWER(CONCAT('%', :keyword, '%'))
                    OR LOWER(COALESCE(a.username, '')) LIKE LOWER(CONCAT('%', :keyword, '%'))
                  )
            ORDER BY c.created_at DESC
            """,
            countQuery = """
                    SELECT COUNT(1)
                    FROM customers c
                    LEFT JOIN accounts a ON a.account_id = c.account_id
                    WHERE (:activeFilter IS NULL OR c.is_active = :activeFilter)
                      AND (:createdFrom IS NULL OR DATE(c.created_at) >= :createdFrom)
                      AND (:createdTo IS NULL OR DATE(c.created_at) <= :createdTo)
                      AND (
                            :keyword IS NULL OR :keyword = ''
                            OR LOWER(c.full_name) LIKE LOWER(CONCAT('%', :keyword, '%'))
                            OR LOWER(COALESCE(c.email, '')) LIKE LOWER(CONCAT('%', :keyword, '%'))
                            OR LOWER(COALESCE(a.email, '')) LIKE LOWER(CONCAT('%', :keyword, '%'))
                            OR LOWER(COALESCE(a.username, '')) LIKE LOWER(CONCAT('%', :keyword, '%'))
                          )
                    """,
            nativeQuery = true)
    Page<Customer> findAllForAdmin(@Param("keyword") String keyword,
                                   @Param("activeFilter") Boolean activeFilter,
                                   @Param("createdFrom") LocalDate createdFrom,
                                   @Param("createdTo") LocalDate createdTo,
                                   Pageable pageable);
}