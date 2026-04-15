package com.clothshop.admin.services;

import com.clothshop.admin.dtos.response.customer.CustomerAdminResponse;
import com.clothshop.admin.mappers.CustomerAdminMapper;
import com.clothshop.common.exceptions.BusinessException;
import com.clothshop.common.exceptions.ErrorCode;
import com.clothshop.domain.enums.AccountStatus;
import com.clothshop.domain.models.auth.Account;
import com.clothshop.domain.models.auth.Customer;
import com.clothshop.domain.repositories.auth.AccountRepository;
import com.clothshop.domain.repositories.auth.CustomerRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Slf4j
public class CustomerManagementService {

    private final CustomerRepository customerRepository;
    private final AccountRepository accountRepository;
    private final CustomerAdminMapper customerMapper;

    /**
     * Lấy danh sách khách hàng phân trang (Chỉ lấy những khách đang active theo mặc định)
     */
    @Transactional(readOnly = true)
    public Page<CustomerAdminResponse> getAllCustomers(Pageable pageable) {
        return customerRepository.findAll(pageable)
                .map(customerMapper::toResponse);
    }

    /**
     * Vô hiệu hóa hoặc Kích hoạt lại tài khoản khách hàng
     */
    @Transactional
    public void toggleStatus(Long id) {
        // 1. Tìm khách hàng (Dùng hàm bypass SQLRestriction đã viết ở Repo)
        Customer customer = customerRepository.findByIdIncludeDeleted(id)
                .orElseThrow(() -> new BusinessException(ErrorCode.RESOURCE_NOT_FOUND, "Không tìm thấy khách hàng"));

        Account account = customer.getAccount();

        // 2. Đảo ngược trạng thái isActive của Customer (Soft delete)
        boolean newStatus = !customer.getIsActive();
        customer.setIsActive(newStatus);

        // 3. Đồng bộ trạng thái vào Account để chặn/cho phép đăng nhập
        if (newStatus) {
            account.setAccountStatus(AccountStatus.ACTIVE);
        } else {
            account.setAccountStatus(AccountStatus.LOCKED);
        }

        // 4. Lưu cả hai
        customerRepository.save(customer);
        accountRepository.save(account);

        log.info("Trạng thái của khách hàng {} (ID: {}) đã chuyển thành: {}",
                customer.getFullName(), id, newStatus ? "ACTIVE" : "DEACTIVATED");
    }
}