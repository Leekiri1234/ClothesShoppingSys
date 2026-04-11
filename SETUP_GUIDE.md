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

Sửa `application.yaml` (username và password MySQL). Lưu ý **OpenAI API Key** đã được cấu hình lấy từ biến môi trường.

## Bước 4: Setup Biến Môi Trường (Cho tính năng Try-On)

Tính năng Virtual Try-On tải cần khóa OpenAI API. Xuất giá trị này trực tiếp trên terminal của bạn:
```bash
export OPENAI_API_KEY="sk-your-openai-api-key-here"
```
*(Lưu ý: Bạn phải chạy lệnh `export` này trên cùng một terminal trước khi khởi động ứng dụng Client. Có thể thêm dòng này vào `~/.zshrc` hoặc `~/.bashrc` để không phải gõ lại mỗi lần).*

## Bước 5: Build Project
```bash
cd /path/to/ClothesShoppingSys/com.clothshop
mvn clean install -DskipTests
```

## Bước 6: Chạy Application

### Admin Portal
```bash
cd shop-api-admin
mvn spring-boot:run
```
→ Truy cập: http://localhost:8081/admin/login  
→ Login: `admin` / `admin@123`

### Client Portal
Xin nhớ mở terminal mới, chạy lại lệnh `export OPENAI_API_KEY=...` nếu bạn chưa thiết lập toàn cục, sau đó:
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

