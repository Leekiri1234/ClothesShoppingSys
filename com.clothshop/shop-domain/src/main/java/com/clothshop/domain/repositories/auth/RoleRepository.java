package com.clothshop.domain.repositories.auth;

import com.clothshop.domain.entities.auth.Role;
import com.clothshop.domain.enums.StaffRole;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

/**
 * Repository for Role entity.
 * Uses StaffRole enum as the primary identifier.
 *
 * Usage:
 * - findByStaffRole(StaffRole.SUPER_ADMIN) to get role by enum
 * - findByRoleSlug("super-admin") to get role by slug
 */
@Repository
public interface RoleRepository extends JpaRepository<Role, Long> {

    Optional<Role> findByRoleSlug(String roleSlug);

    /**
     * Find role by StaffRole enum (primary identifier).
     * This is the recommended way to query roles.
     */
    Optional<Role> findByStaffRole(StaffRole staffRole);

    boolean existsByStaffRole(StaffRole staffRole);
}
