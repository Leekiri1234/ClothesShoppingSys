package com.clothshop.domain.repositories.marketing;

import com.clothshop.domain.entities.marketing.VoucherRedemption;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface VoucherRedemptionRepository extends JpaRepository<VoucherRedemption, Long> {
    // Kiểm tra xem một khách hàng đã sử dụng mã này bao nhiêu lần
    // (Dùng khi voucher giới hạn mỗi người chỉ được dùng 1 lần)
    long countByVoucherIdAndCustomerId(Long voucherId, Long customerId);

    List<VoucherRedemption> findByCustomerId(Long customerId);
}
