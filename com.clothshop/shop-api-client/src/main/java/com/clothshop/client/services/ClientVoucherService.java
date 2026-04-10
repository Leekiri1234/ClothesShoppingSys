package com.clothshop.client.services;

import com.clothshop.client.dtos.response.VoucherClientResponse;
import com.clothshop.client.mappers.ClientVoucherMapper;
import com.clothshop.common.exceptions.BusinessException;
import com.clothshop.common.exceptions.ErrorCode;
import com.clothshop.domain.models.marketing.Voucher;
import com.clothshop.domain.enums.VoucherStatus;
import com.clothshop.domain.repositories.marketing.VoucherRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
public class ClientVoucherService {

    private final VoucherRepository voucherRepository;
    private final ClientVoucherMapper clientVoucherMapper;

    // 1. Lấy danh sách hiển thị ở Kho Voucher và Dropdown Checkout
    @Transactional(readOnly = true)
    public List<VoucherClientResponse> getAvailableVouchers() {
        LocalDateTime now = LocalDateTime.now();
        List<Voucher> vouchers = voucherRepository.findAvailableVouchers(now);
        return clientVoucherMapper.toResponseList(vouchers);
    }

    // 2. Hàm dùng cho AJAX Validation tại trang Checkout
    @Transactional(readOnly = true)
    public VoucherClientResponse validateVoucher(String code, BigDecimal cartTotal) {
        Voucher voucher = voucherRepository.findByCode(code.trim().toUpperCase())
                .orElseThrow(() -> new BusinessException(ErrorCode.RESOURCE_NOT_FOUND, "Mã giảm giá không tồn tại"));

        // Kiểm tra trạng thái
        if (!VoucherStatus.ACTIVE.name().equals(voucher.getStatus())) {
            throw new BusinessException(ErrorCode.VALIDATION_ERROR, "Mã giảm giá đã bị khóa hoặc không hoạt động");
        }

        // Kiểm tra thời gian
        LocalDateTime now = LocalDateTime.now();
        if (now.isBefore(voucher.getValidFrom())) {
            throw new BusinessException(ErrorCode.VALIDATION_ERROR, "Mã giảm giá chưa đến thời gian sử dụng");
        }
        if (now.isAfter(voucher.getValidTo())) {
            throw new BusinessException(ErrorCode.VALIDATION_ERROR, "Mã giảm giá đã hết hạn");
        }

        // Kiểm tra số lượng
        if (voucher.getUsageLimit() != null && voucher.getCurrentUsage() >= voucher.getUsageLimit()) {
            throw new BusinessException(ErrorCode.VALIDATION_ERROR, "Mã giảm giá đã hết lượt sử dụng");
        }

        // Kiểm tra điều kiện đơn hàng
        if (voucher.getMinOrderValue() != null && cartTotal.compareTo(voucher.getMinOrderValue()) < 0) {
            throw new BusinessException(ErrorCode.VALIDATION_ERROR,
                    "Đơn hàng chưa đạt giá trị tối thiểu " + voucher.getMinOrderValue() + "đ để sử dụng mã này");
        }

        return clientVoucherMapper.toResponse(voucher);
    }
}