package com.clothshop.admin.services;

import com.clothshop.common.exceptions.BusinessException;
import com.clothshop.common.exceptions.ErrorCode;
import com.clothshop.domain.entities.auth.Account;
import com.clothshop.domain.entities.product.InventoryLog;
import com.clothshop.domain.entities.product.ProductVariant;
import com.clothshop.domain.repositories.auth.AccountRepository;
import com.clothshop.domain.repositories.product.InventoryLogRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

/**
 * Sử dụng Propagation.REQUIRED để đảm bảo log luôn chạy chung transaction với nghiệp vụ chính.
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class InventoryLogService {

    private final InventoryLogRepository inventoryLogRepository;
    private final AccountRepository accountRepository;

    /**
     * Ghi lại nhật ký thay đổi kho.
     * @param variant Biến thể sản phẩm thay đổi
     * @param delta Số lượng thay đổi (dương là nhập, âm là xuất)
     * @param newStock Số tồn kho sau khi thay đổi
     * @param reason Lý do thay đổi (bắt buộc)
     * @param note Ghi chú bổ sung (optional)
     * @param username Người thực hiện (lấy từ Security Context)
     */
    @Transactional(propagation = Propagation.REQUIRED)
    public void logChange(ProductVariant variant, int delta, int newStock, String reason, String note, String username) {
        log.debug("Logging stock change for variant SKU: {}, delta: {}, reason: {}", variant.getSku(), delta, reason);

        // 1. Tìm thông tin Staff thông qua Account username
        // Dùng method JOIN FETCH có sẵn để lấy staff nhanh nhất, tránh N+1 query.
        Account account = accountRepository.findByUsernameWithStaffAndRole(username)
                .orElseThrow(() -> new BusinessException(ErrorCode.USER_NOT_EXISTED, "Không tìm thấy thông tin nhân viên thực hiện"));

        if (account.getStaff() == null) {
            throw new BusinessException(ErrorCode.OPERATION_NOT_ALLOWED, "Tài khoản thực hiện không phải là nhân viên");
        }

        // 2. Khởi tạo record log
        InventoryLog inventoryLog = InventoryLog.builder()
                .productVariant(variant)
                .staff(account.getStaff())
                .changeQty(delta)
                .newStock(newStock)
                .reason(reason)
                .note(note) // Add note field
                .isActive(true)
                .createdBy(username)
                .build();

        // 3. Lưu vào database
        inventoryLogRepository.save(inventoryLog);
    }

    /**
     * Overloaded method for backward compatibility (without note).
     */
    @Transactional(propagation = Propagation.REQUIRED)
    public void logChange(ProductVariant variant, int delta, int newStock, String reason, String username) {
        logChange(variant, delta, newStock, reason, null, username);
    }
}