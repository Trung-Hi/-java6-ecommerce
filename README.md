# E-Commerce Platform - Java 6 Project

## Tổng quan

Dự án là nền tảng thương mại điện tử đầy đủ tính năng được xây dựng với Spring Boot (Backend) và Vue.js (Frontend), sử dụng SQL Server làm cơ sở dữ liệu.

## Công nghệ sử dụng

### Backend
- **Java 17**
- **Spring Boot 4.0.1**
- **Spring Security** - Xác thực và phân quyền
- **Spring Data JPA** - ORM và truy vấn database
- **JWT (JSON Web Token)** - Xác thực stateless
- **Spring WebSocket** - Chat thời gian thực
- **PayOS** - Thanh toán trực tuyến
- **Apache POI** - Xuất báo cáo Excel

### Frontend
- **Vue.js 3**
- **Vite** - Build tool
- **Chart.js** - Biểu đồ thống kê
- **SweetAlert2** - Thông báo đẹp mắt

### Database
- **SQL Server** (MSSQL JDBC Driver)
- **H2** - Database cho test

## Các tính năng chính

### 1. Xác thực & Phân quyền
- Đăng nhập/Đăng ký
- JWT Authentication (Access Token + Refresh Token)
- OAuth2 Google Login
- Quản lý vai trò (Admin, User)
- Reset mật khẩu

### 2. Quản lý sản phẩm
- CRUD sản phẩm
- Quản lý danh mục
- Product Variants (Size, Color, Material)
- Upload hình ảnh
- Discount & Promotion

### 3. Giỏ hàng & Thanh toán
- Thêm/xóa sản phẩm vào giỏ
- Cập nhật số lượng
- Checkout với địa chỉ giao hàng
- Thanh toán chuyển khoản ngân hàng
- Thanh toán PayOS
- COD (Cash on Delivery)

### 4. Quản lý đơn hàng
- Theo dõi trạng thái đơn hàng
- Xem lịch sử đơn hàng
- Yêu cầu hoàn tiền
- Đổi trả sản phẩm
- Tính phí giao hàng dựa trên khoảng cách

### 5. Chat hỗ trợ
- Chat thời gian thực giữa khách hàng và admin
- Upload hình ảnh trong chat
- WebSocket connection
- Modal sản phẩm trong chat

### 6. Thống kê & Báo cáo (Admin)
- Dashboard doanh thu
- Biểu đồ xu hướng doanh thu theo tháng
- Top sản phẩm bán chạy
- Thống kê trạng thái đơn hàng
- Báo cáo khách hàng VIP
- Xuất báo cáo Excel

### 7. Quản lý tài khoản (Admin)
- Quản lý người dùng
- Phân quyền vai trò
- Kích hoạt/vô hiệu hóa tài khoản
- Reset mật khẩu

## Kiến trúc REST API

### Authentication Flow
```
User Login → JWT Token (Access + Refresh) → HTTP-only Cookie → 
API Request → JwtAuthenticationFilter → Verify Token → 
SecurityContext → Controller
```

### Các REST Controllers chính
- `AuthRestController` - `/api/auth` - Authentication
- `OrderRestController` - `/api/orders` - Quản lý đơn hàng
- `ProductRestController` - `/api/products` - Quản lý sản phẩm
- `UserRestController` - `/api/users` - Quản lý người dùng
- `ChatRestController` - `/api/chat` - Chat hỗ trợ
- Các Admin Controllers - `/api/admin/*` - Admin management

### Frontend API Client
File: `frontend/src/api.js`
- Định nghĩa tất cả các hàm gọi API
- Sử dụng `fetch()` với credentials
- Tự động xử lý 401/403 (redirect login)
- Quản lý JWT token trong cookies

## Cấu trúc dự án

```
FinalAsignment_Java6-main/
├── src/main/java/com/poly/ASM/
│   ├── controller/          # REST Controllers
│   │   ├── admin/          # Admin endpoints
│   │   ├── web/            # Public endpoints
│   │   └── advice/         # Global exception handling
│   ├── service/            # Business logic
│   ├── repository/         # JPA Repositories
│   ├── entity/             # Database entities
│   ├── dto/                # Data Transfer Objects
│   ├── security/           # JWT, Security config
│   └── config/             # Configuration
├── frontend/
│   ├── src/
│   │   ├── api.js          # API client
│   │   ├── views/          # Vue pages
│   │   ├── components/     # Vue components
│   │   ├── router/         # Vue Router
│   │   └── composables/    # Vue composables
│   └── vite.config.js
├── docs/                   # Documentation
├── java6.sql              # Database schema
└── pom.xml                # Maven dependencies
```

## Cài đặt

### Prerequisites
- Java 17+
- Node.js 18+
- SQL Server
- Maven

### Backend Setup
```bash
cd FinalAsignment_Java6-main
# Cấu hình database trong application.properties
mvn spring-boot:run
```

### Frontend Setup
```bash
cd frontend
npm install
npm run dev
```

### Database
```bash
# Chạy file java6.sql để tạo database và tables
# Cấu hình connection string trong application.properties
```

## Cấu hình quan trọng

### JWT Configuration (application.properties)
```properties
jwt.secret=<base64-encoded-secret-key>
jwt.access-token-expiration-ms=<milliseconds>
jwt.refresh-token-expiration-ms=<milliseconds>
```

### Database Configuration
```properties
spring.datasource.url=jdbc:sqlserver://localhost:1433;databaseName=java6
spring.datasource.username=sa
spring.datasource.password=<password>
```

## Bảo mật

- JWT Token lưu trong HTTP-only Cookies
- Password encryption với BCrypt
- CSRF Protection
- Role-based Access Control (RBAC)
- Token Blacklist cho logout
- Refresh Token rotation

## License

Dự án này được tạo cho mục đích học tập Java 6.
