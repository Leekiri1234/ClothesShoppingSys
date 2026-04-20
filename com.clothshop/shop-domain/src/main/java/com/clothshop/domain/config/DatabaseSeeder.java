package com.clothshop.domain.config;

import com.clothshop.domain.models.auth.*;
import com.clothshop.domain.models.cms.*;
import com.clothshop.domain.models.customer.*;
import com.clothshop.domain.models.marketing.*;
import com.clothshop.domain.models.order.*;
import com.clothshop.domain.models.product.*;
import com.clothshop.domain.enums.*;
import com.clothshop.domain.enums.OrderStatus;
import com.clothshop.domain.repositories.auth.*;
import com.clothshop.domain.repositories.cms.*;
import com.clothshop.domain.repositories.customer.*;
import com.clothshop.domain.repositories.marketing.*;
import com.clothshop.domain.repositories.order.*;
import com.clothshop.domain.repositories.product.*;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.HashMap;
import java.util.Map;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;
import org.springframework.jdbc.core.JdbcTemplate;

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
 * 4. Products: 13 sample products with variants and images
 * 5. Collections: 2 collections (Summer, Winter)
 * 6. Featured Products: 5 products for homepage
 * 7. Vouchers & Orders: 3 vouchers and 5 sample client orders
 * 8. Banners: 2 promotional banners
 * 9. Flash Sales: 1 ongoing flash sale with 3 products
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
    private final ProductFeedbackRepository productFeedbackRepository;
    private final ProductVariantRepository productVariantRepository;
    private final ProductImageRepository productImageRepository;
    private final WishlistRepository wishlistRepository;
    private final WishlistItemRepository wishlistItemRepository;
    private final CollectionRepository collectionRepository;
    private final CollectionItemRepository collectionItemRepository;
    private final VoucherRepository voucherRepository;
    private final VoucherRedemptionRepository voucherRedemptionRepository;
    private final OrderRepository orderRepository;
    private final OrderItemRepository orderItemRepository;
    private final OrderStatusHistoryRepository orderStatusHistoryRepository;
    private final PaymentRepository paymentRepository;
    private final RmaRequestRepository rmaRequestRepository;
    private final PasswordEncoder passwordEncoder;
    private final FeaturedProductRepository featuredProductRepository;
    private final BannerRepository bannerRepository;
    private final FlashSaleRepository flashSaleRepository;
    private final FlashSaleItemRepository flashSaleItemRepository;
    private final JdbcTemplate jdbcTemplate;

    @Override
    @Transactional
    public void run(String... args) {
        if (roleRepository.count() > 0) {
            log.info("Base data already seeded. Checking supplemental banner/order/RMA data...");
            seedSupplementalCustomers();
            seedCollections();
            seedBanners();
            seedVouchersAndOrders();
            seedWishlists();
            seedProductFeedbacks();
            seedRmaRequests();
            log.info("Supplemental seeding completed.");
            return;
        }

        log.info("Starting database seeding...");

        seedRoles();
        seedAccounts();
        seedCategories();
        seedProducts();
        seedCollections();
        seedFeaturedProducts();
        seedVouchersAndOrders();
        seedWishlists();
        seedProductFeedbacks();
        seedBanners();
        seedFlashSales();
        seedRmaRequests();

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
     * 2. Seed Accounts (Admin + Staff + Customers)
     * Password: admin@123, marketing@123, sale@123, support@123, customer@123
     */
    private void seedAccounts() {
        log.info("Seeding accounts...");

        // 1. Super Admin Account
        Account adminAccount = createAccount("admin", "admin@123", "admin@clothshop.com",
            AccountType.STAFF, AccountStatus.ACTIVE);
        accountRepository.save(adminAccount);

        Role superAdminRole = roleRepository.findByStaffRole(StaffRole.SUPER_ADMIN)
            .orElseThrow(() -> new RuntimeException("SUPER_ADMIN role not found"));

        Staff superAdmin = new Staff();
        superAdmin.setFullName("System Administrator");
        superAdmin.setPhoneNumber("0901234567");
        superAdmin.setRole(superAdminRole);
        superAdmin.setAccount(adminAccount);
        superAdmin.setCreatedBy("SYSTEM");
        staffRepository.save(superAdmin);

        // 2. Marketing Staff Account
        Account marketingAccount = createAccount("marketing", "marketing@123", "marketing@clothshop.com",
            AccountType.STAFF, AccountStatus.ACTIVE);
        accountRepository.save(marketingAccount);

        Role marketingRole = roleRepository.findByStaffRole(StaffRole.MARKETING_STAFF)
            .orElseThrow(() -> new RuntimeException("MARKETING_STAFF role not found"));

        Staff marketingStaff = new Staff();
        marketingStaff.setFullName("Nguyen Van Marketing");
        marketingStaff.setPhoneNumber("0902345678");
        marketingStaff.setRole(marketingRole);
        marketingStaff.setAccount(marketingAccount);
        marketingStaff.setCreatedBy("SYSTEM");
        staffRepository.save(marketingStaff);

        // 3. Sale Product Staff Account
        Account saleAccount = createAccount("sale", "sale@123", "sale@clothshop.com",
            AccountType.STAFF, AccountStatus.ACTIVE);
        accountRepository.save(saleAccount);

        Role saleRole = roleRepository.findByStaffRole(StaffRole.SALE_PRODUCT_STAFF)
            .orElseThrow(() -> new RuntimeException("SALE_PRODUCT_STAFF role not found"));

        Staff saleStaff = new Staff();
        saleStaff.setFullName("Tran Thi Sale");
        saleStaff.setPhoneNumber("0903456789");
        saleStaff.setRole(saleRole);
        saleStaff.setAccount(saleAccount);
        saleStaff.setCreatedBy("SYSTEM");
        staffRepository.save(saleStaff);

        // 4. Customer Service Staff Account
        Account supportAccount = createAccount("support", "support@123", "support@clothshop.com",
            AccountType.STAFF, AccountStatus.ACTIVE);
        accountRepository.save(supportAccount);

        Role customerServiceRole = roleRepository.findByStaffRole(StaffRole.CUSTOMER_SERVICE)
            .orElseThrow(() -> new RuntimeException("CUSTOMER_SERVICE role not found"));

        Staff supportStaff = new Staff();
        supportStaff.setFullName("Le Van Support");
        supportStaff.setPhoneNumber("0904567890");
        supportStaff.setRole(customerServiceRole);
        supportStaff.setAccount(supportAccount);
        supportStaff.setCreatedBy("SYSTEM");
        staffRepository.save(supportStaff);

        // 5. Customer 1
        Account customer1Account = createAccount("customer", "customer@123", "customer@email.com",
            AccountType.CUSTOMER, AccountStatus.ACTIVE);
        accountRepository.save(customer1Account);

        Customer customer1 = new Customer();
        customer1.setFullName("Nguyen Van A");
        customer1.setEmail("customer@email.com");
        customer1.setPhoneNumber("0909876543");
        customer1.setAddress("123 Nguyen Hue, District 1, Ho Chi Minh City");
        customer1.setAccount(customer1Account);
        customer1.setCreatedBy("SYSTEM");
        customerRepository.save(customer1);

        // 6. Customer 2
        Account customer2Account = createAccount("customer2", "customer@123", "customer2@email.com",
            AccountType.CUSTOMER, AccountStatus.ACTIVE);
        accountRepository.save(customer2Account);

        Customer customer2 = new Customer();
        customer2.setFullName("Tran Thi B");
        customer2.setEmail("customer2@email.com");
        customer2.setPhoneNumber("0908765432");
        customer2.setAddress("456 Le Loi, District 3, Ho Chi Minh City");
        customer2.setAccount(customer2Account);
        customer2.setCreatedBy("SYSTEM");
        customerRepository.save(customer2);

        // 7. Customer 3
        Account customer3Account = createAccount("customer3", "customer@123", "customer3@email.com",
            AccountType.CUSTOMER, AccountStatus.ACTIVE);
        accountRepository.save(customer3Account);

        Customer customer3 = new Customer();
        customer3.setFullName("Le Van C");
        customer3.setEmail("customer3@email.com");
        customer3.setPhoneNumber("0907654321");
        customer3.setAddress("789 Tran Hung Dao, District 5, Ho Chi Minh City");
        customer3.setAccount(customer3Account);
        customer3.setCreatedBy("SYSTEM");
        customerRepository.save(customer3);

        // 8-17. Additional customers (customer4 -> customer13)
        for (CustomerSeed seed : buildAdditionalCustomerSeeds()) {
            String username = seed.username();
            String email = seed.email();

            Account customerAccount = createAccount(username, "customer@123", email,
                AccountType.CUSTOMER, AccountStatus.ACTIVE);
            accountRepository.save(customerAccount);

            Customer customer = new Customer();
            customer.setFullName(seed.fullName());
            customer.setEmail(email);
            customer.setPhoneNumber(seed.phoneNumber());
            customer.setAddress(seed.address());
            customer.setAccount(customerAccount);
            customer.setCreatedBy("SYSTEM");
            customerRepository.save(customer);
        }

        log.info("Accounts seeded: 4 staff (all roles), 13 customers");
    }

    private void seedSupplementalCustomers() {
        log.info("Seeding supplemental customers (idempotent upsert)...");

        List<CustomerSeed> customerSeeds = new java.util.ArrayList<>();
        customerSeeds.add(new CustomerSeed("customer", "customer@email.com", "Nguyen Van A", "0909876543",
            "123 Nguyen Hue, District 1, Ho Chi Minh City"));
        customerSeeds.add(new CustomerSeed("customer2", "customer2@email.com", "Tran Thi B", "0908765432",
            "456 Le Loi, District 3, Ho Chi Minh City"));
        customerSeeds.add(new CustomerSeed("customer3", "customer3@email.com", "Le Van C", "0907654321",
            "789 Tran Hung Dao, District 5, Ho Chi Minh City"));
        customerSeeds.addAll(buildAdditionalCustomerSeeds());

        int accountsCreated = 0;
        int customersCreated = 0;
        int customersRepaired = 0;

        for (CustomerSeed seed : customerSeeds) {
            Account account = accountRepository.findByUsername(seed.username())
                .orElseGet(() -> {
                    Account created = createAccount(seed.username(), "customer@123", seed.email(),
                        AccountType.CUSTOMER, AccountStatus.ACTIVE);
                    return accountRepository.save(created);
                });

            if (account.getId() != null && account.getCreatedBy() != null && "SYSTEM".equals(account.getCreatedBy())
                && seed.username().equals(account.getUsername()) && account.getEmail() != null && account.getEmail().equals(seed.email())
                && account.getAccountType() == AccountType.CUSTOMER && account.getAccountStatus() == AccountStatus.ACTIVE
                && Boolean.TRUE.equals(account.getIsActive())) {
                // No-op: seed account already correct.
            } else {
                if (account.getAccountType() != AccountType.CUSTOMER) {
                    account.setAccountType(AccountType.CUSTOMER);
                }
                if (account.getAccountStatus() != AccountStatus.ACTIVE) {
                    account.setAccountStatus(AccountStatus.ACTIVE);
                }
                if (!Boolean.TRUE.equals(account.getIsActive())) {
                    account.setIsActive(true);
                }
                if (account.getEmail() == null || account.getEmail().isBlank()) {
                    account.setEmail(seed.email());
                }
                if (account.getCreatedBy() == null) {
                    account.setCreatedBy("SYSTEM");
                }
                accountRepository.save(account);
            }

            if (account.getCustomer() == null) {
                Customer customer = customerRepository.findByEmailOrUsername(seed.email(), seed.username())
                    .orElseGet(Customer::new);

                boolean isNew = customer.getId() == null;
                customer.setFullName(seed.fullName());
                customer.setEmail(seed.email());
                customer.setPhoneNumber(seed.phoneNumber());
                customer.setAddress(seed.address());
                customer.setAccount(account);
                customer.setIsActive(true);
                if (customer.getCreatedBy() == null) {
                    customer.setCreatedBy("SYSTEM");
                }
                customerRepository.save(customer);

                if (isNew) {
                    customersCreated++;
                } else {
                    customersRepaired++;
                }
            } else {
                Customer customer = account.getCustomer();
                boolean changed = false;

                if (!Boolean.TRUE.equals(customer.getIsActive())) {
                    customer.setIsActive(true);
                    changed = true;
                }
                if (customer.getFullName() == null || customer.getFullName().isBlank()
                    || customer.getFullName().matches("(?i)^customer\\s*\\d+$")) {
                    customer.setFullName(seed.fullName());
                    changed = true;
                }
                if (customer.getEmail() == null || customer.getEmail().isBlank()) {
                    customer.setEmail(seed.email());
                    changed = true;
                }
                if (customer.getPhoneNumber() == null || customer.getPhoneNumber().isBlank()) {
                    customer.setPhoneNumber(seed.phoneNumber());
                    changed = true;
                }
                if (customer.getAddress() == null || customer.getAddress().isBlank()) {
                    customer.setAddress(seed.address());
                    changed = true;
                }
                if (changed) {
                    customerRepository.save(customer);
                    customersRepaired++;
                }
            }

            if (account.getId() != null && account.getCreatedAt() != null
                && account.getCreatedBy() != null && "SYSTEM".equals(account.getCreatedBy())
                && account.getUsername().equals(seed.username())) {
                // Existing seeded account.
            } else if (account.getUsername().equals(seed.username())) {
                accountsCreated++;
            }
        }

        log.info("Supplemental customers seeded: accounts created {}, customers created {}, customers repaired {}",
            accountsCreated, customersCreated, customersRepaired);
    }

    /**
     * 3. Seed Categories
     */
    private void seedCategories() {
        log.info("Seeding categories...");

        Category menFashion = createCategory("Men Fashion", "men-fashion", CategoryStatus.ACTIVE);
        Category womenFashion = createCategory("Women Fashion", "women-fashion", CategoryStatus.ACTIVE);
        Category accessories = createCategory("Accessories", "accessories", CategoryStatus.ACTIVE);
        Category shoes = createCategory("Shoes", "shoes", CategoryStatus.ACTIVE);
        Category bags = createCategory("Bags", "bags", CategoryStatus.ACTIVE);

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
        ProductVariant tshirtSizeXS = createVariant(tshirt, "TSHIRT_WHT_XS", "White", "XS",
            50, new BigDecimal("199000"), "https://images.unsplash.com/photo-1521572163474-6864f9cf17ab?w=400");
        ProductVariant tshirtSizeS = createVariant(tshirt, "TSHIRT_WHT_S", "White", "S",
            100, new BigDecimal("199000"), "https://images.unsplash.com/photo-1521572163474-6864f9cf17ab?w=400");
        productVariantRepository.save(tshirtSizeXS);
        productVariantRepository.save(tshirtSizeS);

        // Image for T-Shirt
        ProductImage tshirtImage = createProductImage(tshirt,
            "https://images.unsplash.com/photo-1521572163474-6864f9cf17ab?w=500", 1, true);
        productImageRepository.save(tshirtImage);

        // Product 2: Slim Fit Denim Jeans
        Product jeans = createProduct(menFashion, "Slim Fit Denim Jeans",
            "slim-fit-denim-jeans", "Comfortable stretch denim",
            new BigDecimal("599000"), ProductStatus.ACTIVE);
        productRepository.save(jeans);

        // Variants for Jeans
        ProductVariant jeansM = createVariant(jeans, "JEANS_BLU_M", "Blue", "M",
            40, new BigDecimal("599000"), "https://images.unsplash.com/photo-1542272604-787c3835535d?w=400");
        ProductVariant jeansL = createVariant(jeans, "JEANS_BLU_L", "Blue", "L",
            60, new BigDecimal("599000"), "https://images.unsplash.com/photo-1542272604-787c3835535d?w=400");
        productVariantRepository.save(jeansM);
        productVariantRepository.save(jeansL);

        ProductImage jeansImage = createProductImage(jeans,
            "https://images.unsplash.com/photo-1542272604-787c3835535d?w=500", 1, true);
        productImageRepository.save(jeansImage);

        // Product 3: Floral Summer Dress
        Product dress = createProduct(womenFashion, "Floral Summer Dress",
            "floral-summer-dress", "Light and breezy dress",
            new BigDecimal("450000"), ProductStatus.ACTIVE);
        productRepository.save(dress);

        // Variants for Dress
        ProductVariant dressXS = createVariant(dress, "DRESS_FLO_XS", "Floral Pink", "XS",
            30, new BigDecimal("450000"), "https://images.unsplash.com/photo-1572804013309-59a88b7e92f1?w=400");
        ProductVariant dressS = createVariant(dress, "DRESS_FLO_S", "Floral Pink", "S",
            45, new BigDecimal("450000"), "https://images.unsplash.com/photo-1572804013309-59a88b7e92f1?w=400");
        productVariantRepository.save(dressXS);
        productVariantRepository.save(dressS);

        ProductImage dressImage = createProductImage(dress,
            "https://images.unsplash.com/photo-1572804013309-59a88b7e92f1?w=500", 1, true);
        productImageRepository.save(dressImage);

        // Product 4: Black Leather Jacket
        Product jacket = createProduct(menFashion, "Black Leather Jacket",
            "black-leather-jacket", "Premium genuine leather jacket",
            new BigDecimal("1299000"), ProductStatus.ACTIVE);
        productRepository.save(jacket);

        ProductVariant jacketL = createVariant(jacket, "JKT_BLK_L", "Black", "L",
            20, new BigDecimal("1299000"), "https://images.unsplash.com/photo-1551028719-00167b16eac5?w=400");
        ProductVariant jacketXL = createVariant(jacket, "JKT_BLK_XL", "Black", "XL",
            25, new BigDecimal("1299000"), "https://images.unsplash.com/photo-1551028719-00167b16eac5?w=400");
        productVariantRepository.save(jacketL);
        productVariantRepository.save(jacketXL);

        ProductImage jacketImage = createProductImage(jacket,
            "https://images.unsplash.com/photo-1551028719-00167b16eac5?w=500", 1, true);
        productImageRepository.save(jacketImage);

        // Product 5: Cotton Polo Shirt
        Product polo = createProduct(menFashion, "Cotton Polo Shirt",
            "cotton-polo-shirt", "Classic fit polo shirt",
            new BigDecimal("299000"), ProductStatus.ACTIVE);
        productRepository.save(polo);

        ProductVariant poloM = createVariant(polo, "POLO_NVY_M", "Navy Blue", "M",
            70, new BigDecimal("299000"), "https://images.unsplash.com/photo-1586790170083-2f9ceadc732d?w=400");
        ProductVariant poloL = createVariant(polo, "POLO_WHT_L", "White", "L",
            80, new BigDecimal("299000"), "https://images.unsplash.com/photo-1586790170083-2f9ceadc732d?w=400");
        productVariantRepository.save(poloM);
        productVariantRepository.save(poloL);

        ProductImage poloImage = createProductImage(polo,
            "https://images.unsplash.com/photo-1586790170083-2f9ceadc732d?w=500", 1, true);
        productImageRepository.save(poloImage);

        // Product 6: Casual Chinos
        Product chinos = createProduct(menFashion, "Casual Chinos",
            "casual-chinos", "Comfortable slim fit chinos",
            new BigDecimal("499000"), ProductStatus.ACTIVE);
        productRepository.save(chinos);

        ProductVariant chinosM = createVariant(chinos, "CHINO_BEG_M", "Beige", "M",
            50, new BigDecimal("499000"), "https://images.unsplash.com/photo-1473966968600-fa801b869a1a?w=400");
        ProductVariant chinosL = createVariant(chinos, "CHINO_BEG_L", "Beige", "L",
            55, new BigDecimal("499000"), "https://images.unsplash.com/photo-1473966968600-fa801b869a1a?w=400");
        productVariantRepository.save(chinosM);
        productVariantRepository.save(chinosL);

        ProductImage chinosImage = createProductImage(chinos,
            "https://images.unsplash.com/photo-1473966968600-fa801b869a1a?w=500", 1, true);
        productImageRepository.save(chinosImage);

        // Product 7: Striped Maxi Dress
        Product maxiDress = createProduct(womenFashion, "Striped Maxi Dress",
            "striped-maxi-dress", "Elegant long dress for special occasions",
            new BigDecimal("650000"), ProductStatus.ACTIVE);
        productRepository.save(maxiDress);

        ProductVariant maxiS = createVariant(maxiDress, "MAXI_STR_S", "Blue Stripe", "S",
            25, new BigDecimal("650000"), "https://images.unsplash.com/photo-1595777457583-95e059d581b8?w=400");
        ProductVariant maxiM = createVariant(maxiDress, "MAXI_STR_M", "Blue Stripe", "M",
            35, new BigDecimal("650000"), "https://images.unsplash.com/photo-1595777457583-95e059d581b8?w=400");
        productVariantRepository.save(maxiS);
        productVariantRepository.save(maxiM);

        ProductImage maxiImage = createProductImage(maxiDress,
            "https://images.unsplash.com/photo-1595777457583-95e059d581b8?w=500", 1, true);
        productImageRepository.save(maxiImage);

        // Product 8: Knit Cardigan
        Product cardigan = createProduct(womenFashion, "Knit Cardigan",
            "knit-cardigan", "Cozy knitted cardigan perfect for layering",
            new BigDecimal("399000"), ProductStatus.ACTIVE);
        productRepository.save(cardigan);

        ProductVariant cardiganM = createVariant(cardigan, "CARD_GRY_M", "Gray", "M",
            40, new BigDecimal("399000"), "https://images.unsplash.com/photo-1434389677669-e08b4cac3105?w=400");
        ProductVariant cardiganL = createVariant(cardigan, "CARD_BEG_L", "Beige", "L",
            45, new BigDecimal("399000"), "https://images.unsplash.com/photo-1434389677669-e08b4cac3105?w=400");
        productVariantRepository.save(cardiganM);
        productVariantRepository.save(cardiganL);

        ProductImage cardiganImage = createProductImage(cardigan,
            "https://images.unsplash.com/photo-1434389677669-e08b4cac3105?w=500", 1, true);
        productImageRepository.save(cardiganImage);

        // Product 9: Graphic Print T-Shirt
        Product graphicTee = createProduct(menFashion, "Graphic Print T-Shirt",
            "graphic-print-t-shirt", "Trendy graphic design t-shirt",
            new BigDecimal("249000"), ProductStatus.ACTIVE);
        productRepository.save(graphicTee);

        ProductVariant graphicM = createVariant(graphicTee, "GTEE_BLK_M", "Black", "M",
            65, new BigDecimal("249000"), "https://images.unsplash.com/photo-1583743814966-8936f5b7be1a?w=400");
        ProductVariant graphicL = createVariant(graphicTee, "GTEE_BLK_L", "Black", "L",
            70, new BigDecimal("249000"), "https://images.unsplash.com/photo-1583743814966-8936f5b7be1a?w=400");
        productVariantRepository.save(graphicM);
        productVariantRepository.save(graphicL);

        ProductImage graphicImage = createProductImage(graphicTee,
            "https://images.unsplash.com/photo-1583743814966-8936f5b7be1a?w=500", 1, true);
        productImageRepository.save(graphicImage);

        // Product 10: High-Waist Skinny Jeans
        Product skinnyJeans = createProduct(womenFashion, "High-Waist Skinny Jeans",
            "high-waist-skinny-jeans", "Flattering high-rise skinny jeans",
            new BigDecimal("549000"), ProductStatus.ACTIVE);
        productRepository.save(skinnyJeans);

        ProductVariant skinnyM = createVariant(skinnyJeans, "SKNY_BLK_M", "Black", "M",
            38, new BigDecimal("549000"), "https://images.unsplash.com/photo-1541099649105-f69ad21f3246?w=400");
        ProductVariant skinnyL = createVariant(skinnyJeans, "SKNY_BLK_L", "Black", "L",
            42, new BigDecimal("549000"), "https://images.unsplash.com/photo-1541099649105-f69ad21f3246?w=400");
        productVariantRepository.save(skinnyM);
        productVariantRepository.save(skinnyL);

        ProductImage skinnyImage = createProductImage(skinnyJeans,
            "https://images.unsplash.com/photo-1541099649105-f69ad21f3246?w=500", 1, true);
        productImageRepository.save(skinnyImage);

        // Product 11: Hooded Sweatshirt
        Product hoodie = createProduct(menFashion, "Hooded Sweatshirt",
            "hooded-sweatshirt", "Warm and comfortable hoodie",
            new BigDecimal("399000"), ProductStatus.ACTIVE);
        productRepository.save(hoodie);

        ProductVariant hoodieXL = createVariant(hoodie, "HOOD_GRY_XL", "Gray", "XL",
            55, new BigDecimal("399000"), "https://images.unsplash.com/photo-1556821840-3a63f95609a7?w=400");
        ProductVariant hoodieXXL = createVariant(hoodie, "HOOD_BLK_XXL", "Black", "XXL",
            60, new BigDecimal("399000"), "https://images.unsplash.com/photo-1556821840-3a63f95609a7?w=400");
        productVariantRepository.save(hoodieXL);
        productVariantRepository.save(hoodieXXL);

        ProductImage hoodieImage = createProductImage(hoodie,
            "https://images.unsplash.com/photo-1556821840-3a63f95609a7?w=500", 1, true);
        productImageRepository.save(hoodieImage);

        // Product 12: Casual Blazer
        Product blazer = createProduct(womenFashion, "Casual Blazer",
            "casual-blazer", "Professional yet comfortable blazer",
            new BigDecimal("799000"), ProductStatus.ACTIVE);
        productRepository.save(blazer);

        ProductVariant blazerS = createVariant(blazer, "BLZR_NVY_S", "Navy", "S",
            28, new BigDecimal("799000"), "https://images.unsplash.com/photo-1591369822096-ffd140ec948f?w=400");
        ProductVariant blazerM = createVariant(blazer, "BLZR_NVY_M", "Navy", "M",
            32, new BigDecimal("799000"), "https://images.unsplash.com/photo-1591369822096-ffd140ec948f?w=400");
        productVariantRepository.save(blazerS);
        productVariantRepository.save(blazerM);

        ProductImage blazerImage = createProductImage(blazer,
            "https://images.unsplash.com/photo-1591369822096-ffd140ec948f?w=500", 1, true);
        productImageRepository.save(blazerImage);

        // Product 13: Linen Shorts
        Product shorts = createProduct(menFashion, "Linen Shorts",
            "linen-shorts", "Breathable summer shorts",
            new BigDecimal("349000"), ProductStatus.ACTIVE);
        productRepository.save(shorts);

        ProductVariant shortsM = createVariant(shorts, "SHRT_KHK_M", "Khaki", "M",
            48, new BigDecimal("349000"), "https://images.unsplash.com/photo-1591195853828-11db59a44f6b?w=400");
        ProductVariant shortsL = createVariant(shorts, "SHRT_KHK_L", "Khaki", "L",
            52, new BigDecimal("349000"), "https://images.unsplash.com/photo-1591195853828-11db59a44f6b?w=400");
        productVariantRepository.save(shortsM);
        productVariantRepository.save(shortsL);

        ProductImage shortsImage = createProductImage(shorts,
            "https://images.unsplash.com/photo-1591195853828-11db59a44f6b?w=500", 1, true);
        productImageRepository.save(shortsImage);

        log.info("Products seeded: 13 products with variants and images");
    }

    /**
     * 5. Seed Collections (2 Collections with 5 products each)
     */
    private void seedCollections() {
        log.info("Seeding collections...");

        // Map product by slug so seeding does not depend on DB row order.
        Map<String, Product> productsBySlug = productRepository.findAll().stream()
            .collect(Collectors.toMap(Product::getProductSlug, p -> p, (left, right) -> left, HashMap::new));

        if (productsBySlug.size() < 10) {
            log.warn("Not enough products to create collections. Skipping collection seeding.");
            return;
        }

        upsertCollectionWithProducts(
            "Summer Collection 2024",
            "summer-collection-2024",
            "Fresh and vibrant styles for the summer season",
            "https://images.unsplash.com/photo-1475180098004-ca77a66827be?w=1200",
            List.of("classic-white-t-shirt", "floral-summer-dress", "cotton-polo-shirt", "linen-shorts", "graphic-print-t-shirt"),
            productsBySlug
        );

        upsertCollectionWithProducts(
            "Winter Essentials 2024",
            "winter-essentials-2024",
            "Stay warm and stylish with our winter collection",
            "https://images.unsplash.com/photo-1483985988355-763728e1935b?w=1200",
            List.of("black-leather-jacket", "hooded-sweatshirt", "knit-cardigan", "casual-blazer", "slim-fit-denim-jeans"),
            productsBySlug
        );

        // New seeded collection #3 with thumbnail.
        upsertCollectionWithProducts(
            "Office Smart 2024",
            "office-smart-2024",
            "Smart office-ready outfits for weekdays",
            "https://images.unsplash.com/photo-1521572163474-6864f9cf17ab?w=1200",
            List.of("casual-blazer", "cotton-polo-shirt", "slim-fit-denim-jeans", "high-waist-skinny-jeans", "knit-cardigan"),
            productsBySlug
        );

        // New seeded collection #4 with thumbnail.
        upsertCollectionWithProducts(
            "Weekend Comfort 2024",
            "weekend-comfort-2024",
            "Relaxed and comfy picks for your weekend",
            "https://images.unsplash.com/photo-1441986300917-64674bd600d8?w=1200",
            List.of("hooded-sweatshirt", "graphic-print-t-shirt", "linen-shorts", "casual-chinos", "striped-maxi-dress"),
            productsBySlug
        );

        log.info("Collections seeded/updated: 4 collections with thumbnail + products");
    }

    private void upsertCollectionWithProducts(String name,
                                              String baseSlug,
                                              String description,
                                              String imageUrl,
                                              List<String> productSlugs,
                                              Map<String, Product> productsBySlug) {
        Collection collection = collectionRepository.findAll().stream()
            .filter(c -> c.getName() != null && c.getName().equalsIgnoreCase(name))
            .findFirst()
            .orElseGet(() -> {
                Collection c = new Collection();
                c.setName(name);
                c.setSlug(baseSlug);
                c.setCreatedBy("admin");
                return collectionRepository.save(c);
            });

        collection.setName(name);
        collection.setDescription(description);
        collection.setImageUrl(imageUrl);
        collection.setSlug(baseSlug + "-c." + collection.getId());
        collection = collectionRepository.save(collection);

        List<Long> existingProductIds = collectionItemRepository.findProductIdsByCollectionId(collection.getId());
        int nextOrder = collectionItemRepository.findMaxDisplayOrderByCollectionId(collection.getId()).orElse(0) + 1;

        for (String productSlug : productSlugs) {
            Product product = productsBySlug.get(productSlug);
            if (product == null) {
                log.warn("Product slug {} not found. Skipping assignment for collection {}", productSlug, name);
                continue;
            }

            if (!existingProductIds.contains(product.getId())) {
                addProductToCollection(collection, product, nextOrder++);
                existingProductIds.add(product.getId());
            }
        }

        log.info("Seeded collection '{}' with thumbnail and {} configured products", name, productSlugs.size());
    }

    /**
     * 6. Seed Featured Products (homepage spotlight)
     */
    private void seedFeaturedProducts() {
        log.info("Seeding featured products...");
        List<Product> products = productRepository.findAll();
        if (products.isEmpty()) {
            log.warn("No products found, skipping featured products seeding.");
            return;
        }

        // Pick first 5 products as featured with display order
        int limit = Math.min(5, products.size());
        for (int i = 0; i < limit; i++) {
            Product product = products.get(i);
            FeaturedProduct featured = FeaturedProduct.builder()
                .product(product)
                .displayOrder(i + 1)
                .startDate(LocalDateTime.now().minusDays(1))
                .endDate(LocalDateTime.now().plusDays(60))
                .createdBy("marketing")
                .build();
            featuredProductRepository.save(featured);
        }

        log.info("Featured products seeded: {}", limit);
    }

    /**
     * 7. Seed Vouchers + Sample Client Orders
     */
    private void seedVouchersAndOrders() {
        log.info("Seeding vouchers and client orders...");

        List<Customer> customers = customerRepository.findAll();
        List<ProductVariant> variants = productVariantRepository.findAll();

        if (customers.isEmpty() || variants.size() < 3) {
            log.warn("Not enough customers or variants to seed orders. Skipping order seeding.");
            return;
        }

        Voucher percentVoucher = voucherRepository.findByCode("WELCOME10").orElseGet(() -> createVoucher(
            "WELCOME10",
            DiscountType.PERCENTAGE,
            new BigDecimal("10"),
            new BigDecimal("300000"),
            new BigDecimal("120000"),
            50,
            LocalDateTime.now().minusDays(5),
            LocalDateTime.now().plusDays(30),
            VoucherStatus.ACTIVE.name()
        ));

        Voucher fixedVoucher = voucherRepository.findByCode("SAVE50K").orElseGet(() -> createVoucher(
            "SAVE50K",
            DiscountType.FIXED_AMOUNT,
            new BigDecimal("50000"),
            new BigDecimal("250000"),
            null,
            100,
            LocalDateTime.now().minusDays(2),
            LocalDateTime.now().plusDays(45),
            VoucherStatus.ACTIVE.name()
        ));

        Voucher expiredVoucher = voucherRepository.findByCode("FLASH15").orElseGet(() -> createVoucher(
            "FLASH15",
            DiscountType.PERCENTAGE,
            new BigDecimal("15"),
            new BigDecimal("200000"),
            new BigDecimal("150000"),
            20,
            LocalDateTime.now().minusDays(40),
            LocalDateTime.now().minusDays(5),
            VoucherStatus.EXPIRED.name()
        ));

        // Orders for single customer with different statuses
        Customer customer = customers.get(0);

        createOrderWithVoucherIfMissing(
            "ORD-1710000001001",
            customer,
            PaymentMethod.COD,
            OrderStatus.PENDING,
            List.of(new ItemSpec(variants.get(0), 1), new ItemSpec(variants.get(1), 2)),
            percentVoucher,
            "Pending confirmation"
        );

        createOrderWithVoucherIfMissing(
            "ORD-1710000001002",
            customer,
            PaymentMethod.MOMO,
            OrderStatus.CONFIRMED,
            List.of(new ItemSpec(variants.get(2), 1), new ItemSpec(variants.get(3), 1)),
            fixedVoucher,
            "Confirmed by system"
        );

        createOrderWithVoucherIfMissing(
            "ORD-1710000001003",
            customer,
            PaymentMethod.VNPAY,
            OrderStatus.SHIPPING,
            List.of(new ItemSpec(variants.get(4), 2)),
            percentVoucher,
            "Shipping to customer"
        );

        createOrderWithVoucherIfMissing(
            "ORD-1710000001004",
            customer,
            PaymentMethod.BANK_TRANSFER,
            OrderStatus.DELIVERED,
            List.of(new ItemSpec(variants.get(5), 1), new ItemSpec(variants.get(6), 1)),
            fixedVoucher,
            "Delivered successfully"
        );

        createOrderWithVoucherIfMissing(
            "ORD-1710000001005",
            customer,
            PaymentMethod.COD,
            OrderStatus.CANCELLED,
            List.of(new ItemSpec(variants.get(7), 1)),
            expiredVoucher,
            "Cancelled by customer"
        );

        createOrderWithVoucherIfMissing(
            "ORD-1710000001006",
            customers.get(1),
            PaymentMethod.MOMO,
            OrderStatus.PENDING,
            List.of(new ItemSpec(variants.get(8), 1), new ItemSpec(variants.get(9), 1)),
            percentVoucher,
            "Pending payment verification"
        );

        createOrderWithVoucherIfMissing(
            "ORD-1710000001007",
            customers.get(2),
            PaymentMethod.BANK_TRANSFER,
            OrderStatus.PENDING,
            List.of(new ItemSpec(variants.get(10), 1)),
            fixedVoucher,
            "Waiting for customer confirmation"
        );

        createOrderWithVoucherIfMissing(
            "ORD-1710000001008",
            customers.get(0),
            PaymentMethod.VNPAY,
            OrderStatus.COMPLETED,
            List.of(new ItemSpec(variants.get(11), 1), new ItemSpec(variants.get(12), 1)),
            percentVoucher,
            "Completed successfully"
        );

        seedMissingOrdersPastMonth(customers, variants, List.of(percentVoucher, fixedVoucher));

        log.info("Vouchers and orders seeded: 3 vouchers, client orders generated");
    }

    private void seedMissingOrdersPastMonth(List<Customer> customers, List<ProductVariant> variants, List<Voucher> vouchers) {
        long currentOrderCount = orderRepository.count();
        int targetOrders = 100;
        if (currentOrderCount >= targetOrders) {
            log.info("Sufficient orders already exist. Skipping generating {} orders.", targetOrders);
            return;
        }

        int ordersToGenerate = targetOrders - (int) currentOrderCount;
        log.info("Generating {} orders over the past month...", ordersToGenerate);

        java.util.Random random = new java.util.Random();
        LocalDateTime startTime = LocalDateTime.now().minusDays(30);
        long stepMinutes = (30L * 24 * 60) / ordersToGenerate;

        for (int i = 0; i < ordersToGenerate; i++) {
            LocalDateTime generatedDate = startTime.plusMinutes(i * stepMinutes).plusMinutes(random.nextInt(60));
            if (generatedDate.isAfter(LocalDateTime.now())) {
                generatedDate = LocalDateTime.now().minusMinutes(1);
            }

            // Consistent and realistic ORD-timestamp format
            String invoice = "ORD-" + generatedDate.toInstant(java.time.ZoneOffset.UTC).toEpochMilli();
            Customer cust = customers.get(random.nextInt(customers.size()));
            PaymentMethod pm = PaymentMethod.values()[random.nextInt(PaymentMethod.values().length)];

            // Bias towards completed/delivered statuses
            OrderStatus[] statuses = {OrderStatus.DELIVERED, OrderStatus.COMPLETED, OrderStatus.SHIPPING, OrderStatus.PENDING, OrderStatus.CANCELLED};
            OrderStatus status = statuses[random.nextInt(statuses.length)];
            // Increase chance of delivered and completed
            if (random.nextBoolean()) {
                status = random.nextBoolean() ? OrderStatus.DELIVERED : OrderStatus.COMPLETED;
            }

            int numItems = random.nextInt(3) + 1;
            List<ItemSpec> items = new java.util.ArrayList<>();
            for (int j = 0; j < numItems; j++) {
                ProductVariant pv = variants.get(random.nextInt(variants.size()));
                items.add(new ItemSpec(pv, random.nextInt(2) + 1));
            }

            Voucher v = random.nextBoolean() ? vouchers.get(random.nextInt(vouchers.size())) : null;

            createOrderWithVoucherAndDate(
                invoice,
                cust,
                pm,
                status,
                items,
                v,
                "Generated order",
                generatedDate
            );
        }
    }

    private void createOrderWithVoucherIfMissing(String invoice, Customer customer, PaymentMethod paymentMethod,
                                                 OrderStatus status, List<ItemSpec> items, Voucher voucher, String note) {
        if (orderRepository.findByOrderInvoice(invoice).isPresent()) {
            log.info("Order {} already exists. Skipping seed for this invoice.", invoice);
            return;
        }
        createOrderWithVoucher(invoice, customer, paymentMethod, status, items, voucher, note);
    }

    /**
     * Helper method to add a product to a collection
     */
    private void addProductToCollection(Collection collection, Product product, int displayOrder) {
        CollectionItem item = new CollectionItem();
        item.setCollection(collection);
        item.setProduct(product);
        item.setDisplayOrder(displayOrder);
        item.setCreatedBy("admin");
        collectionItemRepository.save(item);
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

    private Category createCategory(String name, String slug, CategoryStatus status) {

        Category category = new Category();
        category.setCategoryName(name);
        category.setCategorySlug(slug);
        category.setCatStatus(status);
        category.setIsActive(true);
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

    private Voucher createVoucher(String code, DiscountType discountType, BigDecimal discountValue,
                                  BigDecimal minOrderValue, BigDecimal maxDiscount, Integer usageLimit,
                                  LocalDateTime validFrom, LocalDateTime validTo, String status) {
        Voucher voucher = new Voucher();
        voucher.setCode(code);
        voucher.setDiscountType(discountType);
        voucher.setDiscountValue(discountValue);
        voucher.setMinOrderValue(minOrderValue);
        voucher.setMaxDiscount(maxDiscount);
        voucher.setUsageLimit(usageLimit);
        voucher.setCurrentUsage(0);
        voucher.setValidFrom(validFrom);
        voucher.setValidTo(validTo);
        voucher.setStatus(status);
        voucher.setCreatedBy("marketing");
        return voucherRepository.save(voucher);
    }

    private void createOrderWithVoucher(String invoice, Customer customer, PaymentMethod paymentMethod,
                                        OrderStatus status, List<ItemSpec> items, Voucher voucher, String note) {
        createOrderWithVoucherAndDate(invoice, customer, paymentMethod, status, items, voucher, note, LocalDateTime.now());
    }

    private void createOrderWithVoucherAndDate(String invoice, Customer customer, PaymentMethod paymentMethod,
                                        OrderStatus status, List<ItemSpec> items, Voucher voucher, String note, LocalDateTime createdAt) {
        BigDecimal totalAmount = BigDecimal.ZERO;
        int totalQty = 0;

        for (ItemSpec item : items) {
            BigDecimal line = item.variant().getRetailPrice().multiply(BigDecimal.valueOf(item.quantity()));
            totalAmount = totalAmount.add(line);
            totalQty += item.quantity();
        }

        BigDecimal discount = voucher != null ? calculateDiscount(totalAmount, voucher) : BigDecimal.ZERO;
        BigDecimal finalPrice = totalAmount.subtract(discount);
        if (finalPrice.compareTo(BigDecimal.ZERO) < 0) {
            finalPrice = BigDecimal.ZERO;
        }

        Order order = Order.builder()
            .orderInvoice(invoice)
            .customer(customer)
            .totalQuantity(totalQty)
            .totalAmount(totalAmount.setScale(2, RoundingMode.HALF_UP))
            .discount(discount.setScale(2, RoundingMode.HALF_UP))
            .totalPrice(finalPrice.setScale(2, RoundingMode.HALF_UP))
            .paymentMethod(paymentMethod)
            .status(status)
            .createdBy("SYSTEM")
            .build();

        order.setCreatedAt(createdAt);

        List<OrderItem> orderItems = items.stream()
            .map(item -> OrderItem.builder()
                .order(order)
                .variant(item.variant())
                .quantity(item.quantity())
                .unitPrice(item.variant().getRetailPrice())
                .createdBy("SYSTEM")
                .build())
            .collect(java.util.stream.Collectors.toList());
        order.setOrderItems(orderItems);

        OrderStatusHistory history = OrderStatusHistory.builder()
                .order(order)
                .oldStatus(null) // Đơn mới nên status cũ là null
                .newStatus(status)
                .note(note)
                .changedAt(createdAt) // Thêm trường changedAt nếu Entity yêu cầu
                .createdBy("SYSTEM")
                .build();
        order.setStatusHistory(List.of(history));

        PaymentStatus paymentStatus = status == OrderStatus.PENDING || status == OrderStatus.CANCELLED
            ? PaymentStatus.PENDING
            : PaymentStatus.PAID;

        Payment payment = Payment.builder()
                .order(order)
                .paymentMethod(paymentMethod.name())
                .amount(finalPrice.setScale(2, RoundingMode.HALF_UP))
                .status(paymentStatus)
                .verifiedBy(null) // Dùng verifiedBy thay vì processedBy theo ERD
                .verifiedAt(paymentStatus == PaymentStatus.PAID ? createdAt : null)
                .createdBy("SYSTEM")
                .build();
        order.setPayment(payment);

        Order savedOrder = orderRepository.save(order);
        orderItemRepository.saveAll(orderItems);
        orderStatusHistoryRepository.save(history);
        paymentRepository.save(payment);

        // Update createdAt via JDBC to bypass JPA @CreationTimestamp updatable=false
        jdbcTemplate.update("UPDATE orders SET created_at = ? WHERE order_id = ?", createdAt, savedOrder.getId());
        jdbcTemplate.update("UPDATE order_items SET created_at = ? WHERE order_id = ?", createdAt, savedOrder.getId());
        jdbcTemplate.update("UPDATE order_status_history SET created_at = ?, changed_at = ? WHERE history_id = ?", createdAt, createdAt, history.getId());
        jdbcTemplate.update("UPDATE payments SET created_at = ? WHERE payment_id = ?", createdAt, payment.getId());

        if (voucher != null) {
            voucher.setCurrentUsage((voucher.getCurrentUsage() == null ? 0 : voucher.getCurrentUsage()) + 1);
            voucherRepository.save(voucher);

            VoucherRedemption redemption = VoucherRedemption.builder()
                .voucher(voucher)
                .customer(customer)
                .order(savedOrder)
                .discountAmount(discount.setScale(2, RoundingMode.HALF_UP))
                .createdBy("SYSTEM")
                .build();
            redemption = voucherRedemptionRepository.save(redemption);
            jdbcTemplate.update("UPDATE voucher_redemptions SET created_at = ? WHERE redemption_id = ?", createdAt, redemption.getId());
        }
    }

    private BigDecimal calculateDiscount(BigDecimal totalAmount, Voucher voucher) {
        if (voucher == null) {
            return BigDecimal.ZERO;
        }

        if (voucher.getMinOrderValue() != null && totalAmount.compareTo(voucher.getMinOrderValue()) < 0) {
            return BigDecimal.ZERO;
        }

        BigDecimal discount;
        if (voucher.getDiscountType() == DiscountType.PERCENTAGE) {
            discount = totalAmount.multiply(voucher.getDiscountValue()).divide(BigDecimal.valueOf(100), 2, RoundingMode.HALF_UP);
        } else {
            discount = voucher.getDiscountValue();
        }

        if (voucher.getMaxDiscount() != null && discount.compareTo(voucher.getMaxDiscount()) > 0) {
            discount = voucher.getMaxDiscount();
        }

        return discount;
    }

    private void seedWishlists() {
        log.info("Seeding wishlists...");

        final int targetWishlistItems = 36;
        final String seededBy = "WISHLIST_SEEDER";

        List<Customer> customers = customerRepository.findAll().stream()
            .sorted(Comparator.comparing(Customer::getId))
            .toList();
        List<Product> products = productRepository.findAll().stream()
            .filter(p -> Boolean.TRUE.equals(p.getIsActive()))
            .sorted(Comparator.comparing(Product::getId))
            .toList();

        if (customers.isEmpty() || products.isEmpty()) {
            log.warn("Not enough customers/products to seed wishlists. Skipping wishlist seeding.");
            return;
        }

        List<WishlistItem> existingWishlistItems = wishlistItemRepository.findAll();
        long seededActiveWishlistItems = existingWishlistItems.stream()
            .filter(i -> Boolean.TRUE.equals(i.getIsActive()) && seededBy.equals(i.getCreatedBy()))
            .count();

        if (seededActiveWishlistItems >= targetWishlistItems) {
            log.info("Seeded wishlist items already present: {} active items. Skipping wishlist seeding.",
                seededActiveWishlistItems);
            return;
        }

        Map<Long, Wishlist> wishlistByCustomerId = wishlistRepository.findAll().stream()
            .filter(w -> w.getCustomer() != null && w.getCustomer().getId() != null)
            .collect(Collectors.toMap(w -> w.getCustomer().getId(), w -> w, (left, right) -> left, HashMap::new));

        List<Product> hottestProducts = products.subList(0, Math.min(3, products.size()));
        List<Product> mediumProducts = products.subList(Math.min(3, products.size()), Math.min(7, products.size()));
        List<Product> tailProducts = products.subList(Math.min(7, products.size()), products.size());

        List<Product> weightedProducts = new java.util.ArrayList<>();
        for (Product product : hottestProducts) {
            for (int w = 0; w < 7; w++) {
                weightedProducts.add(product);
            }
        }
        for (Product product : mediumProducts) {
            for (int w = 0; w < 3; w++) {
                weightedProducts.add(product);
            }
        }
        weightedProducts.addAll(tailProducts);
        if (weightedProducts.isEmpty()) {
            weightedProducts.addAll(products);
        }

        int createdOrReactivated = 0;
        int needToCreate = (int) (targetWishlistItems - seededActiveWishlistItems);
        int maxAttempts = customers.size() * products.size();
        LocalDateTime startAt = LocalDateTime.now().minusDays(29);

        for (int attempt = 0; attempt < maxAttempts && createdOrReactivated < needToCreate; attempt++) {
            Customer customer = customers.get(attempt % customers.size());
            Product product = weightedProducts.get((attempt * 7 + 3) % weightedProducts.size());
            LocalDateTime wishlistedAt = startAt
                .plusHours((attempt * 20L) % (30L * 24L))
                .plusMinutes((attempt * 11L) % 60L);

            Wishlist wishlist = getOrCreateWishlistForCustomer(customer, wishlistByCustomerId, seededBy);
            WishlistItem existing = wishlistItemRepository.findByWishlistIdAndProductId(wishlist.getId(), product.getId());

            if (existing == null) {
                WishlistItem item = new WishlistItem();
                item.setWishlist(wishlist);
                item.setProduct(product);
                item.setCreatedBy(seededBy);
                item = wishlistItemRepository.save(item);
                jdbcTemplate.update("UPDATE wishlist_items SET created_at = ?, updated_at = ? WHERE item_id = ?",
                    wishlistedAt, wishlistedAt, item.getId());
                createdOrReactivated++;
                continue;
            }

            if (!Boolean.TRUE.equals(existing.getIsActive())) {
                existing.setIsActive(true);
                existing.setUpdatedBy(seededBy);
                existing = wishlistItemRepository.save(existing);
                jdbcTemplate.update("UPDATE wishlist_items SET created_at = ?, updated_at = ? WHERE item_id = ?",
                    wishlistedAt, wishlistedAt, existing.getId());
                createdOrReactivated++;
            }
        }

        if (createdOrReactivated < needToCreate) {
            log.warn("Only seeded {} wishlist item(s). Needed {} but unique customer-product pairs were not enough.",
                createdOrReactivated, needToCreate);
        }

        log.info("Wishlist seeded: {} new/reactivated items, {} active items target {}",
            createdOrReactivated,
            seededActiveWishlistItems + createdOrReactivated,
            targetWishlistItems);
    }

    private Wishlist getOrCreateWishlistForCustomer(Customer customer,
                                                    Map<Long, Wishlist> wishlistByCustomerId,
                                                    String createdBy) {
        Wishlist existing = wishlistByCustomerId.get(customer.getId());
        if (existing != null) {
            return existing;
        }

        Wishlist wishlist = new Wishlist();
        wishlist.setCustomer(customer);
        wishlist.setCreatedBy(createdBy);
        wishlist = wishlistRepository.save(wishlist);
        wishlistByCustomerId.put(customer.getId(), wishlist);
        return wishlist;
    }

    private void seedProductFeedbacks() {
        log.info("Seeding product feedback...");

        final int targetSeedReviews = 17;
        final String seededBy = "REVIEW_SEEDER";

        List<ProductFeedback> existingFeedbacks = productFeedbackRepository.findAll();
        long alreadySeeded = existingFeedbacks.stream()
            .filter(f -> seededBy.equals(f.getCreatedBy()))
            .count();

        if (alreadySeeded >= targetSeedReviews) {
            log.info("Seeded reviews already present: {}. Skipping feedback seeding.", alreadySeeded);
            return;
        }

        Set<String> existingCustomerProductPairs = existingFeedbacks.stream()
            .filter(f -> f.getCustomer() != null && f.getCustomer().getId() != null
                && f.getProduct() != null && f.getProduct().getId() != null)
            .map(f -> f.getCustomer().getId() + "-" + f.getProduct().getId())
            .collect(Collectors.toSet());

        Map<String, OrderItem> purchasedPairs = new LinkedHashMap<>();
        for (OrderItem item : orderItemRepository.findAll()) {
            if (item.getOrder() == null || item.getOrder().getCustomer() == null || item.getOrder().getCustomer().getId() == null
                || item.getVariant() == null || item.getVariant().getProduct() == null || item.getVariant().getProduct().getId() == null) {
                continue;
            }

            String pairKey = item.getOrder().getCustomer().getId() + "-" + item.getVariant().getProduct().getId();
            purchasedPairs.putIfAbsent(pairKey, item);
        }

        if (purchasedPairs.isEmpty()) {
            log.warn("No purchased product pairs found. Skipping review seeding.");
            return;
        }

        List<String> sampleComments = List.of(
            "Chất vải đẹp, mặc rất thoải mái.",
            "Đường may chắc chắn, form chuẩn như mô tả.",
            "Màu sắc giống ảnh, giao hàng nhanh.",
            "Đóng gói cẩn thận, sản phẩm đáng tiền.",
            "Mặc lên tôn dáng, sẽ ủng hộ thêm.",
            "Chất liệu ổn trong tầm giá.",
            "Shop tư vấn nhiệt tình, size vừa khít.",
            "Sản phẩm tốt, không có chỉ thừa.",
            "Giặt lần đầu không bị ra màu.",
            "Rất hài lòng, đúng nhu cầu sử dụng.",
            "Kiểu dáng đẹp, mặc đi làm hay đi chơi đều hợp.",
            "Giá hợp lý, chất lượng vượt mong đợi.",
            "Shop làm ăn chán, giao hàng quá trễ và thái độ khó chịu.",
            "Chất lượng quá tệ, mặc 1 lần đã xù lông.",
            "Ảnh một kiểu, nhận hàng một nẻo, rất bực mình.",
            "Đồ như vậy mà cũng bán được à, thất vọng thật sự.",
            "May ẩu, chỉ thừa nhiều, trải nghiệm quá tệ."
        );
        List<Integer> sampleRatings = List.of(5, 5, 4, 5, 4, 4, 5, 5, 4, 5, 4, 5, 2, 1, 1, 2, 1);
        List<String> sampleStatuses = List.of(
            "APPROVED", "APPROVED", "APPROVED", "APPROVED", "APPROVED", "APPROVED",
            "APPROVED", "APPROVED", "APPROVED", "APPROVED", "APPROVED", "APPROVED",
            "APPROVED", "HIDDEN", "HIDDEN", "APPROVED", "HIDDEN"
        );

        int needToCreate = (int) (targetSeedReviews - alreadySeeded);
        int created = 0;
        int templateIndex = (int) alreadySeeded;

        for (Map.Entry<String, OrderItem> entry : purchasedPairs.entrySet()) {
            if (created >= needToCreate) {
                break;
            }

            if (existingCustomerProductPairs.contains(entry.getKey())) {
                continue;
            }

            OrderItem item = entry.getValue();
            ProductFeedback feedback = new ProductFeedback();
            feedback.setProduct(item.getVariant().getProduct());
            feedback.setCustomer(item.getOrder().getCustomer());
            feedback.setOrder(item.getOrder());
            feedback.setRating(sampleRatings.get(templateIndex % sampleRatings.size()));
            feedback.setComment(sampleComments.get(templateIndex % sampleComments.size()));
            String status = sampleStatuses.get(templateIndex % sampleStatuses.size());
            feedback.setFeedbackStatus(status);
            feedback.setHideReason("HIDDEN".equals(status) ? "Nội dung không phù hợp hiển thị công khai" : null);
            feedback.setModeratedAt(LocalDateTime.now().minusDays(templateIndex % 7));
            feedback.setModeratedBy("system");
            feedback.setCreatedBy(seededBy);

            productFeedbackRepository.save(feedback);
            existingCustomerProductPairs.add(entry.getKey());
            created++;
            templateIndex++;
        }

        if (created < needToCreate) {
            log.warn("Only seeded {} review(s). Needed {} but not enough unique purchased pairs.", created, needToCreate);
        }

        log.info("Product feedback seeded: {} new review(s), {} seeded total", created, alreadySeeded + created);
    }

    private void seedBanners() {
        log.info("Seeding banners...");

        Map<String, Banner> existingByTitle = bannerRepository.findAll().stream()
            .collect(Collectors.toMap(Banner::getTitle, b -> b, (left, right) -> left));

        upsertBanner(existingByTitle,
            "Summer Essentials 2024",
            "https://images.unsplash.com/photo-1523381210434-271e8be1f52b?w=1200",
            "/collections/summer-collection-2024-c.1",
            1,
            LocalDate.now().minusDays(30),
            LocalDate.now().plusDays(60));

        upsertBanner(existingByTitle,
            "New Arrivals: Accessories",
            "https://images.unsplash.com/photo-1523275335684-37898b6baf30?w=1200",
            "/categories/accessories",
            2,
            LocalDate.now().minusDays(10),
            LocalDate.now().plusDays(20));
        
        log.info("Banners seeded: 2 banners");
    }

    private void upsertBanner(Map<String, Banner> existingByTitle, String title, String imageUrl,
                              String linkUrl, int displayOrder, LocalDate startDate, LocalDate endDate) {
        Banner banner = existingByTitle.getOrDefault(title, Banner.builder().title(title).build());
        banner.setImageUrl(imageUrl);
        banner.setLinkUrl(linkUrl);
        banner.setDisplayOrder(displayOrder);
        banner.setStatus("ACTIVE");
        banner.setStartDate(startDate);
        banner.setEndDate(endDate);
        if (banner.getCreatedBy() == null) {
            banner.setCreatedBy("SYSTEM");
        }
        bannerRepository.save(banner);
    }

    private void seedFlashSales() {
        log.info("Seeding flash sales...");
        
        List<Product> products = productRepository.findAll();
        if (products.isEmpty()) return;

        FlashSale summerFlash = FlashSale.builder()
            .name("6.6 Summer Blast")
            .startAt(LocalDateTime.now().minusHours(2))
            .endAt(LocalDateTime.now().plusHours(22))
            .status("ONGOING")
            .createdBy("SYSTEM")
            .build();
        
        flashSaleRepository.save(summerFlash);

        // Flash sale items
        for (int i = 0; i < Math.min(3, products.size()); i++) {
            Product p = products.get(i);
            BigDecimal discountValue = new BigDecimal("20"); // 20%
            BigDecimal salePrice = p.getBasePrice().multiply(new BigDecimal("0.8"));
            
            FlashSaleItem item = FlashSaleItem.builder()
                .flashSale(summerFlash)
                .product(p)
                .discountType(DiscountType.PERCENTAGE)
                .discountValue(discountValue)
                .salePrice(salePrice)
                .createdBy("SYSTEM")
                .build();
            flashSaleItemRepository.save(item);
        }

        log.info("Flash sales seeded: 1 flash sale with {} items", Math.min(3, products.size()));
    }

    /**
     * 10. Seed RMA Requests (Yeu cau doi tra mau)
     */
    private void seedRmaRequests() {
        log.info("Seeding RMA requests...");

        cleanupDuplicateRmaRequests();

        // Lay cac don hang da giao thanh cong va loai bo trung lap theo orderId
        List<Order> deliveredOrders = orderRepository.findAll().stream()
                .filter(o -> o.getStatus() == OrderStatus.DELIVERED)
                .collect(Collectors.collectingAndThen(
                        Collectors.toMap(Order::getId, o -> o, (left, right) -> left, java.util.LinkedHashMap::new),
                        m -> List.copyOf(m.values())));

        if (deliveredOrders.isEmpty()) {
            log.warn("Khong tim thay don hang DELIVERED de tao RMA. Skipping...");
            return;
        }

        // Chi tao RMA cho cac don khac nhau, khong reuse cung mot order
        int seeded = 0;

        if (!deliveredOrders.isEmpty() && seedRmaIfMissing(deliveredOrders.get(0), RmaType.RETURN, RmaStatus.PENDING,
                "Tay ao bi bung chi, toi muon tra hang.",
                null,
                null,
                "https://images.unsplash.com/photo-1582552938357-32b906df40cb?w=400,https://images.unsplash.com/photo-1596755094514-f87e34085b2c?w=400")) {
            seeded++;
        }

        if (deliveredOrders.size() >= 2 && seedRmaIfMissing(deliveredOrders.get(1), RmaType.EXCHANGE, RmaStatus.APPROVED,
                "Size L hoi rong, shop cho toi doi sang size M nhe.",
                "Da dong y cho khach doi size. Vui long gui hang ve kho.",
                null,
                "https://images.unsplash.com/photo-1591195853828-11db59a44f6b?w=400")) {
            seeded++;
        }

        if (deliveredOrders.size() >= 3 && seedRmaIfMissing(deliveredOrders.get(2), RmaType.RETURN, RmaStatus.COMPLETED,
                "Giao sai mau san pham, toi dat den nhung lai giao trang.",
                "Da xac nhan loi. Da hoan lai 100% cho khach qua vi Momo.",
                deliveredOrders.get(2).getTotalPrice(),
                "https://images.unsplash.com/photo-1521572163474-6864f9cf17ab?w=400")) {
            seeded++;
        }

        seedMissingRmaRequests(deliveredOrders);

        if (seeded < 3) {
            log.warn("Chi co {} don DELIVERED khac nhau de seed RMA. De co 3 mau RMA, can it nhat 3 don DELIVERED distinct.", seeded);
        }

        log.info("RMA requests seeded: {} sample requests", seeded);
    }

    private void cleanupDuplicateRmaRequests() {
        List<RmaRequest> activeRmas = rmaRequestRepository.findAll();
        Map<Long, List<RmaRequest>> byOrderId = activeRmas.stream()
                .filter(r -> r.getOrder() != null && r.getOrder().getId() != null)
                .collect(Collectors.groupingBy(r -> r.getOrder().getId()));

        byOrderId.values().stream()
                .filter(list -> list.size() > 1)
                .forEach(list -> {
                    list.sort(Comparator.comparing(RmaRequest::getCreatedAt,
                            Comparator.nullsLast(Comparator.naturalOrder()))
                            .thenComparing(RmaRequest::getId, Comparator.nullsLast(Comparator.naturalOrder())));

                    List<RmaRequest> duplicates = list.subList(1, list.size());
                    rmaRequestRepository.deleteAll(duplicates);

                    log.warn("Removed {} duplicate RMA rows for orderId={}", duplicates.size(), list.get(0).getOrder().getId());
                });
    }

    private void seedMissingRmaRequests(List<Order> deliveredOrders) {
        long currentRmaCount = rmaRequestRepository.count();
        int targetRma = 10;
        if (currentRmaCount >= targetRma) {
            log.info("Sufficient RMA requests already exist. Skipping.");
            return;
        }

        int rmasToGenerate = targetRma - (int) currentRmaCount;
        log.info("Generating {} RMA requests...", rmasToGenerate);

        java.util.Random random = new java.util.Random();

        // Sort delivered orders by creation date so we can spread RMAs across the timeline
        List<Order> sortedDelivered = new java.util.ArrayList<>(deliveredOrders);
        sortedDelivered.sort(Comparator.comparing(o -> o.getCreatedAt() != null ? o.getCreatedAt() : LocalDateTime.now().minusDays(30)));

        int step = Math.max(1, sortedDelivered.size() / rmasToGenerate);

        int generated = 0;
        for (int i = 0; i < sortedDelivered.size() && generated < rmasToGenerate; i += step) {
            Order order = sortedDelivered.get(i);
            // Skip if this order already has an RMA
            boolean exists = rmaRequestRepository.findAll().stream()
                    .anyMatch(r -> r.getOrder() != null && r.getOrder().getId() != null && r.getOrder().getId().equals(order.getId()));
            if (exists) {
                continue;
            }

            RmaType[] types = RmaType.values();
            RmaType type = types[random.nextInt(types.length)];

            RmaStatus[] statuses = RmaStatus.values();
            RmaStatus status = statuses[random.nextInt(statuses.length)];

            String[] reasons = {
                "Sản phẩm bị lỗi kỹ thuật.",
                "Giao sai mặt hàng đã đặt.",
                "Kích thước không vừa.",
                "Sản phẩm không giống hình ảnh.",
                "Hư hỏng trong quá trình vận chuyển."
            };
            String reason = reasons[random.nextInt(reasons.length)];

            LocalDateTime orderDate = order.getCreatedAt() != null ? order.getCreatedAt() : LocalDateTime.now().minusDays(30);
            LocalDateTime createdAt = orderDate.plusHours(24 + random.nextInt(48)); // 1-3 days after order
            if (createdAt.isAfter(LocalDateTime.now())) {
                createdAt = LocalDateTime.now().minusHours(1);
            }

            RmaRequest.RmaRequestBuilder<?, ?> builder = RmaRequest.builder()
                .order(order)
                .customer(order.getCustomer())
                .rmaType(type)
                .status(status)
                .reason(reason)
                .evidenceImages("https://images.unsplash.com/photo-1596755094514-f87e34085b2c?w=400")
                .createdBy(order.getCustomer().getFullName());

            if (status == RmaStatus.APPROVED || status == RmaStatus.COMPLETED || status == RmaStatus.REJECTED) {
                builder.adminNote("Đã kiểm tra yêu cầu.");
            }
            if (status == RmaStatus.COMPLETED && type == RmaType.RETURN) {
                builder.refundAmount(order.getTotalPrice());
                builder.processedAt(createdAt.plusHours(12 + random.nextInt(24)));
            }

            RmaRequest request = builder.build();
            request.setCreatedAt(createdAt);

            request = rmaRequestRepository.save(request);

            // Update createdAt via JDBC to bypass JPA
            jdbcTemplate.update("UPDATE rma_requests SET created_at = ? WHERE rma_id = ?", createdAt, request.getId());

            generated++;
        }
    }

    private boolean seedRmaIfMissing(Order order, RmaType type, RmaStatus status, String reason,
                                     String adminNote, BigDecimal refundAmount, String evidenceImages) {
        boolean exists = rmaRequestRepository.findAll().stream()
                .anyMatch(r -> r.getOrder() != null && r.getOrder().getId() != null && r.getOrder().getId().equals(order.getId()));

        if (exists) {
            log.info("RMA for order {} already exists. Skipping seed.", order.getOrderInvoice());
            return false;
        }

        RmaRequest.RmaRequestBuilder<?, ?> builder = RmaRequest.builder()
                .order(order)
                .customer(order.getCustomer())
                .rmaType(type)
                .status(status)
                .reason(reason)
                .evidenceImages(evidenceImages)
                .createdBy(order.getCustomer().getFullName());

        if (adminNote != null) {
            builder.adminNote(adminNote);
        }
        if (refundAmount != null) {
            builder.refundAmount(refundAmount);
        }
        if (status == RmaStatus.COMPLETED) {
            builder.processedAt(LocalDateTime.now());
        }

        rmaRequestRepository.save(builder.build());
        return true;
    }

    private record ItemSpec(ProductVariant variant, int quantity) { }

    private List<CustomerSeed> buildAdditionalCustomerSeeds() {
        return List.of(
            new CustomerSeed("customer4", "customer4@email.com", "Nguyen Thi E", "0910000004", "12 Le Loi, District 1, Ho Chi Minh City"),
            new CustomerSeed("customer5", "customer5@email.com", "Pham Van O", "0910000005", "88 Nguyen Trai, District 5, Ho Chi Minh City"),
            new CustomerSeed("customer6", "customer6@email.com", "Bui Thi T", "0910000006", "21 Cach Mang Thang 8, District 3, Ho Chi Minh City"),
            new CustomerSeed("customer7", "customer7@email.com", "Do Van H", "0910000007", "305 Vo Van Tan, District 3, Ho Chi Minh City"),
            new CustomerSeed("customer8", "customer8@email.com", "Le Thi M", "0910000008", "79 Dien Bien Phu, Binh Thanh, Ho Chi Minh City"),
            new CustomerSeed("customer9", "customer9@email.com", "Tran Van K", "0910000009", "145 Hai Ba Trung, District 1, Ho Chi Minh City"),
            new CustomerSeed("customer10", "customer10@email.com", "Hoang Thi N", "0910000010", "66 Pham Viet Chanh, Binh Thanh, Ho Chi Minh City"),
            new CustomerSeed("customer11", "customer11@email.com", "Vo Van D", "0910000011", "9 Ly Tu Trong, District 1, Ho Chi Minh City"),
            new CustomerSeed("customer12", "customer12@email.com", "Dang Thi P", "0910000012", "41 Tran Quang Khai, District 1, Ho Chi Minh City"),
            new CustomerSeed("customer13", "customer13@email.com", "Ngo Van Q", "0910000013", "120 Hoang Sa, Phu Nhuan, Ho Chi Minh City")
        );
    }

    private record CustomerSeed(String username, String email, String fullName,
                                String phoneNumber, String address) { }
}
