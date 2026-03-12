package com.clothshop.domain.config;

import com.clothshop.domain.entities.auth.*;
import com.clothshop.domain.entities.marketing.*;
import com.clothshop.domain.entities.product.*;
import com.clothshop.domain.enums.*;
import com.clothshop.domain.repositories.auth.*;
import com.clothshop.domain.repositories.marketing.*;
import com.clothshop.domain.repositories.product.*;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.List;

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
    private final CollectionRepository collectionRepository;
    private final CollectionItemRepository collectionItemRepository;
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
        seedCollections();

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

        log.info("Accounts seeded: 4 staff (all roles), 3 customers");
    }

    /**
     * 3. Seed Categories
     */
    private void seedCategories() {
        log.info("Seeding categories...");

        // === LEVEL 0: ROOT CATEGORIES (parent_id = NULL) ===
        Category fashion = createCategory("Fashion", "fashion", "ACTIVE", null);
        categoryRepository.save(fashion);

        // === LEVEL 1: SUB-CATEGORIES (Children of Fashion) ===
        Category menFashion = createCategory("Men Fashion", "men-fashion", "ACTIVE", fashion);
        Category womenFashion = createCategory("Women Fashion", "women-fashion", "ACTIVE", fashion);
        categoryRepository.save(menFashion);
        categoryRepository.save(womenFashion);

        // === LEVEL 2: DETAILED CATEGORIES (Grandchildren of Fashion) ===
        Category menShirts = createCategory("Men Shirts", "men-shirts", "ACTIVE", menFashion);
        Category menJeans = createCategory("Men Jeans", "men-jeans", "ACTIVE", menFashion);
        Category womenDresses = createCategory("Women Dresses", "women-dresses", "ACTIVE", womenFashion);
        Category womenSkirts = createCategory("Women Skirts", "women-skirts", "ACTIVE", womenFashion);

        categoryRepository.save(menShirts);
        categoryRepository.save(menJeans);
        categoryRepository.save(womenDresses);
        categoryRepository.save(womenSkirts);

        // === OTHER ROOT CATEGORIES ===
        Category accessories = createCategory("Accessories", "accessories", "ACTIVE", null);
        Category shoes = createCategory("Shoes", "shoes", "ACTIVE", null);
        Category bags = createCategory("Bags", "bags", "ACTIVE", null);

        categoryRepository.save(accessories);
        categoryRepository.save(shoes);
        categoryRepository.save(bags);

        log.info("Categories seeded: 4 root + 2 level-1 + 4 level-2 = 10 categories");
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

        // Get all products for assignment
        List<Product> allProducts = productRepository.findAll();

        if (allProducts.size() < 10) {
            log.warn("Not enough products to create collections. Skipping collection seeding.");
            return;
        }

        // Collection 1: Summer Collection
        Collection summerCollection = new Collection();
        summerCollection.setName("Summer Collection 2024");
        summerCollection.setSlug("summer-collection-2024"); // Will be updated with ID after save
        summerCollection.setDescription("Fresh and vibrant styles for the summer season");
        summerCollection.setCreatedBy("admin");
        summerCollection = collectionRepository.save(summerCollection);

        // Update slug with Shopee-style ID suffix
        summerCollection.setSlug("summer-collection-2024-c." + summerCollection.getId());
        summerCollection = collectionRepository.save(summerCollection);

        // Add 5 products to Summer Collection
        // Classic White T-Shirt, Floral Summer Dress, Cotton Polo Shirt, Linen Shorts, Graphic Print T-Shirt
        addProductToCollection(summerCollection, allProducts.get(0), 1); // Classic White T-Shirt
        addProductToCollection(summerCollection, allProducts.get(2), 2); // Floral Summer Dress
        addProductToCollection(summerCollection, allProducts.get(4), 3); // Cotton Polo Shirt
        addProductToCollection(summerCollection, allProducts.get(11), 4); // Linen Shorts
        addProductToCollection(summerCollection, allProducts.get(8), 5); // Graphic Print T-Shirt

        log.info("Created Summer Collection with 5 products");

        // Collection 2: Winter Essentials
        Collection winterCollection = new Collection();
        winterCollection.setName("Winter Essentials 2024");
        winterCollection.setSlug("winter-essentials-2024"); // Will be updated with ID after save
        winterCollection.setDescription("Stay warm and stylish with our winter collection");
        winterCollection.setCreatedBy("admin");
        winterCollection = collectionRepository.save(winterCollection);

        // Update slug with Shopee-style ID suffix
        winterCollection.setSlug("winter-essentials-2024-c." + winterCollection.getId());
        winterCollection = collectionRepository.save(winterCollection);

        // Add 5 products to Winter Collection
        // Black Leather Jacket, Hooded Sweatshirt, Knit Cardigan, Casual Blazer, Slim Fit Denim Jeans
        addProductToCollection(winterCollection, allProducts.get(3), 1); // Black Leather Jacket
        addProductToCollection(winterCollection, allProducts.get(10), 2); // Hooded Sweatshirt
        addProductToCollection(winterCollection, allProducts.get(7), 3); // Knit Cardigan
        addProductToCollection(winterCollection, allProducts.get(11), 4); // Casual Blazer
        addProductToCollection(winterCollection, allProducts.get(1), 5); // Slim Fit Denim Jeans

        log.info("Created Winter Collection with 5 products");

        log.info("Collections seeded: 2 collections with 5 products each");
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

    private Category createCategory(String name, String slug, String status, Category parent) {
        Category category = new Category();
        category.setCategoryName(name);
        category.setCategorySlug(slug);
        category.setCatStatus(status);
        category.setParent(parent);
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

