package com.clothshop.domain.repositories.auth;

import com.clothshop.domain.entities.auth.Staff;
import com.clothshop.domain.enums.AccountStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

@Repository
public interface StaffRepository extends JpaRepository<Staff, Long> {

    // Lấy tất cả và sắp xếp (thường dùng cho các dropdown chọn nhân viên)
    List<Staff> findAllByOrderByFullNameAsc();

    @Query(value = "SELECT * FROM staffs WHERE account_id = :accountId LIMIT 1", nativeQuery = true)
    Optional<Staff> findAnyByAccountId(@Param("accountId") Long accountId);

    /**
     * Lấy Staff by ID với eager fetch Account và Role
     * Dùng cho update/edit operations để tránh LazyInitializationException
     */
    @Query("SELECT s FROM Staff s " +
            "JOIN FETCH s.account " +
            "JOIN FETCH s.role " +
            "WHERE s.id = :id")
    Optional<Staff> findByIdWithRelations(@Param("id") Long id);

    @Query("SELECT s FROM Staff s " +
            "JOIN FETCH s.account a " +
            "JOIN FETCH s.role r " +
            "WHERE (:keyword IS NULL OR s.fullName LIKE %:keyword% OR a.email LIKE %:keyword% OR a.username LIKE %:keyword%) " +
            "AND (:roleId IS NULL OR r.id = :roleId) " +
            "AND (:status IS NULL OR a.accountStatus = :status)")
    Page<Staff> findAllWithFilter(@Param("keyword") String keyword,
                                  @Param("roleId") Long roleId,
                                  @Param("status") AccountStatus status,
                                  Pageable pageable);

}
