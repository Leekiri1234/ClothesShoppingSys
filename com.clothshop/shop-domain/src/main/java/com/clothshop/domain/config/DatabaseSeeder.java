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

