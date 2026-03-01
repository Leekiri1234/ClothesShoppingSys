package com.clothshop.domain.repositories.marketing;

import com.clothshop.domain.entities.marketing.Voucher;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
public interface VoucherRepository extends JpaRepository<Voucher, Long> {
    Optional<Voucher> findByCode(String code);

    @Query("SELECT v FROM Voucher v WHERE v.status = 'ACTIVE' " +
            "AND v.validFrom <= :now AND v.validTo >= :now " +
            "AND (v.usageLimit IS NULL OR v.currentUsage < v.usageLimit)")
    List<Voucher> findValidVouchers(@Param("now") LocalDateTime now);
}