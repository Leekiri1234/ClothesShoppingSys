package com.clothshop.domain.enums;

import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * Staff Role Enum - Defines 4 types of staff roles in Admin module.
 *
 * Based on requirements:
 * - SUPER_ADMIN: Full system control (Create/Edit/Delete Staff)
 * - MARKETING_STAFF: Voucher, Banner, Collections, Notifications, Wishlist management
 * - SALE_PRODUCT_STAFF: Product & Collection CRUD, Stock management, Orders, Sales Report
 * - CUSTOMER_SERVICE: Payment verification, Order status, RMA, Feedback approval, Customer management
 */
@Getter
@AllArgsConstructor
public enum StaffRole {
    SUPER_ADMIN("super-admin", "Super Admin",
        "Full system administrator with all privileges including staff account management"),

    MARKETING_STAFF("marketing-staff", "Marketing Staff",
        "Manage vouchers, banners, collections, featured products, and system notifications"),

    SALE_PRODUCT_STAFF("sale-product-staff", "Sale & Product Staff",
        "Manage products, collections, inventory, orders, and view sales reports"),

    CUSTOMER_SERVICE("customer-service", "Customer Service",
        "Handle payment verification, order status, RMA requests, customer feedback, and customer accounts");

    private final String roleSlug;
    private final String displayName;
    private final String description;
}

