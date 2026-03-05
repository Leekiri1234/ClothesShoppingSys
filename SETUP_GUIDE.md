# 🚀 Hướng Dẫn Setup Nhanh

## Bước 1: Clone Project
```bash
git clone https://github.com/your-username/ClothesShoppingSys.git
cd ClothesShoppingSys/com.clothshop
```

## Bước 2: Tạo Database
```sql
CREATE DATABASE clothshop_db 
CHARACTER SET utf8mb4 
COLLATE utf8mb4_unicode_ci;
```

## Bước 3: Cấu Hình application.yaml

### Admin Module
```bash
cd shop-api-admin/src/main/resources
cp application.yaml.example application.yaml
```

Mở `application.yaml` và sửa:
```yaml
spring:
  datasource:
    username: root              # ← Username MySQL của bạn
    password: your_password     # ← Password MySQL của bạn
```

### Client Module (tương tự)
```bash
cd ../../shop-api-client/src/main/resources
cp application.yaml.example application.yaml
```

Sửa username và password trong file vừa tạo.

## Bước 4: Build Project
```bash
cd /path/to/ClothesShoppingSys/com.clothshop
mvn clean install -DskipTests
```

## Bước 5: Chạy Application

### Admin Portal
```bash
cd shop-api-admin
mvn spring-boot:run
```
→ Truy cập: http://localhost:8081/admin/login  
→ Login: `admin` / `admin@123`

### Client Portal
```bash
cd shop-api-client
mvn spring-boot:run
```
→ Truy cập: http://localhost:8080/login  
→ Login: `customer` / `customer@123`

## ⚠️ Lưu Ý Quan Trọng

✅ **Đã làm:**
- File `application.yaml.example` đã được commit lên Git
- File `.gitignore` đã được cấu hình ignore `application.yaml`

❌ **KHÔNG được làm:**
- KHÔNG commit file `application.yaml` (chứa password) lên Git
- KHÔNG hardcode password thật trong code

✅ **Nên làm:**
- Mỗi developer tự tạo file `application.yaml` từ `.example`
- Cập nhật password local riêng
- Chỉ commit file `.example` khi thay đổi cấu trúc config

## 🔍 Troubleshooting

### Lỗi: Cannot find application.yaml
**Nguyên nhân:** Chưa copy từ file .example  
**Giải pháp:** Chạy lệnh `cp application.yaml.example application.yaml`

### Lỗi: Access denied for user
**Nguyên nhân:** Username/password MySQL sai  
**Giải pháp:** Kiểm tra lại thông tin trong `application.yaml`

### Lỗi: Database không tồn tại
**Nguyên nhân:** Chưa tạo database  
**Giải pháp:** 
- Tạo database bằng SQL ở Bước 2
- Hoặc để Spring Boot tự tạo (đã có `createDatabaseIfNotExist=true`)

---

**Thời gian setup:** ~5 phút  
**Lần đầu chạy:** Database sẽ tự động seed dữ liệu mẫu

