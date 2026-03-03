package com.clothshop.admin.mappers;

import com.clothshop.admin.dtos.request.staff.StaffCreateRequest;
import com.clothshop.admin.dtos.request.staff.StaffUpdateRequest;
import com.clothshop.admin.dtos.response.staff.StaffResponse;
import com.clothshop.domain.entities.auth.Staff;
import org.mapstruct.*;

@Mapper(
        componentModel = "spring",
        nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE,
        unmappedTargetPolicy = ReportingPolicy.IGNORE // Bỏ qua các trường không map để tránh warning/error
)
public interface StaffMapper {

    /**
     * Entity Staff -> StaffResponse
     * Móc dữ liệu từ Account lồng bên trong Staff và Role
     */
    @Mapping(source = "account.username", target = "username")
    @Mapping(source = "account.email", target = "email")
    @Mapping(source = "account.accountStatus", target = "accountStatus")
    @Mapping(source = "account.createdAt", target = "createdAt")
    @Mapping(source = "account.updatedAt", target = "updatedAt")
    @Mapping(source = "phoneNumber", target = "phone") // Entity là phoneNumber -> DTO là phone
    @Mapping(source = "role.id", target = "roleId")    // Lấy ID của Role cho DTO
    @Mapping(source = "role.staffRole", target = "staffRole") // Lấy Enum Role
    @Mapping(source = "role.description", target = "roleName") // Dùng description làm tên hiển thị
    StaffResponse toResponse(Staff staff);

    /**
     * StaffCreateRequest -> Entity Staff
     * Lưu ý: Username, Email, Password thuộc về Account nên ta ignore ở đây (Service sẽ xử lý)
     */
    @Mapping(target = "id", ignore = true)
    @Mapping(target = "account", ignore = true)
    @Mapping(target = "role", ignore = true)
    @Mapping(source = "phone", target = "phoneNumber") // DTO phone -> Entity phoneNumber
    Staff toEntity(StaffCreateRequest request);

    /**
     * StaffUpdateRequest -> Entity Staff (Update)
     * Chỉ update thông tin cá nhân của Staff (FullName, Phone, Avatar)
     */
    @Mapping(target = "id", ignore = true)
    @Mapping(target = "account", ignore = true)
    @Mapping(target = "role", ignore = true)
    @Mapping(source = "phone", target = "phoneNumber") // DTO phone -> Entity phoneNumber
    void updateEntityFromRequest(StaffUpdateRequest request, @MappingTarget Staff staff);
}