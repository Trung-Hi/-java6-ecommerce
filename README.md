# 🛒 Hệ thống E-commerce Trực tuyến

Hệ thống bán hàng trực tuyến hoàn chỉnh với các tính năng: quản lý sản phẩm, giỏ hàng, thanh toán (PayOS), quản lý đơn hàng, chat hỗ trợ, và báo cáo doanh thu.

## 📋 Mô tả dự án

Dự án là hệ thống thương mại điện tử được xây dựng với kiến trúc hiện đại, tách biệt frontend và backend, tích hợp các dịch vụ thanh toán điện tử và giao tiếp real-time.

## 🚀 Công nghệ sử dụng

### Backend
- **Java 17** - Ngôn ngữ lập trình
- **Spring Boot 4.0.1** - Framework chính
- **Spring Security** - Xác thực và phân quyền
- **Spring Data JPA** - ORM và truy cập database
- **JWT** - Stateless authentication
- **OAuth2** - Google login
- **WebSocket** - Real-time communication
- **PayOS SDK** - Thanh toán điện tử

### Frontend
- **Vue.js 3** - Progressive JavaScript framework
- **Vite** - Build tool
- **Vue Router** - Routing
- **SweetAlert2** - UI alerts
- **STOMP + SockJS** - WebSocket client

### Database
- **SQL Server** - Hệ quản trị cơ sở dữ liệu
- **Hibernate** - ORM framework

## ✨ Tính năng chính

### Khách hàng
- 🔐 Đăng ký/Đăng nhập (Username/Password, Google OAuth2)
- 🛍️ Xem danh sách sản phẩm, tìm kiếm, lọc
- 🛒 Giỏ hàng (thêm, cập nhật, xóa)
- 💳 Thanh toán đa phương thức (PayOS, chuyển khoản, COD)
- 📦 Theo dõi đơn hàng
- 💬 Chat hỗ trợ real-time
- ⭐ Đánh giá sản phẩm
- 👤 Quản lý hồ sơ cá nhân

### Quản trị viên
- 📦 Quản lý sản phẩm (thêm, sửa, xóa)
- 📂 Quản lý danh mục
- 📋 Quản lý đơn hàng
- 👥 Quản lý tài khoản
- 📊 Báo cáo doanh thu (ngày, tuần, tháng, quý, năm)
- 👑 Quản lý khách VIP
- 💬 Chat hỗ trợ khách hàng

## 📦 Cài đặt

### Yêu cầu
- Java 17+
- Node.js 18+
- SQL Server
- Maven 3.6+

### Cài đặt Backend

```bash
# Clone repository
git clone https://github.com/Trung-Hi/java6.git
cd java6

# Cấu hình database
# Tạo database SQL Server tên "ASM_Java5"

# Cấu hình ứng dụng
# Copy application.example.yml thành application.yml
cp src/main/resources/application.example.yml src/main/resources/application.yml

# Điền các giá trị cấu hình trong application.yml:
# - Database credentials
# - Google OAuth2 keys
# - PayOS keys
# - Email credentials
# - JWT secret

# Chạy ứng dụng
mvn spring-boot:run
```

Backend sẽ chạy trên port 8080.

### Cài đặt Frontend

```bash
# Di chuyển vào thư mục frontend
cd frontend

# Cài đặt dependencies
npm install

# Cấu hình environment variables
# Tạo file .env.local với:
VITE_GOONG_API_KEY=your_goong_api_key
VITE_GOONG_MAP_KEY=your_goong_map_key

# Chạy development server
npm run dev
```

Frontend sẽ chạy trên port 5173.

## ⚙️ Cấu hình

### Cấu hình Database
- Tạo database SQL Server: `ASM_Java5`
- Cấu hình connection trong `application.yml`

### Cấu hình Google OAuth2
1. Truy cập [Google Cloud Console](https://console.cloud.google.com/apis/credentials)
2. Tạo OAuth 2.0 Client ID (Web application)
3. Authorized redirect URI: `http://localhost:8080/login/oauth2/code/google`
4. Copy Client ID và Client Secret vào `application.yml`

### Cấu hình PayOS
1. Đăng ký tài khoản tại [PayOS](https://payos.vn/)
2. Lấy Client ID, API Key, Checksum Key
3. Điền vào `application.yml`

### Cấu hình Email
- Sử dụng Gmail App Password (không dùng password thật)
- Bật 2-Step Verification trên Google Account
- Tạo App Password và điền vào `application.yml`

## 📁 Cấu trúc dự án

```
FinalAsignment_Java6-main/
├── src/main/java/com/poly/ASM/
│   ├── config/          # Cấu hình Spring
│   ├── controller/      # API Controllers
│   ├── dto/            # Data Transfer Objects
│   ├── entity/         # JPA Entities
│   ├── repository/     # JPA Repositories
│   ├── security/       # Security config
│   └── service/        # Business logic
├── src/main/resources/
│   ├── templates/      # Thymeleaf templates
│   └── static/         # Static files
├── frontend/
│   ├── src/
│   │   ├── components/ # Vue components
│   │   ├── views/      # Vue pages
│   │   └── router/     # Vue Router config
│   └── package.json
└── pom.xml
```

## 🔐 Bảo mật

Các file cấu hình nhạy cảm đã được thêm vào `.gitignore`:
- `src/main/resources/application.yml`
- `src/main/resources/application.properties`
- `.env` files

Khi clone dự án về, hãy copy `application.example.yml` thành `application.yml` và điền các giá trị thật.

## 🚀 Chạy dự án

### Backend
```bash
mvn spring-boot:run
```
Truy cập: http://localhost:8080

### Frontend
```bash
cd frontend
npm run dev
```
Truy cập: http://localhost:5173

## 📚 Tài liệu

- [Báo cáo học thuật](BAO_CAO_HOC_LUAT.md) - Báo cáo chi tiết theo chuẩn học thuật
- [Báo cáo dự án](BAO_CAO_DU_AN.md) - Báo cáo tổng quan dự án

## 🤝 Đóng góp

Đóng góp được chào đón! Vui lòng mở Pull Request.

## 📝 License

Dự án này được tạo cho mục đích học tập.

## 👨‍💻 Tác giả

- **Trung Hi** - [GitHub](https://github.com/Trung-Hi)

---

## Cấu hình đăng nhập Google (OAuth2)

Project dùng Spring Security OAuth2 (Authorization Code) với endpoint bắt đầu đăng nhập:

- `GET /oauth2/authorization/google`

Callback mặc định của Spring Security:

- `GET /login/oauth2/code/google`

### Lỗi `Error 401: deleted_client`

Đây không phải lỗi code; nó xảy ra khi `client_id` đang dùng đã bị xoá/disabled trên Google Cloud, hoặc bạn đang chạy với bộ `GOOGLE_CLIENT_ID/GOOGLE_CLIENT_SECRET` cũ.

Chỗ cấu hình trong project:

- `application.yml` đọc từ cấu hình:
  - `spring.security.oauth2.client.registration.google.client-id`
  - `spring.security.oauth2.client.registration.google.client-secret`

### Cách sửa

1) Tạo lại OAuth Client ID trên Google Cloud Console

- APIs & Services → Credentials → Create Credentials → OAuth client ID
- Application type: Web application
- Authorized redirect URIs (local):
  - `http://localhost:8080/login/oauth2/code/google`

Nếu deploy domain thật, thêm đúng domain:

- `https://<domain>/login/oauth2/code/google`

2) Cập nhật cấu hình trong `application.yml`

- Set `client-id` và `client-secret` theo bộ credential mới.

### Kiểm tra nhanh

Mở link `http://localhost:8080/oauth2/authorization/google` và nhìn URL Google trả về (trên thanh địa chỉ) có query `client_id=...`.
`client_id` phải trùng với credential mới vừa tạo.
