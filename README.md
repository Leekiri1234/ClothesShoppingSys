# 🛍️ ClothShop - Hệ Thống Quản Lý Bán Hàng Thời Trang

## 📖 Giới Thiệu

ClothShop là một hệ thống quản lý bán hàng thời trang được xây dựng theo kiến trúc **Modular Monolith** với **Traditional MVC (Server-Side Rendering)** sử dụng Spring Boot và Thymeleaf.

Dự án được phát triển với mục đích học tập, tuân thủ các best practices về kiến trúc phần mềm, clean code và security.

## 🏗️ Kiến Trúc Dự Án

Dự án được chia thành **4 modules độc lập**:

```
com.clothshop/
├── shop-common/          # Infrastructure logic (Utils, Exceptions, Constants)
├── shop-domain/          # Entities, Repositories, Business Enums
├── shop-api-admin/       # Admin Portal (Quản lý sản phẩm, đơn hàng, nhân viên)
└── shop-api-client/      # Customer Portal (Mua sắm, giỏ hàng, thanh toán)
```

### Module Breakdown

| Module | Mô Tả | Port |
|--------|-------|------|
| **shop-common** | Chứa logic hạ tầng dùng chung: Utils, Global Exception Handler, Constants | N/A |
| **shop-domain** | "Xương sống" của hệ thống: JPA Entities, Spring Data Repositories, Business Enums | N/A |
| **shop-api-admin** | Portal quản trị: Controllers, Services, DTOs, Mappers cho Admin | 8081 |
| **shop-api-client** | Portal khách hàng: Controllers, Services, DTOs, Mappers cho Customer | 8080 |

## 🚀 Công Nghệ Sử Dụng

### Backend
- **Framework:** Spring Boot 3.2.2
- **Java Version:** 17
- **Build Tool:** Maven
- **Database:** MySQL 8
- **ORM:** Hibernate/JPA
- **Security:** Spring Security (Session-based, NO JWT)
- **Template Engine:** Thymeleaf + Layout Dialect
- **Mapping:** MapStruct 1.5.5
- **Utilities:** Lombok, Apache Commons Lang3

### Frontend
- **HTML5:** Semantic markup
- **CSS:** Bootstrap 5.3
- **JavaScript:** Vanilla JS (minimal)
- **Icons:** Font Awesome 6.4

## 📦 Cài Đặt & Chạy Dự Án

### Yêu Cầu Hệ Thống
- **JDK 17** trở lên
- **Maven 3.8+**
- **MySQL 8.0+**
- **IDE:** IntelliJ IDEA / Eclipse / VS Code

### Bước 1: Clone Repository
```bash
git clone https://github.com/your-username/ClothesShoppingSys.git
cd ClothesShoppingSys/com.clothshop
```

### Bước 2: Cấu Hình Database

#### 2.1. Tạo Database MySQL
Tạo database MySQL (hoặc để Spring Boot tự tạo):
```sql
CREATE DATABASE clothshop_db 
CHARACTER SET utf8mb4 
COLLATE utf8mb4_unicode_ci;
```

**Lưu ý:** Database sẽ tự động được tạo nếu bạn giữ nguyên config `createDatabaseIfNotExist=true` trong `application.yaml`.

#### 2.2. Cấu Hình Connection String

Dự án cung cấp file **template cấu hình mẫu** để bảo mật thông tin database:

**Bước 1:** Copy file `.example` thành `application.yaml`

```bash
# Với Admin module
cp shop-api-admin/src/main/resources/application.yaml.example \
   shop-api-admin/src/main/resources/application.yaml

# Với Client module  
cp shop-api-client/src/main/resources/application.yaml.example \
   shop-api-client/src/main/resources/application.yaml
```

**Bước 2:** Cập nhật thông tin database trong `application.yaml`

Mở file `application.yaml` vừa tạo và thay thế:
- `DB_USERNAME` → Username MySQL của bạn (mặc định: `root`)
- `DB_PASSWORD` → Password MySQL của bạn

```yaml
spring:
  datasource:
    url: jdbc:mysql://localhost:3306/clothshop_db?createDatabaseIfNotExist=true&useSSL=false&serverTimezone=Asia/Ho_Chi_Minh&allowPublicKeyRetrieval=true
    username: root        # ← Thay đổi nếu khác
    password: your_password_here  # ← Thay mật khẩu MySQL của bạn
```

**Lưu ý quan trọng:**
- ⚠️ File `application.yaml` đã được thêm vào `.gitignore` để **không commit thông tin nhạy cảm** lên Git
- ✅ Chỉ commit file `application.yaml.example` (template không chứa password thật)
- 🔒 Mỗi developer cần tự tạo file `application.yaml` riêng trên máy local

### Bước 3: Build & Run

#### Option 1: Chạy Admin Portal
```bash
cd com.clothshop/shop-api-admin
mvn spring-boot:run
```
- **URL:** http://localhost:8081/admin/login
- **Username:** `admin`
- **Password:** `admin@123`

#### Option 2: Chạy Client Portal
```bash
cd com.clothshop/shop-api-client
mvn spring-boot:run
```
- **URL:** http://localhost:8080/login
- **Username:** `customer`
- **Password:** `customer@123`

#### Option 3: Chạy từ IDE
1. Import project vào IDE (IntelliJ IDEA khuyến nghị)
2. Run `AdminApplication.java` hoặc `ClientApplication.java`
3. Truy cập URL tương ứng

### Bước 4: Database Seeding (Tự Động)

Khi chạy lần đầu, hệ thống sẽ **tự động seed dữ liệu mẫu** thông qua `DatabaseSeeder.java`:

- ✅ 4 Staff Roles (SUPER_ADMIN, MARKETING_STAFF, SALE_PRODUCT_STAFF, CUSTOMER_SERVICE)
- ✅ 1 Admin account (`admin/admin@123`)
- ✅ 1 Customer account (`customer/customer@123`)
- ✅ 5 Categories mẫu
- ✅ 3 Products mẫu với variants

**Log:** Kiểm tra console để xác nhận:
```
INFO: Starting database seeding...
INFO: Database seeding completed successfully!
```

## 🔐 Authentication & Authorization

### Admin Module (Port 8081)
- **Login URL:** `/admin/login`
- **Access Control:**
  - `SUPER_ADMIN`: Toàn quyền quản lý hệ thống
  - `MARKETING_STAFF`: Quản lý voucher, banner, collections
  - `SALE_PRODUCT_STAFF`: Quản lý sản phẩm, kho hàng, đơn hàng
  - `CUSTOMER_SERVICE`: Xác nhận thanh toán, xử lý RMA

### Client Module (Port 8080)
- **Login URL:** `/login`
- **Register URL:** `/register`
- **Access Control:**
  - Public: Trang chủ, danh sách sản phẩm, tìm kiếm
  - Customer Only: Profile, giỏ hàng, checkout, đơn hàng

### Security Features
- ✅ Session-based Authentication (NO JWT)
- ✅ CSRF Protection (Mandatory)
- ✅ Session Fixation Protection
- ✅ Password Encryption (BCrypt)
- ✅ Remember-Me (7 days)
- ✅ Soft Delete (is_active field)

## 📂 Cấu Trúc Thư Mục

```
com.clothshop/
├── pom.xml                    # Parent POM
├── shop-common/
│   └── src/main/java/com/clothshop/common/
│       ├── constants/         # System constants
│       ├── exceptions/        # Business exceptions
│       └── utils/             # Utility classes
│
├── shop-domain/
│   └── src/main/java/com/clothshop/domain/
│       ├── config/
│       │   ├── DatabaseSeeder.java      # Auto seed data
│       │   └── DomainConfig.java        # PasswordEncoder bean
│       ├── entities/          # JPA Entities
│       │   ├── auth/          # Account, Customer, Staff, Role
│       │   ├── product/       # Product, ProductVariant, Category
│       │   ├── order/         # Order, OrderItem, Payment
│       │   ├── customer/      # Cart, Wishlist
│       │   ├── marketing/     # Voucher, Collection, Banner
│       │   └── cms/           # Notification, Banner
│       ├── repositories/      # Spring Data JPA Repositories
│       └── enums/             # Business Enums
│
├── shop-api-admin/
│   └── src/main/
│       ├── java/com/clothshop/admin/
│       │   ├── config/        # SecurityConfig
│       │   ├── controllers/   # Admin Controllers
│       │   ├── services/      # Admin Business Logic
│       │   ├── dtos/          # Admin DTOs
│       │   └── mappers/       # MapStruct Mappers
│       └── resources/
│           ├── application.yaml
│           ├── static/        # CSS, JS, Images
│           └── templates/     # Thymeleaf Templates
│               └── admin/
│
└── shop-api-client/
    └── src/main/
        ├── java/com/clothshop/client/
        │   ├── config/        # SecurityConfig
        │   ├── controllers/   # Client Controllers
        │   ├── services/      # Client Business Logic
        │   ├── dtos/          # Client DTOs
        │   └── mappers/       # MapStruct Mappers
        └── resources/
            ├── application.yaml
            ├── static/        # CSS, JS, Images
            └── templates/     # Thymeleaf Templates
                └── client/
```

## 🎯 Tính Năng Chính

### Admin Portal
- [ ] Quản lý sản phẩm (CRUD)
- [ ] Quản lý variants (màu sắc, kích thước, giá, kho)
- [ ] Quản lý danh mục
- [ ] Quản lý đơn hàng
- [ ] Quản lý khách hàng
- [ ] Quản lý voucher & flash sale
- [ ] Quản lý collections
- [ ] Báo cáo doanh thu
- [ ] Quản lý nhân viên (SUPER_ADMIN only)

### Customer Portal
- [ ] Xem danh sách sản phẩm
- [ ] Chi tiết sản phẩm với variants
- [ ] Tìm kiếm & lọc sản phẩm
- [ ] Giỏ hàng (lưu vào DB)
- [ ] Đặt hàng & thanh toán
- [ ] Quản lý đơn hàng cá nhân
- [ ] Wishlist
- [ ] Profile & lịch sử mua hàng
- [ ] Đánh giá sản phẩm

## 📝 Data Flow (Bắt Buộc)

```
Controller → DTO → Service → Repository (Entity) → Mapping (Entity to DTO) → Controller → View
```

**Nguyên tắc:**
1. Controller nhận DTO từ request
2. Service xử lý business logic
3. Repository làm việc với Entity
4. **MapStruct** convert Entity ↔ DTO
5. **KHÔNG BAO GIỜ** expose Entity ra View

## 🔐 Git Workflow & Security

### Quản Lý File Cấu Hình

Dự án sử dụng pattern `.example` để bảo mật thông tin nhạy cảm:

#### ✅ Được Commit (Public)
```
✓ application.yaml.example    # Template configuration
✓ .gitignore                   # Ignore rules
✓ README.md                    # Documentation
```

#### ❌ Không Commit (Private)
```
✗ application.yaml             # Chứa password thật
✗ application.properties       # Chứa sensitive data
```

### Quy Trình Làm Việc

#### 1. Clone Project Lần Đầu
```bash
git clone <repository-url>
cd ClothesShoppingSys/com.clothshop

# Tạo file config từ template
cp shop-api-admin/src/main/resources/application.yaml.example \
   shop-api-admin/src/main/resources/application.yaml

cp shop-api-client/src/main/resources/application.yaml.example \
   shop-api-client/src/main/resources/application.yaml

# Cập nhật username/password MySQL trong 2 file vừa tạo
```

#### 2. Thay Đổi Cấu Hình Cấu Trúc
Nếu bạn cần thay đổi cấu trúc config (thêm property mới, đổi port, etc.):

```bash
# 1. Sửa file .example (KHÔNG sửa file application.yaml)
vim shop-api-admin/src/main/resources/application.yaml.example

# 2. Commit file .example
git add shop-api-admin/src/main/resources/application.yaml.example
git commit -m "chore: add new database pool configuration"

# 3. Copy lại từ .example sang application.yaml (local)
cp application.yaml.example application.yaml

# 4. Cập nhật lại password trong application.yaml
```

#### 3. Pull Code Mới
```bash
git pull origin main

# Nếu có conflict ở file application.yaml → Bỏ qua (file này đã bị ignore)
# Chỉ cần merge file .example, sau đó copy lại:
cp application.yaml.example application.yaml
# Nhớ điền lại password
```

### Checklist Trước Khi Commit

- [ ] File `application.yaml` KHÔNG xuất hiện trong `git status`
- [ ] Chỉ commit file `application.yaml.example` nếu có thay đổi cấu trúc
- [ ] File `.example` KHÔNG chứa password thật (chỉ có `DB_USERNAME`, `DB_PASSWORD`)
- [ ] Đã test code trên local trước khi push

## 🔧 Troubleshooting

### Lỗi: "Cannot find application.yaml"
**Nguyên nhân:** Chưa tạo file config từ template  
**Giải pháp:** 
```bash
cp shop-api-admin/src/main/resources/application.yaml.example \
   shop-api-admin/src/main/resources/application.yaml
```

### Lỗi: "Access denied for user 'root'@'localhost'"
**Giải pháp:** Kiểm tra username/password MySQL trong `application.yaml`

### Lỗi: "Table 'clothshop_db.accounts' doesn't exist"
**Giải pháp:** 
- Đảm bảo `ddl-auto: update` trong `application.yaml`
- Kiểm tra Entity có `@Entity` annotation
- Restart application

### Lỗi: Login không redirect đến dashboard
**Giải pháp:**
- Kiểm tra `CustomUserDetailsService` đã được inject vào `SecurityConfig` chưa
- Verify username/password đúng với data trong DB
- Check console log để xem authentication error

### Lỗi: "Could not autowire. No beans of 'PasswordEncoder' type found"
**Giải pháp:**
- PasswordEncoder bean được define trong `shop-domain/config/DomainConfig.java`
- Đảm bảo dependency `spring-security-crypto` đã được add vào `shop-domain/pom.xml`

## 🤝 Đóng Góp

Dự án này được phát triển cho mục đích học tập. Mọi đóng góp, góp ý đều được hoan nghênh!

### Quy Trình Đóng Góp
1. Fork repository
2. Tạo branch mới (`git checkout -b feature/AmazingFeature`)
3. Commit changes (`git commit -m 'Add some AmazingFeature'`)
4. Push to branch (`git push origin feature/AmazingFeature`)
5. Mở Pull Request

## 📜 License

Dự án này được phát triển cho mục đích học tập và nghiên cứu.

## 👨‍💻 Tác Giả

**Họ và Tên:** [Tên của bạn]  
**Trường:** [Tên trường]  
**Lớp:** [Tên lớp]  
**Email:** [Email của bạn]

## 🙏 Lời Cảm Ơn

- Spring Boot Documentation
- Thymeleaf Documentation
- MapStruct Team
- Stack Overflow Community
- Giảng viên hướng dẫn

---

**Lưu ý:** Đây là dự án học tập, chưa được tối ưu cho production. Không sử dụng cho mục đích thương mại.

