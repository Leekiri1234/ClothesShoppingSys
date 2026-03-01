package com.clothshop.domain.config;

import com.clothshop.domain.entities.auth.*;
import com.clothshop.domain.entities.product.*;
import com.clothshop.domain.enums.*;
import com.clothshop.domain.repositories.auth.*;
import com.clothshop.domain.repositories.product.*;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;

/**
 * Database Seeder - Seeds initial data for development/testing.
 * Runs only once when database is empty (checks if roles table has records).
 *
 * Architecture:
 * - Located in shop-domain (infrastructure layer)
 * - Uses Repositories directly (no Service layer needed for seeding)
 * - Transactional to ensure data consistency
 * - Executes on application startup via CommandLineRunner
 *
 * Default Seeded Data:
 * 1. Roles: 4 staff roles (SUPER_ADMIN, MARKETING_STAFF, SALE_PRODUCT_STAFF, CUSTOMER_SERVICE)
 * 2. Accounts: 1 admin (admin/admin@123), 1 customer (customer/customer@123)
 * 3. Categories: 5 categories (Men Fashion, Women Fashion, Accessories, Shoes, Bags)
 * 4. Products: 3 sample products with variants and images
 *
 * To disable: Remove @Component annotation or set spring.jpa.hibernate.ddl-auto=none
 * To modify: Edit the seedXXX() methods and restart application (will only run if DB is empty)
 */
@Component
@RequiredArgsConstructor
@Slf4j
public class DatabaseSeeder implements CommandLineRunner {

    private final RoleRepository roleRepository;
    private final AccountRepository accountRepository;
    private final StaffRepository staffRepository;
    private final CustomerRepository customerRepository;
    private final CategoryRepository categoryRepository;
    private final ProductRepository productRepository;
    private final ProductVariantRepository productVariantRepository;
    private final ProductImageRepository productImageRepository;
    private final PasswordEncoder passwordEncoder;

    @Override
    @Transactional
    public void run(String... args) {
        // Only seed if database is empty
        if (roleRepository.count() > 0) {
            log.info("Database already seeded. Skipping...");
            return;
        }

        log.info("Starting database seeding...");

        seedRoles();
        seedAccounts();
        seedCategories();
        seedProducts();

        log.info("Database seeding completed successfully!");
    }

    /**
     * 1. Seed Roles (4 Staff Roles)
     */
    private void seedRoles() {
        log.info("Seeding roles...");

        Role superAdminRole = createRole(StaffRole.SUPER_ADMIN, "super-admin",
            "Full system administrator");

        Role marketingRole = createRole(StaffRole.MARKETING_STAFF, "marketing-staff",
            "Manage marketing content");

        Role saleRole = createRole(StaffRole.SALE_PRODUCT_STAFF, "sale-product-staff",
            "Manage sales and products");

        Role customerServiceRole = createRole(StaffRole.CUSTOMER_SERVICE, "customer-service",
            "Handle customer support");

        roleRepository.save(superAdminRole);
        roleRepository.save(marketingRole);
        roleRepository.save(saleRole);
        roleRepository.save(customerServiceRole);

        log.info("Roles seeded: 4 roles");
    }

    /**
     * 2. Seed Accounts (Admin + Customer)
     * Password: admin@123 and customer@123
     */
    private void seedAccounts() {
        log.info("Seeding accounts...");

        // Admin Account
        Account adminAccount = createAccount("admin", "admin@123", "admin@clothshop.com",
            AccountType.STAFF, AccountStatus.ACTIVE);
        accountRepository.save(adminAccount);

        // Create Staff for Admin
        Role superAdminRole = roleRepository.findByStaffRole(StaffRole.SUPER_ADMIN)
            .orElseThrow(() -> new RuntimeException("SUPER_ADMIN role not found"));

        Staff staff = new Staff();
        staff.setFullName("System Administrator");
        staff.setPhoneNumber("0901234567");
        staff.setRole(superAdminRole);
        staff.setAccount(adminAccount);
        staff.setCreatedBy("SYSTEM");
        staffRepository.save(staff);

        // Customer Account
        Account customerAccount = createAccount("customer", "customer@123", "customer@email.com",
            AccountType.CUSTOMER, AccountStatus.ACTIVE);
        accountRepository.save(customerAccount);

        // Create Customer
        Customer customer = new Customer();
        customer.setFullName("Nguyen Van A");
        customer.setEmail("customer@email.com");
        customer.setPhoneNumber("0909876543");
        customer.setAddress("123 Nguyen Hue, District 1, Ho Chi Minh City");
        customer.setAccount(customerAccount);
        customer.setCreatedBy("SYSTEM");
        customerRepository.save(customer);

        log.info("Accounts seeded: 1 admin, 1 customer");
    }

    /**
     * 3. Seed Categories
     */
    private void seedCategories() {
        log.info("Seeding categories...");

        Category menFashion = createCategory("Men Fashion", "men-fashion", "ACTIVE");
        Category womenFashion = createCategory("Women Fashion", "women-fashion", "ACTIVE");
        Category accessories = createCategory("Accessories", "accessories", "ACTIVE");
        Category shoes = createCategory("Shoes", "shoes", "ACTIVE");
        Category bags = createCategory("Bags", "bags", "ACTIVE");

        categoryRepository.save(menFashion);
        categoryRepository.save(womenFashion);
        categoryRepository.save(accessories);
        categoryRepository.save(shoes);
        categoryRepository.save(bags);

        log.info("Categories seeded: 5 categories");
    }

    /**
     * 4. Seed Products (with Variants and Images)
     */
    private void seedProducts() {
        log.info("Seeding products...");

        Category menFashion = categoryRepository.findByCategorySlug("men-fashion")
            .orElseThrow(() -> new RuntimeException("Men Fashion category not found"));

        Category womenFashion = categoryRepository.findByCategorySlug("women-fashion")
            .orElseThrow(() -> new RuntimeException("Women Fashion category not found"));

        // Product 1: Classic White T-Shirt
        Product tshirt = createProduct(menFashion, "Classic White T-Shirt",
            "classic-white-t-shirt", "Premium cotton t-shirt",
            new BigDecimal("199000"), ProductStatus.ACTIVE);
        productRepository.save(tshirt);

        // Variants for T-Shirt
        ProductVariant tshirtSizeS = createVariant(tshirt, "TSHIRT_WHT_S", "White", "S",
            50, new BigDecimal("199000"), "/images/products/tshirt-white-s.jpg");
        ProductVariant tshirtSizeM = createVariant(tshirt, "TSHIRT_WHT_M", "White", "M",
            100, new BigDecimal("199000"), "/images/products/tshirt-white-m.jpg");
        productVariantRepository.save(tshirtSizeS);
        productVariantRepository.save(tshirtSizeM);

        // Image for T-Shirt
        ProductImage tshirtImage = createProductImage(tshirt,
            "/images/products/tshirt-white-main.jpg", 1, true);
        productImageRepository.save(tshirtImage);

        // Product 2: Slim Fit Denim Jeans
        Product jeans = createProduct(menFashion, "Slim Fit Denim Jeans",
            "slim-fit-denim-jeans", "Comfortable stretch denim",
            new BigDecimal("599000"), ProductStatus.ACTIVE);
        productRepository.save(jeans);

        // Product 3: Floral Summer Dress
        Product dress = createProduct(womenFashion, "Floral Summer Dress",
            "floral-summer-dress", "Light and breezy dress",
            new BigDecimal("450000"), ProductStatus.ACTIVE);
        productRepository.save(dress);

        log.info("Products seeded: 3 products with variants and images");
    }

    // ==================== Helper Methods ====================

    private Role createRole(StaffRole staffRole, String slug, String description) {
        Role role = new Role();
        role.setStaffRole(staffRole);
        role.setRoleSlug(slug);
        role.setDescription(description);
        role.setCreatedBy("SYSTEM");
        return role;
    }

    private Account createAccount(String username, String rawPassword, String email,
                                   AccountType type, AccountStatus status) {
        Account account = new Account();
        account.setUsername(username);
        account.setPassword(passwordEncoder.encode(rawPassword));
        account.setEmail(email);
        account.setAccountType(type);
        account.setAccountStatus(status);
        account.setCreatedBy("SYSTEM");
        return account;
    }

    private Category createCategory(String name, String slug, String status) {
        Category category = new Category();
        category.setCategoryName(name);
        category.setCategorySlug(slug);
        category.setCatStatus(status);
        category.setCreatedBy("admin");
        return category;
    }

    private Product createProduct(Category category, String name, String slug,
                                   String description, BigDecimal price, ProductStatus status) {
        Product product = new Product();
        product.setCategory(category);
        product.setProductName(name);
        product.setProductSlug(slug);
        product.setProductDesc(description);
        product.setBasePrice(price);
        product.setProdStatus(status);
        product.setCreatedBy("admin");
        return product;
    }

    private ProductVariant createVariant(Product product, String sku, String color,
                                         String size, Integer stock, BigDecimal price, String imageUrl) {
        ProductVariant variant = new ProductVariant();
        variant.setProduct(product);
        variant.setSku(sku);
        variant.setColor(color);
        variant.setSizeValue(size);
        variant.setStockQuantity(stock);
        variant.setRetailPrice(price);
        variant.setImageUrl(imageUrl);
        variant.setCreatedBy("admin");
        return variant;
    }

    private ProductImage createProductImage(Product product, String imageUrl,
                                            Integer sortOrder, Boolean isMain) {
        ProductImage image = new ProductImage();
        image.setProduct(product);
        image.setImageUrl(imageUrl);
        image.setSortOrder(sortOrder);
        image.setIsMain(isMain);
        image.setCreatedBy("admin");
        return image;
    }
}

