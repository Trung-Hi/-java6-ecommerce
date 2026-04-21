# BÁO CÁO DỰ ÁN E-COMMERCE
## Hệ thống bán hàng trực tuyến

---

## MỤC LỤC

1. [Giới thiệu dự án](#1-giới-thiệu-dự-án)
2. [Cấu trúc tổng thể](#2-cấu-trúc-tổng-thể)
3. [Kiến trúc Backend](#3-kiến-trúc-backend)
4. [Kiến trúc Frontend](#4-kiến-trúc-frontend)
5. [Database Schema](#5-database-schema)
6. [API Endpoints](#6-api-endpoints)
7. [Chức năng chính](#7-chức-năng-chính)
8. [Cấu hình và Dependencies](#8-cấu-hình-và-dependencies)
9. [Triển khai](#9-triển-khai)
10. [Kết luận](#10-kết-luận)

---

## 1. GIỚI THIỆU DỰ ÁN

### 1.1 Tên dự án
- **Tên:** FinalAsignment_Java6
- **Loại:** Hệ thống E-commerce bán hàng trực tuyến
- **Ngôn ngữ lập trình:** Java 17, Vue.js 3

### 1.2 Mô tả tổng quan
Dự án là hệ thống bán hàng trực tuyến hoàn chỉnh với các tính năng:
- Quản lý sản phẩm, danh mục, size
- Giỏ hàng và thanh toán (PayOS, chuyển khoản)
- Quản lý đơn hàng và giao hàng
- Chat hỗ trợ khách hàng
- Đánh giá sản phẩm
- Quản lý tài khoản người dùng
- Báo cáo doanh thu
- Quản lý VIP khách hàng

### 1.3 Công nghệ sử dụng
- **Backend:** Spring Boot 4.0.1, Spring Security, JPA, WebSocket
- **Frontend:** Vue.js 3, Vite, Vue Router, SweetAlert2
- **Database:** SQL Server
- **Thanh toán:** PayOS
- **Xác thực:** JWT, OAuth2 (Google)

---

## 2. CẤU TRÚC TỔNG THỂ

### 2.1 Cấu trúc thư mục project

```
FinalAsignment_Java6-main/
├── src/
│   ├── main/
│   │   ├── java/com/poly/ASM/
│   │   │   ├── config/          # Cấu hình Spring
│   │   │   ├── controller/      # API Controllers
│   │   │   │   ├── admin/      # Admin APIs
│   │   │   │   ├── web/        # Web APIs
│   │   │   │   └── advice/     # Exception handling
│   │   │   ├── dto/            # Data Transfer Objects
│   │   │   ├── entity/         # JPA Entities
│   │   │   ├── exception/      # Custom exceptions
│   │   │   ├── repository/     # JPA Repositories
│   │   │   ├── security/       # Security config
│   │   │   └── service/        # Business logic
│   │   └── resources/
│   │       ├── application.yml # Config
│   │       ├── templates/     # Thymeleaf templates
│   │       └── static/        # Static files
│   └── test/
├── frontend/
│   ├── src/
│   │   ├── components/        # Vue components
│   │   ├── views/            # Vue pages
│   │   ├── router/           # Vue Router config
│   │   ├── api.js            # API client
│   │   └── legacy/           # Legacy pages
│   └── package.json
├── sql/                      # SQL scripts
├── pom.xml                   # Maven config
└── README.md
```

### 2.2 Tổng quan số lượng file
- **Backend:** 35 file Java
- **Frontend:** 35 file Vue
- **Entities:** 15 entities
- **Services:** 34 services
- **Repositories:** 19 repositories
- **Controllers:** 21 controllers

---

## 3. KIẾN TRÚC BACKEND

### 3.1 Controllers (21 file)

#### Admin Controllers (6)
1. **AccountAController.java** - Quản lý tài khoản admin
2. **AdminChatRestController.java** - Chat admin API
3. **CategoryAController.java** - Quản lý danh mục
4. **OrderAController.java** - Quản lý đơn hàng admin
5. **ProductAController.java** - Quản lý sản phẩm
6. **ReportAController.java** - Báo cáo doanh thu

#### Web Controllers (13)
1. **AccountController.java** - Quản lý tài khoản user
2. **AuthController.java** - Xác thực
3. **AuthRestController.java** - Auth REST API
4. **CartController.java** - Giỏ hàng
5. **ChatRestController.java** - Chat API
6. **ChatUploadController.java** - Upload ảnh chat
7. **ChatWsController.java** - WebSocket chat
8. **HomeController.java** - Trang chủ
9. **LocationController.java** - Địa vị trí
10. **NotificationController.java** - Thông báo
11. **OrderController.java** - Đơn hàng
12. **OrderRestController.java** - Order REST API
13. **PasswordResetController.java** - Reset mật khẩu
14. **ProductController.java** - Sản phẩm
15. **ProductRestController.java** - Product REST API
16. **ProductReviewController.java** - Đánh giá sản phẩm
17. **SpaForwardController.java** - SPA forward
18. **UserRestController.java** - User API

#### Advice Controllers (2)
1. **GlobalAdvice.java** - Global exception handling
2. **RestApiExceptionHandler.java** - REST exception handling

### 3.2 Services (34 file)

#### Auth Services (5)
- AuthService, AuthProviderService, PasswordResetService
- AuthServiceImpl, PasswordResetServiceImpl

#### Cart Services (3)
- CartService, CartItem, CartServiceImpl

#### Chat Services (2)
- ChatService, ChatServiceImpl

#### Email Services (2)
- EmailService, EmailServiceImpl

#### Notification Services (2)
- NotificationService, NotificationServiceImpl

#### Order Services (5)
- OrderService, OrderDetailService, ReportService
- OrderAutoCancelService
- OrderServiceImpl, OrderDetailServiceImpl, ReportServiceImpl

#### Payment Services (2)
- PayosPaymentService, PayosPaymentServiceImpl

#### Product Services (8)
- CategoryService, ProductService, ProductSizeService, SizeService
- CategoryServiceImpl, ProductServiceImpl, ProductSizeServiceImpl, SizeServiceImpl

#### Review Services (2)
- ProductReviewService, ProductReviewServiceImpl

#### User Services (3)
- AccountService, AuthorityService, RoleService
- AccountServiceImpl, AuthorityServiceImpl, RoleServiceImpl

### 3.3 Entities (15 file)

#### User Entities (4)
- Account - Thông tin tài khoản
- Authority - Quyền hạn
- Role - Vai trò
- PasswordResetToken - Token reset mật khẩu

#### Product Entities (4)
- Product - Sản phẩm
- Category - Danh mục
- Size - Size
- ProductSize - Kết nối Product-Size

#### Order Entities (2)
- Order - Đơn hàng
- OrderDetail - Chi tiết đơn hàng

#### Cart Entities (1)
- CartItemEntity - Item giỏ hàng

#### Review Entities (1)
- ProductReview - Đánh giá sản phẩm

#### Chat Entities (1)
- ChatMessage - Tin nhắn chat

#### Notification Entities (1)
- Notification - Thông báo

#### Common Entities (1)
- BaseEntity - Entity base

### 3.4 Repositories (19 file)

#### User Repositories (4)
- AccountRepository, AuthorityRepository, RoleRepository
- PasswordResetTokenRepository

#### Product Repositories (6)
- ProductRepository, CategoryRepository, SizeRepository
- ProductSizeRepository, ProductMasterRepository, AttributeValueRepository

#### Order Repositories (4)
- OrderRepository, OrderDetailRepository
- RevenueReport, VipReport

#### Other Repositories (5)
- CartItemRepository, ChatMessageRepository, NotificationRepository
- ProductReviewRepository, ProductReviewStats

### 3.5 Security Configuration

**File:** SecurityConfig.java

**Features:**
- JWT Authentication
- OAuth2 (Google Login)
- Role-based access control (ADMIN, USER)
- Password encoding (BCrypt)
- CORS configuration

---

## 4. KIẾN TRÚC FRONTEND

### 4.1 Components (14 file)

#### Layout Components (4)
- AppHeader.vue - Header navigation
- AppFooter.vue - Footer
- AdminLayout.vue - Admin layout
- AdminNav.vue - Admin navigation

#### UI Components (2)
- CurrencyInput.vue - Input tiền tệ
- AdminTableContainer.vue - Container bảng admin

#### Chat Components (3)
- ChatBox.vue - Box chat
- ChatCameraCapture.vue - Chụp ảnh chat
- ProductModal.vue - Modal sản phẩm chat

#### Other Components (5)
- AddProductModal.vue - Modal thêm sản phẩm

### 4.2 Views (35 file)

#### Account Views (4)
- LoginView.vue - Đăng nhập
- SignUpView.vue - Đăng ký
- EditProfileView.vue - Sửa hồ sơ
- ChangePasswordView.vue - Đổi mật khẩu
- ForgotPasswordView.vue - Quên mật khẩu

#### Admin Views (8)
- AdminAccountView.vue - Quản lý tài khoản
- AdminCategoryView.vue - Quản lý danh mục
- AdminProductView.vue - Quản lý sản phẩm
- AdminOrderView.vue - Quản lý đơn hàng
- AdminRevenueView.vue - Báo cáo doanh thu
- AdminVipView.vue - Quản lý VIP
- AdminChatView.vue - Chat admin
- AdminOrderView_New.vue - Admin order mới

#### Order Views (6)
- CheckoutView.vue - Thanh toán
- BankTransferView.vue - Chuyển khoản
- OrderListView.vue - Danh sách đơn hàng
- OrderDetailView.vue - Chi tiết đơn hàng
- MyProductListView.vue - Sản phẩm đã mua
- OrderListView_Backup.vue - Backup order list

#### Product Views (2)
- ProductListView.vue - Danh sách sản phẩm
- ProductDetailView.vue - Chi tiết sản phẩm

#### Other Views (1)
- HomeView.vue - Trang chủ
- CartView.vue - Giỏ hàng

### 4.3 Router Configuration

**File:** router/index.js

**Routes (27 routes):**

#### Public Routes
- /auth/login - Đăng nhập
- /home/index - Trang chủ
- /product/list - Danh sách sản phẩm
- /product/detail - Chi tiết sản phẩm
- /account/sign-up - Đăng ký
- /account/forgot-password - Quên mật khẩu

#### Authenticated Routes
- /cart/index - Giỏ hàng
- /order/check-out - Thanh toán
- /order/bank-transfer - Chuyển khoản
- /order/order-list - Đơn hàng
- /order/order-detail - Chi tiết đơn hàng
- /order/my-product-list - Sản phẩm đã mua
- /account/edit-profile - Sửa hồ sơ
- /account/change-password - Đổi mật khẩu

#### Admin Routes
- /admin/account - Quản lý tài khoản
- /admin/category - Quản lý danh mục
- /admin/product - Quản lý sản phẩm
- /admin/order - Quản lý đơn hàng
- /admin/revenue - Báo cáo tổng
- /admin/revenue/day - Báo cáo ngày
- /admin/revenue/week - Báo cáo tuần
- /admin/revenue/month - Báo cáo tháng
- /admin/revenue/quarter - Báo cáo quý
- /admin/revenue/year - Báo cáo năm
- /admin/vip - Quản lý VIP
- /admin/chat - Chat admin

**Navigation Guards:**
- Kiểm tra authentication
- Kiểm tra admin role
- Redirect khi chưa đăng nhập
- Block Google users đổi mật khẩu

---

## 5. DATABASE SCHEMA

### 5.1 Entities và Relationships

#### Account (Tài khoản)
- id (PK)
- username (unique)
- password
- email (unique)
- fullname
- phone
- address
- photo
- activated
- isDelete (soft delete)
- accountType (NORMAL/GOOGLE)

#### Role (Vai trò)
- id (PK)
- name (unique)
- description

#### Authority (Quyền hạn)
- id (PK)
- account_id (FK → Account)
- role_id (FK → Role)

#### Product (Sản phẩm)
- id (PK)
- name
- price
- discount
- quantity
- image
- description
- category_id (FK → Category)
- isDelete (soft delete)
- createDate

#### Category (Danh mục)
- id (PK)
- name (unique)
- description

#### Size (Size)
- id (PK)
- name (unique)
- description

#### ProductSize (Kết nối Product-Size)
- id (PK)
- product_id (FK → Product)
- size_id (FK → Size)
- quantity

#### Order (Đơn hàng)
- id (PK)
- account_id (FK → Account)
- address
- totalAmount
- status (PENDING_PAYMENT, PLACED_PAID, SHIPPING_PAID, DELIVERED, CANCELLED)
- createDate
- deliveryDistanceM
- expectedDeliveryDate
- pay_checkout_url
- pay_qr_code
- pay_account_name
- pay_account_number
- pay_bank_bin
- pay_payment_link_id

#### OrderDetail (Chi tiết đơn hàng)
- id (PK)
- order_id (FK → Order)
- product_id (FK → Product)
- sizeId
- sizeName
- price
- quantity

#### CartItem (Item giỏ hàng)
- id (PK)
- account_id (FK → Account)
- product_id (FK → Product)
- sizeId
- sizeName
- quantity

#### ProductReview (Đánh giá)
- id (PK)
- product_id (FK → Product)
- account_id (FK → Account)
- rating (1-5)
- comment
- createDate

#### ChatMessage (Tin nhắn chat)
- id (PK)
- sender_id (FK → Account)
- receiver_id (FK → Account)
- message
- imageUrl
- isRead
- createTime

#### Notification (Thông báo)
- id (PK)
- account_id (FK → Account)
- message
- isRead
- createTime

### 5.2 Database
- **Type:** SQL Server
- **Connection:** JDBC
- **Version:** 12.4.2.jre11

---

## 6. API ENDPOINTS

### 6.1 Authentication APIs

#### POST /api/auth/login
- Đăng nhập với username/password
- Returns: JWT token, user info

#### GET /oauth2/authorization/google
- Đăng nhập Google OAuth2

#### GET /login/oauth2/code/google
- OAuth2 callback

#### POST /api/auth/signup
- Đăng ký tài khoản mới

#### GET /api/auth/me
- Lấy thông tin user hiện tại

#### POST /api/auth/reset-password
- Reset mật khẩu

### 6.2 Product APIs

#### GET /api/products
- Lấy danh sách sản phẩm
- Params: page, keyword, categoryId, minPrice, maxPrice, sort

#### GET /api/products/{id}
- Lấy chi tiết sản phẩm

#### POST /api/admin/products
- Tạo sản phẩm mới (Admin)

#### PUT /api/admin/products/{id}
- Cập nhật sản phẩm (Admin)

#### DELETE /api/admin/products/{id}
- Xóa sản phẩm (Admin)

### 6.3 Cart APIs

#### GET /api/cart
- Lấy giỏ hàng

#### POST /api/cart
- Thêm vào giỏ hàng

#### PUT /api/cart/{id}
- Cập nhật số lượng

#### DELETE /api/cart/{id}
- Xóa item

#### DELETE /api/cart
- Xóa toàn bộ giỏ hàng

### 6.4 Order APIs

#### GET /api/order-workflow/checkout
- Lấy dữ liệu checkout

#### POST /api/order-workflow/checkout
- Tạo đơn hàng

#### GET /api/orders
- Lấy danh sách đơn hàng

#### GET /api/orders/{id}
- Lấy chi tiết đơn hàng

#### PUT /api/orders/{id}/shipping
- Cập nhật địa chỉ giao hàng

#### POST /api/orders/bank-transfer
- Tạo link thanh toán chuyển khoản

#### POST /api/orders/bank-transfer/confirm
- Xác nhận chuyển khoản

#### POST /api/orders/{id}/cancel
- Hủy đơn hàng

#### POST /api/orders/{id}/refund
- Yêu cầu hoàn tiền

### 6.5 Admin APIs

#### GET /api/admin/accounts
- Lấy danh sách tài khoản (Admin)

#### POST /api/admin/accounts
- Tạo tài khoản (Admin)

#### PUT /api/admin/accounts/{username}
- Cập nhật tài khoản (Admin)

#### DELETE /api/admin/accounts/{username}
- Xóa tài khoản (Admin)

#### GET /api/admin/categories
- Lấy danh sách danh mục (Admin)

#### POST /api/admin/categories
- Tạo danh mục (Admin)

#### PUT /api/admin/categories/{id}
- Cập nhật danh mục (Admin)

#### DELETE /api/admin/categories/{id}
- Xóa danh mục (Admin)

#### GET /api/admin/orders
- Lấy danh sách đơn hàng (Admin)

#### PUT /api/admin/orders/{id}/status
- Cập nhật trạng thái đơn hàng (Admin)

#### GET /api/admin/revenue
- Báo cáo doanh thu (Admin)

#### GET /api/admin/vip
- Báo cáo khách hàng VIP (Admin)

### 6.6 Chat APIs

#### GET /api/chat/conversations
- Lấy danh sách hội thoại

#### GET /api/chat/messages/{username}
- Lấy tin nhắn với user

#### POST /api/chat/send
- Gửi tin nhắn

#### POST /api/chat/upload
- Upload ảnh chat

### 6.7 Review APIs

#### GET /api/reviews/product/{productId}
- Lấy đánh giá sản phẩm

#### POST /api/reviews
- Thêm đánh giá

---

## 7. CHỨC NĂNG CHÍNH

### 7.1 Quản lý Sản phẩm

**Features:**
- Thêm/Sửa/Xóa sản phẩm
- Quản lý danh mục
- Quản lý size sản phẩm
- Upload ảnh sản phẩm
- Tìm kiếm, lọc, sắp xếp
- Giảm giá sản phẩm

**Files:**
- AdminProductView.vue
- ProductAController.java
- ProductService.java

### 7.2 Giỏ hàng

**Features:**
- Thêm sản phẩm vào giỏ
- Cập nhật số lượng
- Xóa item
- Tính tổng tiền
- Lưu giỏ hàng theo session

**Files:**
- CartView.vue
- CartController.java
- CartService.java

### 7.3 Thanh toán

**Features:**
- Checkout với địa chỉ
- Thanh toán PayOS
- Thanh toán chuyển khoản
- Tính phí giao hàng
- Ước tính thời gian giao

**Files:**
- CheckoutView.vue
- BankTransferView.vue
- OrderController.java
- PayosPaymentService.java

### 7.4 Quản lý Đơn hàng

**Features:**
- Tạo đơn hàng
- Theo dõi trạng thái đơn
- Hủy đơn hàng
- Yêu cầu hoàn tiền
- Cập nhật địa chỉ giao hàng

**Trạng thái đơn:**
- PENDING_PAYMENT - Chờ thanh toán
- PLACED_PAID - Đã đặt, đã thanh toán
- SHIPPING_PAID - Đang giao
- DELIVERED - Đã giao
- CANCELLED - Đã hủy

**Files:**
- OrderListView.vue
- OrderDetailView.vue
- OrderController.java

### 7.5 Chat Hỗ trợ

**Features:**
- Chat real-time với WebSocket
- Gửi tin nhắn văn bản
- Gửi ảnh
- Thông báo tin nhắn mới
- Lịch sử hội thoại

**Files:**
- ChatBox.vue
- ChatWsController.java
- ChatService.java

### 7.6 Đánh giá Sản phẩm

**Features:**
- Đánh giá 1-5 sao
- Viết bình luận
- Xem đánh giá sản phẩm
- Thống kê đánh giá

**Files:**
- ProductReviewController.java
- ProductReviewService.java

### 7.7 Quản lý Tài khoản

**Features:**
- Đăng ký/Đăng nhập
- Đăng nhập Google
- Sửa hồ sơ
- Đổi mật khẩu
- Quên mật khẩu
- Upload ảnh avatar

**Files:**
- AccountController.java
- AuthController.java
- AuthService.java

### 7.8 Báo cáo Doanh thu

**Features:**
- Báo cáo tổng
- Báo cáo theo ngày
- Báo cáo theo tuần
- Báo cáo theo tháng
- Báo cáo theo quý
- Báo cáo theo năm
- Xuất Excel

**Files:**
- AdminRevenueView.vue
- ReportAController.java
- ReportService.java

### 7.9 Quản lý VIP

**Features:**
- Danh sách khách VIP
- Thống kê chi tiêu
- Phân loại khách hàng

**Files:**
- AdminVipView.vue
- VipReport.java

---

## 8. CẤU HÌNH VÀ DEPENDENCIES

### 8.1 Backend Dependencies (pom.xml)

#### Spring Boot
- spring-boot-starter-data-jpa 4.0.1
- spring-boot-starter-thymeleaf
- spring-boot-starter-validation
- spring-boot-starter-security
- spring-boot-starter-oauth2-client
- spring-boot-starter-webmvc
- spring-boot-starter-websocket
- spring-boot-starter-mail

#### Security
- jjwt-api 0.12.7
- jjwt-impl 0.12.7
- jjwt-jackson 0.12.7

#### Payment
- payos-java 2.0.1

#### Reporting
- poi 5.4.1
- poi-ooxml 5.4.1

#### Database
- mssql-jdbc 12.4.2.jre11
- h2 (test only)

#### Development
- lombok
- spring-boot-devtools

### 8.2 Frontend Dependencies (package.json)

#### Core
- vue 3.5.13
- vue-router 4.5.1
- vite 5.4.10

#### WebSocket
- @stomp/stompjs 7.0.0
- sockjs-client 1.6.1

#### UI
- sweetalert2 11.26.24

### 8.3 Configuration (application.yml)

```yaml
spring:
  application:
    name: Asignment_Java5
  datasource:
    url: jdbc:sqlserver://localhost:1433;databaseName=ASM_Java5
    username: sa
    password: 123456
  jpa:
    hibernate:
      ddl-auto: update
  servlet:
    multipart:
      max-file-size: 10MB
      max-request-size: 20MB
  security:
    oauth2:
      client:
        registration:
          google:
            client-id: ${GOOGLE_CLIENT_ID}
            client-secret: ${GOOGLE_CLIENT_SECRET}
  mail:
    host: ${MAIL_HOST}
    port: ${MAIL_PORT}
    username: ${MAIL_USERNAME}
    password: ${MAIL_PASSWORD}

server:
  port: 8080

payos:
  client-id: ${PAYOS_CLIENT_ID}
  api-key: ${PAYOS_API_KEY}
  checksum-key: ${PAYOS_CHECKSUM_KEY}

jwt:
  secret: ${JWT_SECRET}
  access-token-expiration-ms: 86400000
  refresh-token-expiration-ms: 604800000
```

---

## 9. TRIỂN KHAI

### 9.1 Cài đặt Backend

**Yêu cầu:**
- Java 17+
- Maven 3.6+
- SQL Server

**Cài đặt:**
```bash
cd FinalAsignment_Java6-main
mvn clean install
mvn spring-boot:run
```

**Environment Variables:**
- GOOGLE_CLIENT_ID
- GOOGLE_CLIENT_SECRET
- PAYOS_CLIENT_ID
- PAYOS_API_KEY
- PAYOS_CHECKSUM_KEY
- JWT_SECRET
- MAIL_HOST
- MAIL_PORT
- MAIL_USERNAME
- MAIL_PASSWORD

### 9.2 Cài đặt Frontend

**Yêu cầu:**
- Node.js 18+
- npm

**Cài đặt:**
```bash
cd frontend
npm install
npm run dev
```

**Environment Variables (.env.local):**
```
VITE_GOONG_API_KEY=your_goong_api_key
VITE_GOONG_MAP_KEY=your_goong_map_key
```

### 9.3 Database Setup

1. Tạo database SQL Server: ASM_Java5
2. Chạy ứng dụng (Hibernate sẽ tự động tạo tables)

### 9.4 Build Production

**Backend:**
```bash
mvn clean package
java -jar target/Asignment_Java5-0.0.1-SNAPSHOT.jar
```

**Frontend:**
```bash
npm run build
```

---

## 10. KẾT LUẬN

### 10.1 Tổng kết
Dự án là hệ thống E-commerce hoàn chỉnh với:
- 35 file Java backend
- 35 file Vue frontend
- 15 entities database
- 34 services
- 21 controllers
- 27 routes frontend

### 10.2 Điểm mạnh
- Kiến trúc RESTful API rõ ràng
- Tách biệt frontend/backend
- Hỗ trợ thanh toán PayOS
- Chat real-time với WebSocket
- Báo cáo doanh thu chi tiết
- OAuth2 Google login
- Soft delete entities

### 10.3 Có thể cải thiện
- Thêm unit tests
- Tối ưu hóa performance
- Thêm caching
- CI/CD pipeline
- Docker containerization

### 10.4 Kỹ năng sử dụng
- Spring Boot
- Spring Security
- JPA/Hibernate
- Vue.js 3
- WebSocket
- JWT Authentication
- OAuth2
- PayOS Integration
- SQL Server

---

**Người lập báo cáo:** Cascade AI Assistant
**Ngày lập:** 21/04/2026
**Phiên bản:** 1.0
