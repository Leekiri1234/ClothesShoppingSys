package com.clothshop.domain.entities.auth;

import com.clothshop.domain.entities.base.BaseEntity;
import com.clothshop.domain.enums.StaffRole;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.SQLDelete;
import org.hibernate.annotations.SQLRestriction;

/**
 * Role entity for staff permissions.
 * Uses StaffRole enum directly as the primary identifier.
 *
 * Design:
 * - staff_role: StaffRole enum (SUPER_ADMIN, MARKETING_STAFF, etc.) - PRIMARY KEY
 * - role_slug: Human-readable URL slug
 * - description: Detailed permission description
 */
@Entity
@Table(name = "roles", indexes = @Index(name = "idx_role_slug", columnList = "role_slug"))
@SQLDelete(sql = "UPDATE roles SET is_active = false WHERE id = ?")
@SQLRestriction("is_active = true")
@AttributeOverride(name = "id", column = @Column(name = "role_id"))
@Getter @Setter
public class Role extends BaseEntity {

    @Enumerated(EnumType.STRING)
    @Column(name = "staff_role", unique = true, nullable = false, length = 50)
    private StaffRole staffRole; // SUPER_ADMIN, MARKETING_STAFF, SALE_PRODUCT_STAFF, CUSTOMER_SERVICE

    @Column(name = "role_slug", unique = true, nullable = false, length = 50)
    private String roleSlug; // super-admin, marketing-staff, sale-product-staff, customer-service

    @Lob
    @Column(name = "description")
    private String description;

    @OneToMany(mappedBy = "role", fetch = FetchType.LAZY)
    private java.util.List<Staff> staffs;
}
