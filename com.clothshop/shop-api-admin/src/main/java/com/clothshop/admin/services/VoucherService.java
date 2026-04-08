package com.clothshop.admin.services;

import com.clothshop.admin.dtos.request.marketing.VoucherCreateRequest;
import com.clothshop.admin.dtos.request.marketing.VoucherUpdateRequest;
import com.clothshop.admin.dtos.response.marketing.VoucherResponse;
import com.clothshop.admin.mappers.VoucherMapper;
import com.clothshop.common.exceptions.BusinessException;
import com.clothshop.common.exceptions.ErrorCode;
import com.clothshop.domain.models.marketing.Voucher;
import com.clothshop.domain.enums.VoucherStatus;
import com.clothshop.domain.repositories.marketing.VoucherRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

@Service
@RequiredArgsConstructor
@Slf4j
public class VoucherService {

    private final VoucherRepository voucherRepository;
    private final VoucherMapper voucherMapper;

    @Transactional(readOnly = true)
    public Page<VoucherResponse> getAllVouchers(Pageable pageable) {
        return voucherRepository.findAll(pageable).map(voucherMapper::toResponse);
    }

    @Transactional(readOnly = true)
    public VoucherResponse getVoucherById(Long id) {
        Voucher voucher = voucherRepository.findById(id)
                .orElseThrow(() -> new BusinessException(ErrorCode.RESOURCE_NOT_FOUND, "Không tìm thấy voucher"));
        return voucherMapper.toResponse(voucher);
    }

    @Transactional
    public void createVoucher(VoucherCreateRequest request, String username) {
        // 1. Chuẩn hóa mã Code (Xóa khoảng trắng, viết hoa)
        String cleanCode = request.getCode().trim().toUpperCase();

        // 2. Sử dụng existsByCode: Tối ưu RAM vì không cần load toàn bộ Entity
        if (voucherRepository.existsByCode(cleanCode)) {
            log.warn("Attempt to create duplicate voucher code: {}", cleanCode);
            throw new BusinessException(ErrorCode.DUPLICATE_RESOURCE, "Mã giảm giá đã tồn tại");
        }

        // 3. Logic validation thời gian
        if (request.getValidFrom().isAfter(request.getValidTo())) {
            throw new BusinessException(ErrorCode.VALIDATION_ERROR, "Ngày hết hạn phải sau ngày bắt đầu");
        }

        // 4. Mapping và lưu trữ
        Voucher voucher = voucherMapper.toEntity(request);
        voucher.setCode(cleanCode);
        voucher.setCreatedBy(username);
        voucher.setStatus(VoucherStatus.ACTIVE.name());

        voucherRepository.save(voucher);
        log.info("Voucher [{}] created successfully by admin: {}", cleanCode, username);
    }

    @Transactional
    public void updateVoucher(Long id, VoucherUpdateRequest request, String username) {
        Voucher voucher = voucherRepository.findById(id)
                .orElseThrow(() -> new BusinessException(ErrorCode.RESOURCE_NOT_FOUND, "Không tìm thấy voucher"));

        if (request.getValidFrom().isAfter(request.getValidTo())) {
            throw new BusinessException(ErrorCode.VALIDATION_ERROR, "Ngày hết hạn phải sau ngày bắt đầu");
        }

        voucherMapper.updateEntityFromRequest(request, voucher);
        voucher.setUpdatedBy(username);

        boolean wasExpired = VoucherStatus.EXPIRED.name().equals(voucher.getStatus());
        if (wasExpired && request.getValidTo().isAfter(LocalDateTime.now())) {
            voucher.setStatus(VoucherStatus.ACTIVE.name());
        }

        voucherRepository.save(voucher);
    }

    @Transactional
    public void disableVoucher(Long id, String username) {
        Voucher voucher = voucherRepository.findById(id)
                .orElseThrow(() -> new BusinessException(ErrorCode.RESOURCE_NOT_FOUND));

        voucher.setStatus(VoucherStatus.DISABLED.name());
        voucher.setUpdatedBy(username);
        voucherRepository.save(voucher);
        log.info("Voucher {} disabled by {}", voucher.getCode(), username);
    }

    @Transactional
    public void reactivateVoucher(Long id, String username) {
        Voucher voucher = voucherRepository.findById(id)
                .orElseThrow(() -> new BusinessException(ErrorCode.RESOURCE_NOT_FOUND, "Không tìm thấy voucher"));

        if (voucher.getValidTo() != null && voucher.getValidTo().isBefore(LocalDateTime.now())) {
            throw new BusinessException(ErrorCode.VALIDATION_ERROR, "Voucher đã hết hạn, không thể kích hoạt lại");
        }

        voucher.setStatus(VoucherStatus.ACTIVE.name());
        voucher.setUpdatedBy(username);
        voucherRepository.save(voucher);
        log.info("Voucher {} re-activated by {}", voucher.getCode(), username);
    }
}