package com.clothshop.domain.repositories.auth;

import com.clothshop.domain.entities.auth.Account;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;

@Repository
public interface AccountRepository extends JpaRepository<Account, Long> {

    Optional<Account> findByUsername(String username);
    Optional<Account> findByEmail(String email);
    Optional<Account> findByEmailAndIsActiveTrue(String email);
    Optional<Account> findByUsernameAndIsActiveTrue(String username);

    boolean existsByUsername(String username);
    boolean existsByEmail(String email);

    // ============================================================
    // DÀNH CHO MODULE ADMIN (STAFF LOGIN)
    // ============================================================
    @Query("SELECT a FROM Account a " +
            "LEFT JOIN FETCH a.staff s " +
            "LEFT JOIN FETCH s.role r " +
            "WHERE a.username = :username AND a.isActive = true")
    Optional<Account> findByUsernameWithStaffAndRole(@Param("username") String username);

    @Query("SELECT a FROM Account a " +
            "LEFT JOIN FETCH a.staff s " +
            "LEFT JOIN FETCH s.role r " +
            "WHERE a.email = :email AND a.isActive = true")
    Optional<Account> findByEmailWithStaffAndRole(@Param("email") String email);

    // ============================================================
    // DÀNH CHO MODULE CLIENT (CUSTOMER LOGIN) - FIX LỖI 500 TẠI ĐÂY
    // ============================================================
    @Query("SELECT a FROM Account a " +
            "LEFT JOIN FETCH a.customer c " +
            "WHERE a.username = :username " +
            "AND a.isActive = true " +
            "AND a.accountType = com.clothshop.domain.enums.AccountType.CUSTOMER") // Ép kiểu luôn
    Optional<Account> findByUsernameWithCustomer(@Param("username") String username);

    @Query("SELECT a FROM Account a " +
            "LEFT JOIN FETCH a.customer c " +
            "WHERE a.email = :email " +
            "AND a.isActive = true " +
            "AND a.accountType = com.clothshop.domain.enums.AccountType.CUSTOMER") // Ép kiểu luôn
    Optional<Account> findByEmailWithCustomer(@Param("email") String email);

    @Modifying
    @Transactional
    @Query(value = "UPDATE accounts SET account_status = 'LOCKED', is_active = 0 WHERE account_id = :id", nativeQuery = true)
    void updateStatusToLockedNative(@Param("id") Long id);

    @Modifying
    @Transactional
    @Query(value = "UPDATE accounts SET is_active = 0, account_status = 'LOCKED' " +
            "WHERE account_id = (SELECT s.account_id FROM staffs s WHERE s.staff_id = :staffId)",
            nativeQuery = true)
    void lockAccountByStaffIdNative(@Param("staffId") Long staffId);
}