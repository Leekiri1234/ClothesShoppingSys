package com.clothshop.admin.services;

import com.clothshop.admin.dtos.request.staff.StaffCreateRequest;
import com.clothshop.admin.dtos.request.staff.StaffFilterRequest;
import com.clothshop.admin.dtos.request.staff.StaffUpdateRequest;
import com.clothshop.admin.dtos.response.staff.StaffResponse;
import com.clothshop.admin.mappers.StaffMapper;
import com.clothshop.common.dtos.response.PageResponse;
import com.clothshop.common.exceptions.BusinessException;
import com.clothshop.common.exceptions.ErrorCode;
import com.clothshop.domain.entities.auth.Account;
import com.clothshop.domain.entities.auth.Role;
import com.clothshop.domain.entities.auth.Staff;
import com.clothshop.domain.enums.AccountStatus;
import com.clothshop.domain.enums.AccountType;
import com.clothshop.domain.repositories.auth.AccountRepository;
import com.clothshop.domain.repositories.auth.RoleRepository;
import com.clothshop.domain.repositories.auth.StaffRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional(rollbackFor = Exception.class)
public class StaffManagementService {

    private final StaffRepository staffRepository;
    private final AccountRepository accountRepository;
    private final StaffMapper staffMapper;
    private final PasswordEncoder passwordEncoder;
    private final RoleRepository roleRepository;

    /**
     * Helper method: Lấy username của user đang đăng nhập
     */
    private String getCurrentUsername() {
        return SecurityContextHolder.getContext().getAuthentication().getName();
    }

    /**
     * Lấy danh sách nhân viên kèm phân trang và tìm kiếm
     */
    @Transactional(readOnly = true)
    public PageResponse<StaffResponse> getAllStaff(StaffFilterRequest filter, Pageable pageable) {
        AccountStatus status = filter.getStatus();

        Page<Staff> staffPage = staffRepository.findAllWithFilter(
                filter.getKeyword(),
                filter.getRoleId(),
                status,
                pageable
        );

        List<StaffResponse> contentList = staffPage.getContent().stream()
                .map(staffMapper::toResponse)
                .toList();

        return PageResponse.<StaffResponse>builder()
                .content(contentList)
                .pageNumber(staffPage.getNumber())
                .pageSize(staffPage.getSize())
                .totalElements(staffPage.getTotalElements())
                .totalPages(staffPage.getTotalPages())
                .first(staffPage.isFirst())
                .last(staffPage.isLast())
                .build();
    }

    /**
     * Lấy chi tiết nhân viên theo ID (Dùng cho trang Edit)
     */
    @Transactional(readOnly = true)
    public StaffResponse getStaffById(Long id) {
        Staff staff = staffRepository.findByIdWithRelations(id)
                .orElseThrow(() -> new BusinessException(ErrorCode.RESOURCE_NOT_FOUND, "Không tìm thấy nhân viên"));
        return staffMapper.toResponse(staff);
    }

    /**
     * Tạo mới tài khoản nhân viên
     */
    public StaffResponse createStaff(StaffCreateRequest request) {
        if (accountRepository.existsByUsername(request.getUsername())) {
            throw new BusinessException(ErrorCode.DUPLICATE_RESOURCE, "Username đã tồn tại");
        }
        if (accountRepository.existsByEmail(request.getEmail())) {
            throw new BusinessException(ErrorCode.DUPLICATE_RESOURCE, "Email đã tồn tại");
        }

        Role role = roleRepository.findById(request.getRoleId())
                .orElseThrow(() -> new BusinessException(ErrorCode.RESOURCE_NOT_FOUND, "Role không tồn tại"));

        // Tạo Account trước
        Account account = Account.builder()
                .username(request.getUsername())
                .email(request.getEmail())
                .password(passwordEncoder.encode(request.getPassword()))
                .accountType(AccountType.STAFF)
                .accountStatus(AccountStatus.ACTIVE)
                .isActive(true)
                .build();

        Account savedAccount = accountRepository.save(account);

        // Tạo Staff và liên kết với Account
        Staff staff = staffMapper.toEntity(request);
        staff.setAccount(savedAccount);
        staff.setRole(role);
        staff.setIsActive(true);

        return staffMapper.toResponse(staffRepository.save(staff));
    }

    /**
     * Cập nhật thông tin nhân viên
     */
    public StaffResponse updateStaff(Long id, StaffUpdateRequest request) {
        Staff staff = staffRepository.findByIdWithRelations(id)
                .orElseThrow(() -> new BusinessException(ErrorCode.RESOURCE_NOT_FOUND, "Không tìm thấy nhân viên"));

        Role currentRole = staff.getRole();
        Account account = staff.getAccount();

        // CONSTRAINT: Nếu staff được edit có role SUPER_ADMIN
        // -> Chỉ cho phép chính người đó edit, không cho SUPER_ADMIN khác edit
        if (currentRole.getStaffRole() == com.clothshop.domain.enums.StaffRole.SUPER_ADMIN) {
            String currentUsername = getCurrentUsername();
            if (!account.getUsername().equals(currentUsername)) {
                throw new BusinessException(ErrorCode.OPERATION_NOT_ALLOWED,
                    "Chỉ có thể chỉnh sửa thông tin của chính mình. Không thể chỉnh sửa thông tin SUPER_ADMIN khác.");
            }
        }

        // 1. Cập nhật thông tin cá nhân (fullName, phone, avatar) thông qua Mapper
        staffMapper.updateEntityFromRequest(request, staff);

        // 2. Cập nhật Account (Email)
        if (!account.getEmail().equalsIgnoreCase(request.getEmail())) {
            if (accountRepository.existsByEmail(request.getEmail())) {
                throw new BusinessException(ErrorCode.DUPLICATE_RESOURCE, "Email đã được sử dụng");
            }
            account.setEmail(request.getEmail());
        }

        // 3. Cập nhật Role (chỉ nếu roleId khác với role hiện tại)
        if (request.getRoleId() != null && !currentRole.getId().equals(request.getRoleId())) {
            // CONSTRAINT: Không cho phép thay đổi role của SUPER_ADMIN
            if (currentRole.getStaffRole() == com.clothshop.domain.enums.StaffRole.SUPER_ADMIN) {
                throw new BusinessException(ErrorCode.OPERATION_NOT_ALLOWED,
                    "Không thể thay đổi role của tài khoản SUPER_ADMIN");
            }

            Role newRole = roleRepository.findById(request.getRoleId())
                    .orElseThrow(() -> new BusinessException(ErrorCode.RESOURCE_NOT_FOUND, "Role không tồn tại"));
            staff.setRole(newRole);
        }

        return staffMapper.toResponse(staffRepository.save(staff));
    }

    /**
     * Vô hiệu hoá tài khoản nhân viên
     */
    @Transactional
    public void toggleStaffStatus(Long id) {
        Staff staff = staffRepository.findByIdWithRelations(id)
                .orElseThrow(() -> new BusinessException(ErrorCode.USER_NOT_EXISTED,"Không tìm thấy nhân viên"));

        // CONSTRAINT: SUPER_ADMIN không được deactivate SUPER_ADMIN khác
        if (staff.getRole().getStaffRole() == com.clothshop.domain.enums.StaffRole.SUPER_ADMIN) {
            throw new BusinessException(ErrorCode.OPERATION_NOT_ALLOWED,
                "Không thể khóa/mở khóa tài khoản SUPER_ADMIN");
        }

        Account acc = staff.getAccount();

        if (acc.getAccountStatus() == AccountStatus.ACTIVE) {
            // Hành động KHÓA
            acc.setAccountStatus(AccountStatus.LOCKED);
            acc.setIsActive(false);
            staff.setIsActive(false);
        } else {
            // Hành động MỞ KHÓA
            acc.setAccountStatus(AccountStatus.ACTIVE);
            acc.setIsActive(true);
            staff.setIsActive(true);
        }

        // Lưu lại cả 2
        accountRepository.save(acc);
        staffRepository.save(staff);
    }
}