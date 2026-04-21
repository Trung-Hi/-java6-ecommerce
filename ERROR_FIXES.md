# 🔧 FIX CÁC LỖI FULLSTACK PROJECT

## Tổng Quan 4 Lỗi

| # | Lỗi | Nguyên Nhân | Fix |
|---|-----|-------------|-----|
| 1 | `deleteModalOpen` not defined | Biến cũ chưa xóa hết trong template | Kiểm tra lại template |
| 2 | `ECONNREFUSED` Proxy Error | Backend không chạy hoặc sai port | Kiểm tra Spring Boot |
| 3 | HTTP 500 Internal Error | Lỗi SQL/Backend | Xem IntelliJ console |
| 4 | Images 404 | Thiếu thư mục images | Tạo public/images |

---

## 1️⃣ FIX: Property "deleteModalOpen" was accessed during render

### ✅ Tình Trạng
Đã xóa `deleteModalOpen` và `productToDelete` khi chuyển sang SweetAlert2.

### 🔍 Kiểm Tra
Chạy lệnh này để tìm còn sót không:

```powershell
cd "C:\Users\GIGABYTE\Downloads\FinalAsignment_Java6-main\frontend"
Select-String -Path "src\views\admin\AdminAccountView.vue" -Pattern "deleteModalOpen|productToDelete"
```

### 🛠️ Fix Nếu Còn Sót

**Cách 1: Xóa cache và restart**
```powershell
# Xóa cache Vite
rm -rf node_modules/.vite
npm run dev
```

**Cách 2: Hard refresh trình duyệt**
- Chrome: `Ctrl + Shift + R`
- Hoặc mở DevTools → Network tab → Check "Disable cache"

**Cách 3: Kiểm tra file thực sự**
Mở `@AdminAccountView.vue` tìm (Ctrl+F):
- `deleteModalOpen`
- `productToDelete`
- Nếu còn → Xóa đi

---

## 2️⃣ FIX: ECONNREFUSED - Vite Proxy Error

### 🔍 Nguyên Nhân
Frontend (port 5173) không kết nối được Backend (port 8080)

### ✅ Checklist Kiểm Tra

#### Bước 1: Kiểm tra Spring Boot đã chạy chưa
```powershell
# Mở terminal mới, kiểm tra port 8080
netstat -ano | findstr :8080
```
Nếu **không có kết quả** → Spring Boot chưa chạy

#### Bước 2: Kiểm tra port trong IntelliJ
Mở `application.properties`:
```properties
# File: src/main/resources/application.properties
server.port=8080
```

#### Bước 3: Khởi động Spring Boot
Trong IntelliJ IDEA:
1. Mở file `AsmJava6Application.java`
2. Click nút ▶️ (Run)
3. Đợi thấy log: `Started AsmJava6Application in X.XXX seconds`

#### Bước 4: Test API
Mở trình duyệt truy cập:
```
http://localhost:8080/api/admin/accounts
```
Nếu **hiện JSON** → Backend OK

### 🛠️ Fix vite.config.js

```javascript
// vite.config.js
export default defineConfig({
    server: {
        proxy: {
            "/api": {
                target: "http://localhost:8080",  // Đảm bảo đúng port
                changeOrigin: true,
                secure: false  // Thêm nếu dùng self-signed SSL
            },
            "/images": {
                target: "http://localhost:8080",
                changeOrigin: true
            }
        }
    }
});
```

### 🔧 Nếu Backend chạy port khác

Ví dụ Spring Boot chạy port **8081**:

```javascript
// vite.config.js
"/api": {
    target: "http://localhost:8081",  // Đổi port
    changeOrigin: true
}
```

Hoặc set biến môi trường:
```powershell
$env:VITE_BACKEND_URL="http://localhost:8081"
npm run dev
```

---

## 3️⃣ FIX: HTTP 500 Internal Server Error

### 🔍 Xem Log Trong IntelliJ

#### Bước 1: Mở Console
Trong IntelliJ IDEA:
1. Nhìn xuống cửa sổ dưới cùng
2. Tab **"Run"** hoặc **"Console"**
3. Tìm dòng màu đỏ (ERROR)

#### Bước 2: Tìm Stacktrace
Ví dụ lỗi thường gặp:
```
org.hibernate.exception.SQLGrammarException: could not extract ResultSet
Caused by: com.microsoft.sqlserver.jdbc.SQLServerException: 
Invalid object name 'accounts'.
```

### 🔥 Common Spring Boot 500 Errors

| Lỗi | Nguyên Nhân | Fix |
|-----|-------------|-----|
| `SQLGrammarException` | Thiếu bảng SQL | Chạy script tạo bảng |
| `DataAccessException` | Sai username/password SQL | Kiểm tra `application.properties` |
| `LazyInitializationException` | Lỗi fetch lazy | Thêm `@Transactional` |
| `NullPointerException` | Object null | Kiểm tra `Optional.isPresent()` |
| `ClassNotFoundException` | Thiếu driver SQL Server | Thêm dependency mssql-jdbc |

### 🛠️ application.properties Template

```properties
# Database SQL Server
spring.datasource.url=jdbc:sqlserver://localhost:1433;databaseName=ASM_Java6;encrypt=true;trustServerCertificate=true
spring.datasource.username=sa
spring.datasource.password=YourPassword
spring.datasource.driver-class-name=com.microsoft.sqlserver.jdbc.SQLServerDriver

# JPA/Hibernate
spring.jpa.hibernate.ddl-auto=update
spring.jpa.show-sql=true
spring.jpa.properties.hibernate.dialect=org.hibernate.dialect.SQLServerDialect

# Server
server.port=8080
```

### 📝 Kiểm Tra SQL Server

```sql
-- Kiểm tra bảng accounts có tồn tại không
SELECT * FROM INFORMATION_SCHEMA.TABLES 
WHERE TABLE_NAME = 'accounts';

-- Kiểm tra dữ liệu
SELECT COUNT(*) FROM accounts;
```

---

## 4️⃣ FIX: Images 404 Not Found

### 🔍 Nguyên Nhân
Thiếu folder `images` trong `public/`

### 🛠️ Fix - 2 Cách

#### Cách 1: Tạo thư mục public/images (Khuyến nghị)
```
frontend/
├── public/
│   └── images/
│       ├── logo.png          ← Default avatar
│       ├── avatar-default.jpg
│       └── user-photos/      ← Thư mục chứa avatar users
│           ├── p178.jpg
│           └── ...
```

**Code đã đúng** trong `AdminAccountView.vue`:
```javascript
const resolveAvatar = (photo) => {
    const raw = String(photo || "").trim();
    if (!raw) return "/images/logo.png";        // ← Public folder
    if (/^https?:\/\//i.test(raw)) return raw;  // ← External URL
    if (raw.startsWith("/")) return raw;        // ← Already absolute
    return `/images/${raw}`;                    // ← Public folder
};
```

#### Cách 2: Proxy sang Backend (Đã có)
Trong `vite.config.js` đã có:
```javascript
"/images": {
    target: "http://localhost:8080",  // ← Backend phục vụ images
    changeOrigin: true
}
```
**→ Backend cần phục vụ static files từ `/images`**

### 📁 Tạo Folder Script

```powershell
# Tạo thư mục images
New-Item -ItemType Directory -Force -Path "C:\Users\GIGABYTE\Downloads\FinalAsignment_Java6-main\frontend\public\images"

# Tạo file logo.png tạm (hoặc copy file thật)
# Copy file ảnh vào đây
```

### ✅ Kiểm Tra
Sau khi tạo folder, truy cập:
```
http://localhost:5173/images/logo.png
```
Nếu **hiện ảnh** → OK

---

## 🚀 Quick Fix Commands

```powershell
# 1. Vào thư mục frontend
cd "C:\Users\GIGABYTE\Downloads\FinalAsignment_Java6-main\frontend"

# 2. Xóa cache
rm -rf node_modules/.vite

# 3. Tạo folder images
New-Item -ItemType Directory -Force -Path "public\images"

# 4. Copy logo (nếu có)
# Copy-Item "path\to\logo.png" "public\images\"

# 5. Chạy dev server
npm run dev
```

---

## 📋 Spring Boot Backend Setup

### File: `application.properties`
```properties
server.port=8080

# Database
spring.datasource.url=jdbc:sqlserver://localhost:1433;databaseName=ASM_Java6;encrypt=true;trustServerCertificate=true
spring.datasource.username=sa
spring.datasource.password=your_password
spring.datasource.driver-class-name=com.microsoft.sqlserver.jdbc.SQLServerDriver

# JPA
spring.jpa.hibernate.ddl-auto=update
spring.jpa.show-sql=true
```

### Check SQL Server Driver Dependency
File `pom.xml`:
```xml
<dependency>
    <groupId>com.microsoft.sqlserver</groupId>
    <artifactId>mssql-jdbc</artifactId>
    <scope>runtime</scope>
</dependency>
```

---

## ✨ Tóm Tắt Fix Nhanh

| Lỗi | Hành Động |
|-----|-----------|
| deleteModalOpen warning | Restart dev server, clear cache |
| ECONNREFUSED | Chạy Spring Boot, check port 8080 |
| HTTP 500 | Xem IntelliJ console, fix SQL connection |
| Images 404 | Tạo `public/images/` folder |

---

## 🆘 Nếu Vẫn Lỗi

1. **Frontend console lỗi**: Mở F12 → Console → Copy lỗi gửi tôi
2. **Backend lỗi**: Mở IntelliJ → Run tab → Copy stacktrace gửi tôi
3. **Network lỗi**: F12 → Network → Xem request nào đỏ (failed)
