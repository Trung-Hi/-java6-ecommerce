# BÁO CÁO ĐỒ ÁN TỐT NGHIỆP
## HỆ THỐNG E-COMMERCE TRỰC TUYẾN

---

# MỤC LỤC

CHƯƠNG 1: GIỚI THIỆU................................................... 1
1.1 Lý do chọn đề tài................................................. 1
1.2 Mục tiêu hệ thống................................................. 2
1.3 Phạm vi dự án...................................................... 3
1.4 Đối tượng sử dụng................................................. 4

CHƯƠNG 2: CƠ SỞ LÝ THUYẾT........................................... 5
2.1 Các công nghệ sử dụng............................................. 5
2.1.1 Backend: Java và Spring Boot.............................. 5
2.1.2 Frontend: Vue.js 3........................................ 6
2.1.3 Database: SQL Server..................................... 7
2.2 Mô hình kiến trúc................................................ 8
2.2.1 Kiến trúc MVC............................................... 8
2.2.2 RESTful API................................................. 9
2.2.3 Microservices Architecture.................................. 10
2.3 Các khái niệm liên quan........................................... 11
2.3.1 Authentication và Authorization........................... 11
2.3.2 JWT (JSON Web Token).................................... 12
2.3.3 OAuth2................................................... 13
2.3.4 WebSocket............................................... 14

CHƯƠNG 3: PHÂN TÍCH HỆ THỐNG....................................... 15
3.1 Mô tả nghiệp vụ.................................................. 15
3.2 Use case........................................................ 16
3.2.1 Use case Đăng ký/Đăng nhập............................... 16
3.2.2 Use case Quản lý sản phẩm............................... 17
3.2.3 Use case Giỏ hàng và Thanh toán......................... 18
3.2.4 Use case Quản lý đơn hàng............................... 19
3.3 Phân tích chức năng............................................. 20
3.3.1 Chức năng Quản lý sản phẩm............................. 20
3.3.2 Chức năng Giỏ hàng..................................... 21
3.3.3 Chức năng Thanh toán................................... 22
3.3.4 Chức năng Quản lý đơn hàng............................. 23
3.3.5 Chức năng Chat hỗ trợ.................................. 24
3.4 Phân tích dữ liệu............................................... 25

CHƯƠNG 4: THIẾT KẾ HỆ THỐNG........................................ 26
4.1 Thiết kế kiến trúc tổng thể....................................... 26
4.2 Thiết kế database................................................ 27
4.2.1 Entity Account........................................... 27
4.2.2 Entity Product........................................... 28
4.2.3 Entity Order............................................. 29
4.2.4 Các entity khác.......................................... 30
4.3 Thiết kế giao diện (UI/UX)....................................... 31
4.4 Thiết kế API.................................................... 32

CHƯƠNG 5: TRIỂN KHAI HỆ THỐNG...................................... 33
5.1 Mô tả code chính................................................. 33
5.2 Giải thích các module quan trọng.................................. 34
5.2.1 Module Authentication................................... 34
5.2.2 Module Payment......................................... 35
5.2.3 Module Chat.............................................. 36
5.3 Luồng xử lý dữ liệu............................................. 37

CHƯƠNG 6: KIỂM THỬ VÀ ĐÁNH GIÁ................................... 38
6.1 Test case....................................................... 38
6.2 Kết quả đạt được................................................. 39
6.3 Ưu điểm........................................................ 40
6.4 Nhược điểm.................................................... 41

CHƯƠNG 7: KẾT LUẬN VÀ HƯỚNG PHÁT TRIỂN...................... 42
7.1 Tổng kết....................................................... 42
7.2 Hướng phát triển................................................ 43

---

# CHƯƠNG 1: GIỚI THIỆU

## 1.1 Lý do chọn đề tài

Trong bối cảnh nền kinh tế số đang phát triển mạnh mẽ tại Việt Nam, thương mại điện tử (e-commerce) đã trở thành một xu hướng tất yếu và không thể đảo ngược. Theo báo cáo của Hiệp hội Thương mại Điện tử Việt Nam (VECOM), quy mô thị trường e-commerce Việt Nam đạt khoảng 25 tỷ USD vào năm 2023 và dự kiến sẽ tiếp tục tăng trưởng ổn định trong những năm tới.

Sự phát triển này tạo ra nhu cầu cấp thiết về các hệ thống bán hàng trực tuyến hiện đại, an toàn và hiệu quả. Các doanh nghiệp vừa và nhỏ (SMEs) cần có giải pháp công nghệ để quản lý bán hàng, tương tác với khách hàng và tối ưu hóa quy trình vận hành. Tuy nhiên, nhiều doanh nghiệp vẫn gặp khó khăn trong việc xây dựng hệ thống e-commerce do chi phí cao, thời gian triển khai dài, và thiếu đội ngũ kỹ thuật có chuyên môn.

Xuất phát từ thực tế trên, nhóm tác giả quyết định thực hiện đề tài "Xây dựng hệ thống E-commerce trực tuyến" nhằm cung cấp một giải pháp hoàn chỉnh, tích hợp các chức năng thiết yếu của một sàn thương mại điện tử hiện đại. Hệ thống được xây dựng dựa trên các công nghệ nguồn mở phổ biến, giúp giảm chi phí triển khai và dễ dàng tùy chỉnh theo nhu cầu cụ thể của từng doanh nghiệp.

Đề tài này không chỉ mang ý nghĩa thực tiễn trong việc giải quyết bài toán quản lý bán hàng trực tuyến mà còn đóng góp vào kho tàng kiến thức về phát triển ứng dụng web theo kiến trúc hiện đại (microservices, RESTful API), tích hợp các dịch vụ thanh toán điện tử (PayOS), và ứng dụng các công nghệ mới như WebSocket cho giao tiếp real-time.

## 1.2 Mục tiêu hệ thống

Hệ thống E-commerce được xây dựng nhằm đạt được các mục tiêu sau:

**Mục tiêu chức năng:**
- Cung cấp nền tảng mua bán trực tuyến hoàn chỉnh cho doanh nghiệp
- Quản lý danh mục sản phẩm, danh mục, và biến thể sản phẩm (size, màu sắc)
- Hỗ trợ quy trình mua hàng từ thêm vào giỏ hàng đến thanh toán
- Tích hợp đa phương thức thanh toán (PayOS, chuyển khoản ngân hàng, COD)
- Quản lý đơn hàng theo vòng đời đầy đủ (đặt hàng, giao hàng, hoàn thành, hủy)
- Cung cấp hệ thống chat hỗ trợ khách hàng real-time
- Quản lý tài khoản người dùng với phân quyền chi tiết
- Hệ thống báo cáo doanh thu và thống kê khách hàng VIP
- Tích hợp chức năng đánh giá sản phẩm

**Mục tiêu phi chức năng:**
- Đảm bảo tính bảo mật và an toàn thông tin cho người dùng
- Cung cấp giao diện người dùng thân thiện, dễ sử dụng
- Đảm bảo hiệu suất hệ thống với khả năng mở rộng (scalability)
- Hỗ trợ đa nền tảng (web, mobile responsive)
- Cung cấp tính sẵn sàng cao (high availability)
- Đảm bảo tính nhất quán dữ liệu (data consistency)

**Mục tiêu kỹ thuật:**
- Áp dụng kiến trúc RESTful API để tách biệt frontend và backend
- Sử dụng các công nghệ hiện đại và phổ biến trong cộng đồng phát triển
- Tuân thủ các nguyên tắc thiết kế phần mềm (SOLID, DRY, KISS)
- Triển khai theo mô hình containerization (Docker-ready)
- Tài liệu hóa hệ thống đầy đủ để bảo trì và phát triển tiếp

## 1.3 Phạm vi dự án

Dự án tập trung vào phát triển một hệ thống e-commerce hoàn chỉnh với các phạm vi sau:

**Phạm vi chức năng:**
- Quản lý sản phẩm: Thêm, sửa, xóa, tìm kiếm, lọc sản phẩm
- Quản lý danh mục: Quản lý cây danh mục sản phẩm
- Quản lý giỏ hàng: Thêm, cập nhật, xóa item, tính tổng tiền
- Thanh toán: Tích hợp PayOS, chuyển khoản, COD
- Quản lý đơn hàng: Theo dõi trạng thái, cập nhật địa chỉ, hủy đơn
- Chat hỗ trợ: Giao tiếp real-time giữa admin và khách hàng
- Đánh giá sản phẩm: Đánh giá sao, bình luận
- Quản lý tài khoản: Đăng ký, đăng nhập, quản lý profile
- Báo cáo doanh thu: Báo cáo theo ngày, tuần, tháng, quý, năm
- Quản lý VIP: Phân loại khách hàng theo mức chi tiêu

**Phạm vi kỹ thuật:**
- Backend: Java 17, Spring Boot 4.0.1, Spring Security, JPA
- Frontend: Vue.js 3, Vite, Vue Router
- Database: SQL Server
- Authentication: JWT, OAuth2 (Google Login)
- Real-time communication: WebSocket, STOMP
- Payment Integration: PayOS SDK

**Phạm vi dữ liệu:**
- Quản lý thông tin tài khoản người dùng
- Quản lý thông tin sản phẩm và danh mục
- Quản lý đơn hàng và chi tiết đơn hàng
- Quản lý giỏ hàng
- Quản lý tin nhắn chat
- Quản lý đánh giá sản phẩm
- Quản lý thông báo

**Ngoài phạm vi (không thực hiện):**
- Hệ thống quản lý kho (inventory management)
- Hệ thống CRM nâng cao
- Hệ thống Marketing automation
- Hệ thống tích hợp với các sàn TMĐT khác
- Ứng dụng mobile native (iOS, Android)

## 1.4 Đối tượng sử dụng

Hệ thống được thiết kế để phục vụ hai nhóm đối tượng chính:

**Khách hàng (Customer):**
- Người dùng cuối muốn mua sắm trực tuyến
- Có thể truy cập hệ thống qua trình duyệt web
- Yêu cầu giao diện thân thiện, dễ sử dụng
- Cần các chức năng: tìm kiếm sản phẩm, xem chi tiết, thêm vào giỏ, thanh toán, theo dõi đơn hàng
- Có thể đăng ký tài khoản để lưu thông tin và lịch sử mua hàng
- Có thể đánh giá sản phẩm đã mua

**Quản trị viên (Administrator):**
- Nhân viên quản lý của doanh nghiệp
- Có quyền truy cập vào các chức năng quản lý
- Yêu cầu giao diện quản trị chuyên nghiệp
- Cần các chức năng: quản lý sản phẩm, quản lý đơn hàng, quản lý khách hàng, xem báo cáo, chat hỗ trợ
- Có quyền phân quyền cho các vai trò khác nhau (admin, staff)
- Yêu cầu hệ thống bảo mật cao

---

# CHƯƠNG 2: CƠ SỞ LÝ THUYẾT

## 2.1 Các công nghệ sử dụng

### 2.1.1 Backend: Java và Spring Boot

**Java 17**
Java là một trong những ngôn ngữ lập trình phổ biến nhất trong phát triển ứng dụng doanh nghiệp (enterprise). Phiên bản Java 17 (LTS) được chọn cho dự án với các lý do:
- Hiệu suất cao và ổn định
- Hệ sinh thái thư viện phong phú
- Tính đa nền tảng (cross-platform)
- Hỗ trợ lập trình đa luồng (multithreading)
- Cộng đồng lớn và tài liệu phong phú
- Tính bảo mật mạnh mẽ với Security Manager

**Spring Boot 4.0.1**
Spring Boot là framework phát triển ứng dụng Java dựa trên Spring Framework, được thiết kế để đơn giản hóa quá trình khởi tạo và cấu hình ứng dụng Spring. Các ưu điểm của Spring Boot:
- Auto-configuration: Tự động cấu hình dựa trên dependencies
- Embedded server: Tích hợp Tomcat, không cần deploy server riêng
- Spring Boot Actuator: Cung cấp các endpoint monitoring
- Spring Initializr: Khởi tạo project nhanh chóng
- Hỗ trợ microservices architecture
- Tích hợp dễ dàng với các công nghệ khác (JPA, Security, WebSocket)

Các module Spring Boot sử dụng trong dự án:
- spring-boot-starter-data-jpa: Truy cập database
- spring-boot-starter-security: Xác thực và phân quyền
- spring-boot-starter-web: Xây dựng RESTful API
- spring-boot-starter-websocket: WebSocket cho real-time communication
- spring-boot-starter-oauth2-client: OAuth2 integration
- spring-boot-starter-mail: Gửi email

### 2.1.2 Frontend: Vue.js 3

**Vue.js 3**
Vue.js là một progressive JavaScript framework cho xây dựng giao diện người dùng. Phiên bản Vue 3 được chọn với các lý do:
- Composition API: Cách tổ chức code logic linh hoạt hơn
- Performance: Tối ưu hóa rendering với Virtual DOM
- Reactivity system: Reactive data binding hiệu quả
- Component-based: Tái sử dụng component dễ dàng
- Learning curve: Dễ học hơn React, Angular
- Ecosystem: Vue Router, Vuex/Pinia, Vue CLI

**Vite**
Vite là build tool mới cho frontend development, được sử dụng thay vì webpack:
- Dev server cực nhanh với ES Module native
- Hot Module Replacement (HMR) nhanh
- Build production optimized
- Configuration đơn giản
- Hỗ trợ TypeScript, JSX, CSS preprocessor

**Vue Router**
Vue Router là thư viện routing chính thức cho Vue.js:
- Dynamic routing
- Route guards (kiểm tra authentication, authorization)
- Lazy loading components
- Nested routes
- Navigation guards

**Các thư viện frontend khác:**
- SweetAlert2: Hiển thị dialog/alert đẹp
- @stomp/stompjs + sockjs-client: WebSocket client
- Axios (thông qua custom api.js): HTTP client

### 2.1.3 Database: SQL Server

**SQL Server**
SQL Server là hệ quản trị cơ sở dữ liệu quan hệ (RDBMS) của Microsoft:
- ACID compliance: Đảm bảo tính nhất quán dữ liệu
- Scalability: Hỗ trợ dữ liệu lớn
- Security: Mô hình bảo mật đa tầng
- Integration: Tích hợp tốt với .NET ecosystem
- Performance: Query optimizer hiệu quả
- Backup and Recovery: Công cụ backup mạnh mẽ

**JPA (Java Persistence API)**
JPA là specification cho mapping object-relational trong Java:
- Hibernate Implementation: JPA provider phổ biến
- Entity mapping: Mapping Java class sang database table
- JPQL: Java Persistence Query Language
- Lazy loading: Tối ưu hóa performance
- Caching: First-level và second-level cache
- Transaction management: Quản lý transaction declarative

## 2.2 Mô hình kiến trúc

### 2.2.1 Kiến trúc MVC

**Model-View-Controller (MVC)**
MVC là architectural pattern phân tách ứng dụng thành ba thành phần chính:

**Model:**
- Đại diện cho dữ liệu và business logic
- Trong dự án: JPA Entities (Account, Product, Order, v.v.)
- Không chứa logic presentation
- Tương tác với database thông qua Repository pattern

**View:**
- Đại diện cho giao diện người dùng
- Trong dự án: Vue.js components và templates
- Hiển thị data từ Model
- Nhận input từ user và chuyển cho Controller

**Controller:**
- Xử lý HTTP requests
- Gọi Service layer để xử lý business logic
- Trả về response (JSON hoặc HTML)
- Trong dự án: Spring Boot Controllers

**Ưu điểm của MVC:**
- Separation of concerns: Tách biệt trách nhiệm
- Maintainability: Dễ bảo trì và mở rộng
- Testability: Dễ test từng layer
- Reusability: Tái sử dụng component

### 2.2.2 RESTful API

**REST (Representational State Transfer)**
REST là architectural style cho thiết kế web services:

**Các nguyên tắc REST:**
- Resource-based: Mọi thứ là resource (URI)
- Stateless: Server không lưu state client
- Cacheable: Response có thể được cache
- Uniform interface: Các method HTTP thống nhất (GET, POST, PUT, DELETE)
- Layered system: Có thể có nhiều layer (load balancer, cache, v.v.)

**HTTP Methods sử dụng:**
- GET: Lấy resource
- POST: Tạo resource mới
- PUT: Cập nhật resource
- DELETE: Xóa resource
- PATCH: Cập nhật một phần resource

**Status Codes:**
- 200 OK: Thành công
- 201 Created: Tạo thành công
- 400 Bad Request: Request không hợp lệ
- 401 Unauthorized: Chưa xác thực
- 403 Forbidden: Không có quyền
- 404 Not Found: Resource không tồn tại
- 500 Internal Server Error: Lỗi server

**Ưu điểm của RESTful API:**
- Scalability: Dễ scale horizontally
- Flexibility: Client và server độc lập
- Performance: Có thể cache response
- Simplicity: Dễ hiểu và implement

### 2.2.3 Microservices Architecture

**Microservices**
Microservices là architectural style chia ứng dụng thành các service nhỏ, độc lập:

**Trong dự án:**
Dù không hoàn toàn microservices, dự án áp dụng các nguyên tắc:
- Service layer: Tách biệt business logic
- Repository layer: Tách biệt data access
- Controller layer: Tách biệt API endpoint
- Module separation: Các module (auth, product, order, cart, v.v.) độc lập

**Ưu điểm:**
- Scalability: Mỗi service có thể scale riêng
- Flexibility: Dễ thay đổi công nghệ cho từng service
- Resilience: Một service lỗi không ảnh hưởng toàn bộ hệ thống
- Deployment: Deploy từng service riêng

**Thách thức:**
- Complexity: Quản lý nhiều service phức tạp hơn
- Data consistency: Distributed transaction khó hơn
- Network latency: Communication qua network
- Monitoring: Cần monitoring distributed system

## 2.3 Các khái niệm liên quan

### 2.3.1 Authentication và Authorization

**Authentication (Xác thực)**
Xác thực là quá trình xác định danh tính của user:
- Xác nhận "ai là ai"
- Trong dự án: Username/password, OAuth2 (Google)
- Output: JWT token hoặc session

**Authorization (Phân quyền)**
Phân quyền là quá trình xác định quyền hạn của user đã xác thực:
- Xác nhận "được làm gì"
- Trong dự án: Role-based access control (ADMIN, USER)
- Kiểm tra quyền trước khi truy cập resource

**Implementation trong dự án:**
- Spring Security: Framework security
- JWT: Stateless authentication
- Role-based: Phân quyền theo role
- Method-level security: @PreAuthorize annotation

### 2.3.2 JWT (JSON Web Token)

**JWT là gì?**
JWT là compact, URL-safe means of representing claims to be transferred between two parties. Token gồm 3 phần:
- Header: Thông tin về thuật toán và type
- Payload: Claims (user info, expiry, v.v.)
- Signature: Chữ ký digital

**Cấu trúc JWT:**
```
Header.Payload.Signature
```

**Ưu điểm của JWT:**
- Stateless: Server không lưu session
- Compact: Nhỏ gọn, dễ truyền qua HTTP
- Self-contained: Chứa thông tin user
- Cross-platform: Hỗ trợ nhiều ngôn ngữ

**Trong dự án:**
- User đăng nhập → Server tạo JWT
- Client lưu JWT (localStorage)
- Client gửi JWT trong mỗi request header
- Server verify JWT và extract user info
- Token expiry: 24 giờ (access token), 7 ngày (refresh token)

### 2.3.3 OAuth2

**OAuth2 là gì?**
OAuth2 là authorization framework cho phép third-party application truy cập resource mà không cần chia sẻ credentials.

**Các role trong OAuth2:**
- Resource Owner: User sở hữu resource
- Client: Application muốn truy cập resource
- Authorization Server: Server cấp access token
- Resource Server: Server lưu resource

**OAuth2 Flow (Authorization Code):**
1. Client redirect user đến Authorization Server
2. User login và authorize
3. Authorization Server redirect về client với authorization code
4. Client exchange authorization code cho access token
5. Client sử dụng access token để truy cập resource

**Trong dự án:**
- Google OAuth2: Đăng nhập bằng Google
- Spring Security OAuth2 Client: Integration
- Redirect URI: /login/oauth2/code/google
- Scope: openid, profile, email

### 2.3.4 WebSocket

**WebSocket là gì?**
WebSocket là communication protocol cung cấp full-duplex communication channel qua single TCP connection.

**Ưu điểm so với HTTP:**
- Real-time: Gửi/nhận tin nhắn real-time
- Full-duplex: Server và client có thể gửi bất kỳ lúc nào
- Lower latency: Không cần establish connection mỗi lần gửi
- Efficient: Header nhỏ hơn HTTP

**Trong dự án:**
- Chat giữa admin và customer
- STOMP protocol: Messaging protocol trên WebSocket
- @SendTo: Broadcast message đến subscribers
- @MessageMapping: Handle incoming message

**Implementation:**
- WebSocketConfig: Cấu hình WebSocket
- ChatWsController: Controller xử lý message
- StompClient: Client-side WebSocket client
- Topic/Subscription: Publish-subscribe pattern

---

# CHƯƠNG 3: PHÂN TÍCH HỆ THỐNG

## 3.1 Mô tả nghiệp vụ

Hệ thống E-commerce hỗ trợ các nghiệp vụ chính sau:

**Nghiệp vụ Quản lý Sản phẩm:**
- Admin thêm sản phẩm mới với thông tin chi tiết
- Admin cập nhật thông tin sản phẩm (giá, mô tả, hình ảnh)
- Admin xóa sản phẩm (soft delete)
- Admin quản lý danh mục sản phẩm
- Admin quản lý size sản phẩm
- Customer tìm kiếm, lọc, sắp xếp sản phẩm
- Customer xem chi tiết sản phẩm

**Nghiệp vụ Giỏ hàng:**
- Customer thêm sản phẩm vào giỏ hàng
- Customer cập nhật số lượng trong giỏ
- Customer xóa item khỏi giỏ
- Hệ thống tính tổng tiền giỏ hàng
- Giỏ hàng lưu theo session (không cần đăng nhập)

**Nghiệp vụ Thanh toán:**
- Customer nhập thông tin giao hàng (địa chỉ, số điện thoại)
- Hệ thống tính phí giao hàng dựa trên khoảng cách
- Customer chọn phương thức thanh toán (PayOS, chuyển khoản, COD)
- Hệ thống tạo link thanh toán PayOS
- Hệ thống tạo QR code chuyển khoản
- Hệ thống xử lý callback từ PayOS

**Nghiệp vụ Quản lý Đơn hàng:**
- Hệ thống tạo đơn hàng khi thanh toán thành công
- Customer xem danh sách đơn hàng
- Customer xem chi tiết đơn hàng
- Customer hủy đơn hàng (nếu chưa giao)
- Customer yêu cầu hoàn tiền
- Admin cập nhật trạng thái đơn hàng
- Admin xem báo cáo đơn hàng

**Nghiệp vụ Chat Hỗ trợ:**
- Customer chat với admin để hỏi về sản phẩm
- Admin phản hồi tin nhắn
- Hệ thống thông báo tin nhắn mới
- Hệ thống lưu lịch sử chat

**Nghiệp vụ Đánh giá:**
- Customer đánh giá sản phẩm đã mua (1-5 sao)
- Customer viết bình luận
- Customer xem đánh giá của người khác
- Hệ thống thống kê đánh giá trung bình

**Nghiệp vụ Báo cáo:**
- Admin xem báo cáo doanh thu tổng
- Admin xem báo cáo theo thời gian (ngày, tuần, tháng, quý, năm)
- Admin xuất báo cáo ra Excel
- Admin xem danh sách khách VIP

## 3.2 Use case

### 3.2.1 Use case Đăng ký/Đăng nhập

**Use case: Đăng ký tài khoản**
- **Actor:** Khách hàng chưa có tài khoản
- **Precondition:** Chưa đăng nhập
- **Main flow:**
  1. User truy cập trang đăng ký
  2. User nhập thông tin (username, email, password, fullname, phone, address)
  3. System validate thông tin (username unique, email unique, password strong)
  4. System tạo tài khoản mới
  5. System gán role USER mặc định
  6. System redirect đến trang đăng nhập
- **Alternative flow:**
  - Username đã tồn tại: Hiển thị error message
  - Email đã tồn tại: Hiển thị error message
  - Password không đủ mạnh: Hiển thị error message
- **Postcondition:** Tài khoản mới được tạo trong database

**Use case: Đăng nhập**
- **Actor:** Khách hàng đã có tài khoản
- **Precondition:** Tài khoản đã tồn tại, chưa đăng nhập
- **Main flow:**
  1. User truy cập trang đăng nhập
  2. User nhập username và password
  3. System validate credentials
  4. System tạo JWT token
  5. System redirect đến trang chủ
- **Alternative flow:**
  - Username không tồn tại: Hiển thị error message
  - Password sai: Hiển thị error message
  - Tài khoản bị khóa: Hiển thị error message
- **Postcondition:** User được xác thực, JWT token được lưu

**Use case: Đăng nhập Google**
- **Actor:** Khách hàng có tài khoản Google
- **Precondition:** Google OAuth2 được cấu hình
- **Main flow:**
  1. User click "Đăng nhập với Google"
  2. System redirect đến Google OAuth2 page
  3. User login và authorize trên Google
  4. Google redirect về với authorization code
  5. System exchange code cho access token
  6. System lấy user info từ Google
  7. System tạo hoặc cập nhật tài khoản
  8. System tạo JWT token
  9. System redirect đến trang chủ
- **Alternative flow:**
  - User hủy authorize: Redirect về trang đăng nhập
- **Postcondition:** User được xác thực với Google account

### 3.2.2 Use case Quản lý sản phẩm

**Use case: Thêm sản phẩm mới**
- **Actor:** Admin
- **Precondition:** Admin đã đăng nhập, có role ADMIN
- **Main flow:**
  1. Admin truy cập trang quản lý sản phẩm
  2. Admin click "Thêm sản phẩm"
  3. System hiển thị form thêm sản phẩm
  4. Admin nhập thông tin sản phẩm (tên, giá, giảm giá, mô tả, danh mục, size, số lượng, ảnh)
  5. Admin upload ảnh sản phẩm
  6. System validate thông tin
  7. System lưu sản phẩm vào database
  8. System lưu ảnh vào thư mục uploads
  9. System hiển thị thông báo thành công
- **Alternative flow:**
  - Thông tin không hợp lệ: Hiển thị error message
  - Upload ảnh thất bại: Hiển thị error message
- **Postcondition:** Sản phẩm mới được tạo

**Use case: Cập nhật sản phẩm**
- **Actor:** Admin
- **Precondition:** Sản phẩm đã tồn tại
- **Main flow:**
  1. Admin chọn sản phẩm cần cập nhật
  2. System hiển thị form cập nhật với thông tin hiện tại
  3. Admin thay đổi thông tin
  4. Admin click "Cập nhật"
  5. System validate thông tin
  6. System cập nhật sản phẩm trong database
  7. System hiển thị thông báo thành công
- **Postcondition:** Sản phẩm được cập nhật

### 3.2.3 Use case Giỏ hàng và Thanh toán

**Use case: Thêm vào giỏ hàng**
- **Actor:** Khách hàng
- **Precondition:** Sản phẩm đã tồn tại
- **Main flow:**
  1. Customer xem danh sách sản phẩm hoặc chi tiết sản phẩm
  2. Customer chọn size và số lượng
  3. Customer click "Thêm vào giỏ hàng"
  4. System kiểm tra sản phẩm còn trong kho
  5. System thêm item vào giỏ hàng (session)
  6. System hiển thị thông báo thành công
  7. System cập nhật số lượng giỏ hàng trên header
- **Alternative flow:**
  - Sản phẩm hết hàng: Hiển thị error message
  - Số lượng không đủ: Hiển thị error message
- **Postcondition:** Item được thêm vào giỏ hàng

**Use case: Thanh toán**
- **Actor:** Khách hàng đã đăng nhập
- **Precondition:** Giỏ hàng có ít nhất 1 item
- **Main flow:**
  1. Customer truy cập trang giỏ hàng
  2. Customer click "Thanh toán"
  3. System redirect đến trang checkout
  4. System hiển thị thông tin giỏ hàng
  5. Customer nhập thông tin giao hàng (địa chỉ, số điện thoại)
  6. Customer chọn phương thức thanh toán
  7. System tính phí giao hàng (dựa trên khoảng cách)
  8. System hiển thị tổng tiền
  9. Customer xác nhận thanh toán
  10. Nếu chọn PayOS: System tạo link thanh toán PayOS
  11. Nếu chọn chuyển khoản: System hiển thị QR code
  12. Nếu chọn COD: System tạo đơn hàng trực tiếp
  13. System chuyển đến trang thanh toán hoặc trang đơn hàng
- **Alternative flow:**
  - Giỏ hàng trống: Redirect về trang sản phẩm
  - Thông tin giao hàng không hợp lệ: Hiển thị error message
- **Postcondition:** Đơn hàng được tạo hoặc link thanh toán được tạo

### 3.2.4 Use case Quản lý đơn hàng

**Use case: Xem danh sách đơn hàng**
- **Actor:** Khách hàng đã đăng nhập
- **Precondition:** User đã có đơn hàng
- **Main flow:**
  1. Customer truy cập trang "Đơn hàng của tôi"
  2. System hiển thị danh sách đơn hàng
  3. Customer có thể lọc theo trạng thái
  4. Customer có thể xem chi tiết từng đơn hàng
- **Postcondition:** Danh sách đơn hàng được hiển thị

**Use case: Cập nhật trạng thái đơn hàng**
- **Actor:** Admin
- **Precondition:** Đơn hàng đã tồn tại
- **Main flow:**
  1. Admin truy cập trang quản lý đơn hàng
  2. Admin chọn đơn hàng cần cập nhật
  3. Admin chọn trạng thái mới (Đang giao, Đã giao, Hủy)
  4. Admin click "Cập nhật"
  5. System cập nhật trạng thái trong database
  6. System gửi thông báo cho customer (nếu có)
  7. System hiển thị thông báo thành công
- **Postcondition:** Trạng thái đơn hàng được cập nhật

## 3.3 Phân tích chức năng

### 3.3.1 Chức năng Quản lý sản phẩm

**Mô tả:**
Chức năng quản lý sản phẩm cho phép admin thêm, sửa, xóa sản phẩm và quản lý danh mục, size sản phẩm.

**Input:**
- Thông tin sản phẩm: tên, giá, giảm giá, mô tả, danh mục, size, số lượng, ảnh
- Thông tin danh mục: tên, mô tả
- Thông tin size: tên, mô tả

**Process:**
1. Admin nhập thông tin sản phẩm
2. System validate thông tin (giá phải > 0, số lượng phải >= 0)
3. System upload ảnh vào thư mục uploads
4. System lưu thông tin vào database (table Product, Category, Size, ProductSize)
5. System trả về thông báo thành công

**Output:**
- Sản phẩm được lưu trong database
- Ảnh được lưu trong thư mục uploads
- Thông báo thành công/error

**Validation:**
- Tên sản phẩm không được trống
- Giá phải là số dương
- Số lượng phải >= 0
- Ảnh phải là file ảnh hợp lệ
- Danh mục phải tồn tại

### 3.3.2 Chức năng Giỏ hàng

**Mô tả:**
Chức năng giỏ hàng cho phép customer thêm, cập nhật, xóa sản phẩm vào giỏ hàng và tính tổng tiền.

**Input:**
- Product ID
- Size ID
- Số lượng

**Process:**
1. Customer click "Thêm vào giỏ hàng"
2. System kiểm tra sản phẩm còn trong kho
3. System thêm item vào session (CartService)
4. System tính tổng tiền
5. System cập nhật số lượng giỏ hàng trên UI

**Output:**
- Item được thêm vào session
- Tổng tiền được tính
- Số lượng giỏ hàng được cập nhật

**Validation:**
- Sản phẩm phải tồn tại
- Số lượng phải >= 1
- Số lượng trong kho phải đủ

### 3.3.3 Chức năng Thanh toán

**Mô tả:**
Chức năng thanh toán tích hợp PayOS, chuyển khoản ngân hàng, và COD.

**Input:**
- Thông tin giao hàng: địa chỉ, số điện thoại
- Phương thức thanh toán
- Items giỏ hàng

**Process:**
1. Customer nhập thông tin giao hàng
2. System geocode địa chỉ để lấy tọa độ
3. System tính khoảng cách từ kho đến địa chỉ giao hàng
4. System tính phí giao hàng
5. Customer chọn phương thức thanh toán
6. Nếu PayOS: System gọi PayOS API để tạo payment link
7. Nếu chuyển khoản: System tạo QR code
8. Nếu COD: System tạo đơn hàng trực tiếp
9. System lưu đơn hàng vào database

**Output:**
- Payment link (PayOS)
- QR code (chuyển khoản)
- Đơn hàng (COD)

**Validation:**
- Địa chỉ không được trống
- Số điện thoại phải hợp lệ
- Phương thức thanh toán phải được chọn

### 3.3.4 Chức năng Quản lý đơn hàng

**Mô tả:**
Chức năng quản lý đơn hàng cho phép customer xem danh sách đơn hàng, chi tiết đơn hàng, hủy đơn hàng. Admin có thể cập nhật trạng thái đơn hàng.

**Input:**
- Order ID
- Trạng thái mới (cho admin)

**Process:**
1. Customer truy cập trang đơn hàng
2. System hiển thị danh sách đơn hàng của customer
3. Customer click vào đơn hàng để xem chi tiết
4. Nếu muốn hủy: Customer click "Hủy đơn hàng"
5. System kiểm tra trạng thái đơn hàng (chỉ hủy được nếu chưa giao)
6. System cập nhật trạng thái thành CANCELLED
7. Admin có thể cập nhật trạng thái đơn hàng

**Output:**
- Danh sách đơn hàng
- Chi tiết đơn hàng
- Thông báo hủy thành công

**Validation:**
- Đơn hàng phải thuộc về customer
- Chỉ hủy được nếu trạng thái phù hợp
- Admin phải có quyền cập nhật

### 3.3.5 Chức năng Chat hỗ trợ

**Mô tả:**
Chức năng chat cho phép customer và admin giao tiếp real-time để hỏi về sản phẩm.

**Input:**
- Tin nhắn văn bản
- Ảnh (optional)

**Process:**
1. Customer mở chat box
2. Customer gửi tin nhắn
3. Message được gửi qua WebSocket
4. Admin nhận tin nhắn real-time
5. Admin phản hồi
6. Customer nhận phản hồi real-time
7. System lưu lịch sử chat trong database

**Output:**
- Tin nhắn được gửi và nhận real-time
- Lịch sử chat được lưu

**Validation:**
- Tin nhắn không được trống
- Ảnh phải hợp lệ (nếu có)

## 3.4 Phân tích dữ liệu

**Các entity chính:**

**Account:**
- Lưu thông tin tài khoản người dùng
- Quan hệ: One-to-many với Order, CartItem, ChatMessage, ProductReview
- Fields: username, password, email, fullname, phone, address, photo, activated, isDelete, accountType

**Product:**
- Lưu thông tin sản phẩm
- Quan hệ: Many-to-one với Category, One-to-many với ProductSize, OrderDetail
- Fields: name, price, discount, quantity, image, description, category_id, isDelete, createDate

**Order:**
- Lưu thông tin đơn hàng
- Quan hệ: Many-to-one với Account, One-to-many với OrderDetail
- Fields: account_id, address, totalAmount, status, createDate, deliveryDistanceM, expectedDeliveryDate

**OrderDetail:**
- Lưu chi tiết từng sản phẩm trong đơn hàng
- Quan hệ: Many-to-one với Order, Product
- Fields: order_id, product_id, sizeId, sizeName, price, quantity

**CartItem:**
- Lưu sản phẩm trong giỏ hàng
- Quan hệ: Many-to-one với Account, Product
- Fields: account_id, product_id, sizeId, sizeName, quantity

**ChatMessage:**
- Lưu tin nhắn chat
- Quan hệ: Many-to-one với Account (sender, receiver)
- Fields: sender_id, receiver_id, message, imageUrl, isRead, createTime

**ProductReview:**
- Lưu đánh giá sản phẩm
- Quan hệ: Many-to-one với Product, Account
- Fields: product_id, account_id, rating, comment, createDate

**Các quan hệ chính:**
- Account (1) → (n) Order
- Account (1) → (n) CartItem
- Product (1) → (n) OrderDetail
- Category (1) → (n) Product
- Product (n) ↔ (n) Size (through ProductSize)
- Account (1) → (n) ChatMessage (sender)
- Account (1) → (n) ChatMessage (receiver)
- Product (1) → (n) ProductReview

---

# CHƯƠNG 4: THIẾT KẾ HỆ THỐNG

## 4.1 Thiết kế kiến trúc tổng thể

Hệ thống được thiết kế theo kiến trúc Client-Server với tách biệt giữa frontend và backend:

**Frontend (Vue.js):**
- Chạy trên port 5173 (Vite dev server)
- Giao tiếp với backend qua RESTful API
- State management: Reactive với Vue 3 Composition API
- Routing: Vue Router
- Communication: Axios HTTP client

**Backend (Spring Boot):**
- Chạy trên port 8080 (Tomcat embedded)
- Cung cấp RESTful API endpoints
- Xử lý business logic
- Tương tác với database (SQL Server)
- Xác thực và phân quyền (JWT, OAuth2)

**Database (SQL Server):**
- Chạy trên port 1433
- Lưu dữ liệu persistent
- JPA/Hibernate ORM mapping

**Communication:**
- HTTP/HTTPS cho REST API
- WebSocket cho real-time chat
- JWT cho authentication
- OAuth2 cho Google login

**Kiến trúc layer:**
- Controller Layer: Xử lý HTTP requests
- Service Layer: Business logic
- Repository Layer: Data access
- Entity Layer: Data model

## 4.2 Thiết kế database

### 4.2.1 Entity Account

**Table: accounts**

| Column | Type | Description |
|--------|------|-------------|
| id | BIGINT (PK) | Primary key |
| username | VARCHAR (unique) | Tên đăng nhập |
| password | VARCHAR | Mật khẩu (hashed) |
| email | VARCHAR (unique) | Email |
| fullname | VARCHAR | Họ tên đầy đủ |
| phone | VARCHAR | Số điện thoại |
| address | VARCHAR | Địa chỉ |
| photo | VARCHAR | Ảnh avatar |
| activated | BOOLEAN | Trạng thái kích hoạt |
| isDelete | BOOLEAN | Soft delete |
| accountType | VARCHAR | NORMAL/GOOGLE |

**Giải thích:**
- username và email phải unique để tránh trùng lặp
- password được hash với BCrypt
- isDelete cho soft delete, không xóa thật khỏi database
- accountType phân biệt tài khoản thường và tài khoản Google

### 4.2.2 Entity Product

**Table: products**

| Column | Type | Description |
|--------|------|-------------|
| id | BIGINT (PK) | Primary key |
| name | VARCHAR | Tên sản phẩm |
| price | DECIMAL | Giá gốc |
| discount | DECIMAL | Giảm giá |
| quantity | INT | Tổng số lượng |
| image | VARCHAR | Ảnh sản phẩm |
| description | TEXT | Mô tả |
| category_id | BIGINT (FK) | FK → categories |
| isDelete | BOOLEAN | Soft delete |
| createDate | DATETIME | Ngày tạo |

**Table: categories**

| Column | Type | Description |
|--------|------|-------------|
| id | BIGINT (PK) | Primary key |
| name | VARCHAR (unique) | Tên danh mục |
| description | TEXT | Mô tả |

**Table: sizes**

| Column | Type | Description |
|--------|------|-------------|
| id | BIGINT (PK) | Primary key |
| name | VARCHAR (unique) | Tên size (S, M, L, XL) |
| description | TEXT | Mô tả |

**Table: product_sizes**

| Column | Type | Description |
|--------|------|-------------|
| id | BIGINT (PK) | Primary key |
| product_id | BIGINT (FK) | FK → products |
| size_id | BIGINT (FK) | FK → sizes |
| quantity | INT | Số lượng theo size |

**Giải thích:**
- Product có thể thuộc nhiều size (many-to-many)
- quantity trong product_sizes là số lượng theo từng size
- quantity trong products là tổng số lượng tất cả size
- isDelete cho soft delete sản phẩm

### 4.2.3 Entity Order

**Table: orders**

| Column | Type | Description |
|--------|------|-------------|
| id | BIGINT (PK) | Primary key |
| account_id | BIGINT (FK) | FK → accounts |
| address | VARCHAR | Địa chỉ giao hàng |
| totalAmount | DECIMAL | Tổng tiền |
| status | VARCHAR | Trạng thái đơn hàng |
| createDate | DATETIME | Ngày tạo |
| deliveryDistanceM | BIGINT | Khoảng cách (mét) |
| expectedDeliveryDate | DATETIME | Ngày giao dự kiến |
| pay_checkout_url | VARCHAR | PayOS checkout URL |
| pay_qr_code | TEXT | QR code |
| pay_account_name | VARCHAR | Tên tài khoản ngân hàng |
| pay_account_number | VARCHAR | Số tài khoản |
| pay_bank_bin | VARCHAR | Mã ngân hàng |
| pay_payment_link_id | VARCHAR | PayOS payment link ID |

**Table: order_details**

| Column | Type | Description |
|--------|------|-------------|
| id | BIGINT (PK) | Primary key |
| order_id | BIGINT (FK) | FK → orders |
| product_id | BIGINT (FK) | FK → products |
| sizeId | BIGINT | ID size |
| sizeName | VARCHAR | Tên size |
| price | DECIMAL | Giá tại thời điểm mua |
| quantity | INT | Số lượng |

**Giải thích:**
- status có các giá trị: PENDING_PAYMENT, PLACED_PAID, SHIPPING_PAID, DELIVERED, CANCELLED
- Các trường pay_* lưu thông tin thanh toán PayOS
- price trong order_details là giá snapshot tại thời điểm mua (để tránh thay đổi giá sau này ảnh hưởng đơn hàng cũ)

### 4.2.4 Các entity khác

**Table: cart_items**

| Column | Type | Description |
|--------|------|-------------|
| id | BIGINT (PK) | Primary key |
| account_id | BIGINT (FK) | FK → accounts |
| product_id | BIGINT (FK) | FK → products |
| sizeId | BIGINT | ID size |
| sizeName | VARCHAR | Tên size |
| quantity | INT | Số lượng |

**Table: chat_messages**

| Column | Type | Description |
|--------|------|-------------|
| id | BIGINT (PK) | Primary key |
| sender_id | BIGINT (FK) | FK → accounts (sender) |
| receiver_id | BIGINT (FK) | FK → accounts (receiver) |
| message | TEXT | Nội dung tin nhắn |
| imageUrl | VARCHAR | Ảnh tin nhắn |
| isRead | BOOLEAN | Đã đọc |
| createTime | DATETIME | Thời gian gửi |

**Table: product_reviews**

| Column | Type | Description |
|--------|------|-------------|
| id | BIGINT (PK) | Primary key |
| product_id | BIGINT (FK) | FK → products |
| account_id | BIGINT (FK) | FK → accounts |
| rating | INT | Đánh giá 1-5 |
| comment | TEXT | Bình luận |
| createDate | DATETIME | Ngày đánh giá |

**Table: roles**

| Column | Type | Description |
|--------|------|-------------|
| id | VARCHAR (PK) | Role ID (ADMIN, USER) |
| name | VARCHAR | Tên role |
| description | TEXT | Mô tả |

**Table: authorities**

| Column | Type | Description |
|--------|------|-------------|
| id | BIGINT (PK) | Primary key |
| account_id | BIGINT (FK) | FK → accounts |
| role_id | VARCHAR (FK) | FK → roles |

**Giải thích:**
- cart_items lưu giỏ hàng theo session
- chat_messages lưu tin nhắn WebSocket
- product_reviews lưu đánh giá sản phẩm
- roles và authorities implement role-based access control

## 4.3 Thiết kế giao diện (UI/UX)

**Giao diện Customer:**
- Trang chủ (Home): Hiển thị banner, danh sách sản phẩm, filter, search
- Danh sách sản phẩm: Grid layout, card product với ảnh, tên, giá
- Chi tiết sản phẩm: Ảnh lớn, thông tin chi tiết, size selector, nút thêm vào giỏ
- Giỏ hàng: Table list items, tổng tiền, nút thanh toán
- Checkout: Form nhập địa chỉ, chọn phương thức thanh toán, bản đồ
- Đơn hàng: List đơn hàng với trạng thái, nút xem chi tiết
- Chat: Chat box ở góc màn hình, mở khi click icon

**Giao diện Admin:**
- Dashboard: Tổng quan đơn hàng, doanh thu
- Quản lý sản phẩm: Table CRUD, modal thêm/sửa
- Quản lý đơn hàng: Table với status filter, nút cập nhật trạng thái
- Quản lý khách hàng: Table danh sách khách, thông tin chi tiết
- Báo cáo doanh thu: Chart biểu, filter theo thời gian, nút export Excel
- Chat admin: List các hội thoại, chat box

**UX Principles:**
- Responsive design: Hỗ trợ mobile, tablet, desktop
- Consistent styling: Sử dụng Bootstrap cho consistency
- Feedback: Hiển thị loading, success/error message
- Validation: Validate input trước khi submit
- Accessibility: Semantic HTML, ARIA labels

## 4.4 Thiết kế API

**RESTful API Design:**

**Base URL:** http://localhost:8080/api

**Authentication:**
- Header: Authorization: Bearer {jwt_token}
- Public endpoints: Không cần token
- Protected endpoints: Cần JWT token

**Response Format:**
```json
{
  "success": true/false,
  "message": "message",
  "data": { ... }
}
```

**Error Response:**
```json
{
  "success": false,
  "message": "error message",
  "data": null
}
```

**Các endpoint chính:**

**Authentication:**
- POST /auth/login - Đăng nhập
- POST /auth/signup - Đăng ký
- GET /auth/me - Lấy thông tin user
- POST /auth/reset-password - Reset mật khẩu

**Products:**
- GET /products - Lấy danh sách sản phẩm (page, filter)
- GET /products/{id} - Lấy chi tiết sản phẩm
- POST /admin/products - Tạo sản phẩm (Admin)
- PUT /admin/products/{id} - Cập nhật sản phẩm (Admin)
- DELETE /admin/products/{id} - Xóa sản phẩm (Admin)

**Cart:**
- GET /cart - Lấy giỏ hàng
- POST /cart - Thêm vào giỏ
- PUT /cart/{id} - Cập nhật số lượng
- DELETE /cart/{id} - Xóa item

**Orders:**
- GET /order-workflow/checkout - Lấy dữ liệu checkout
- POST /order-workflow/checkout - Tạo đơn hàng
- GET /orders - Lấy danh sách đơn hàng
- GET /orders/{id} - Lấy chi tiết đơn hàng
- POST /orders/bank-transfer - Tạo link thanh toán
- POST /orders/{id}/cancel - Hủy đơn hàng

**Admin:**
- GET /admin/accounts - Quản lý tài khoản
- GET /admin/products - Quản lý sản phẩm
- GET /admin/orders - Quản lý đơn hàng
- GET /admin/revenue - Báo cáo doanh thu

---

# CHƯƠNG 5: TRIỂN KHAI HỆ THỐNG

## 5.1 Mô tả code chính

**Main Application Class:**
```java
@SpringBootApplication
public class AsignmentJava6Application {
    public static void main(String[] args) {
        SpringApplication.run(AsignmentJava6Application.class, args);
    }
}
```

**Security Configuration:**
```java
@Configuration
@EnableWebSecurity
@EnableMethodSecurity
public class SecurityConfig {
    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        http
            .csrf(csrf -> csrf.disable())
            .sessionManagement(session -> 
                session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
            .authorizeHttpRequests(auth -> auth
                .requestMatchers("/api/auth/**").permitAll()
                .requestMatchers("/oauth2/**").permitAll()
                .requestMatchers("/api/admin/**").hasRole("ADMIN")
                .anyRequest().authenticated()
            )
            .oauth2Login(oauth2 -> {
                // OAuth2 configuration
            })
            .addFilterBefore(jwtAuthenticationFilter, 
                UsernamePasswordAuthenticationFilter.class);
        return http.build();
    }
}
```

**JWT Authentication Filter:**
```java
public class JwtAuthenticationFilter extends OncePerRequestFilter {
    @Override
    protected void doFilterInternal(HttpServletRequest request, 
                                    HttpServletResponse response, 
                                    FilterChain filterChain) {
        String token = extractToken(request);
        if (token != null && jwtService.validateToken(token)) {
            String username = jwtService.extractUsername(token);
            UserDetails userDetails = userDetailsService.loadUserByUsername(username);
            UsernamePasswordAuthenticationToken authToken = 
                new UsernamePasswordAuthenticationToken(userDetails, null, 
                    userDetails.getAuthorities());
            SecurityContextHolder.getContext().setAuthentication(authToken);
        }
        filterChain.doFilter(request, response);
    }
}
```

**Service Layer Example:**
```java
@Service
public class ProductServiceImpl implements ProductService {
    @Autowired
    private ProductRepository productRepository;
    
    @Override
    public Product create(Product product) {
        // Business logic
        if (product.getPrice() <= 0) {
            throw new IllegalArgumentException("Giá phải lớn hơn 0");
        }
        return productRepository.save(product);
    }
}
```

**Repository Layer:**
```java
@Repository
public interface ProductRepository extends JpaRepository<Product, Long> {
    List<Product> findByCategoryId(Long categoryId);
    List<Product> findByNameContainingIgnoreCase(String name);
    @Query("SELECT p FROM Product p WHERE p.isDelete = false")
    List<Product> findAllActive();
}
```

## 5.2 Giải thích các module quan trọng

### 5.2.1 Module Authentication

**Components:**
- AuthService: Xử lý logic đăng ký, đăng nhập
- AuthProviderService: Xử lý OAuth2 (Google)
- PasswordResetService: Xử lý reset mật khẩu
- JWT Service: Tạo và validate JWT token
- Security Config: Cấu hình Spring Security

**Flow:**
1. User đăng nhập với username/password
2. AuthService verify credentials
3. Nếu đúng, tạo JWT token
4. Token được trả về cho client
5. Client lưu token và gửi trong mỗi request header
6. JWT Filter validate token trước khi truy cập protected resource

**Code key:**
```java
@Service
public class AuthServiceImpl implements AuthService {
    @Override
    public String login(String username, String password) {
        Authentication authentication = authenticationManager.authenticate(
            new UsernamePasswordAuthenticationToken(username, password)
        );
        UserDetails userDetails = userDetailsService.loadUserByUsername(username);
        return jwtService.generateToken(userDetails);
    }
}
```

### 5.2.2 Module Payment

**Components:**
- PayosPaymentService: Tích hợp PayOS SDK
- OrderController: Xử lý logic thanh toán
- Payos Config: Cấu hình PayOS keys

**Flow:**
1. Customer chọn thanh toán PayOS
2. OrderController tạo order với status PENDING_PAYMENT
3. PayosPaymentService gọi PayOS API để tạo payment link
4. PayOS trả về checkout URL và QR code
5. Customer được redirect đến PayOS
6. Sau khi thanh toán, PayOS callback về hệ thống
7. System cập nhật order status thành PLACED_PAID

**Code key:**
```java
@Service
public class PayosPaymentServiceImpl implements PayosPaymentService {
    @Override
    public CreatePaymentLinkResponse createPaymentLink(Long orderId, 
                                                        Long amount, 
                                                        String orderCode, 
                                                        String returnUrl, 
                                                        String cancelUrl) {
        CreatePaymentLinkRequest request = new CreatePaymentLinkRequest();
        request.setOrderCode(orderCode);
        request.setAmount(amount);
        request.setDescription(orderCode);
        request.setCancelUrl(cancelUrl);
        request.setReturnUrl(returnUrl);
        return payOSClient.createPaymentLink(request);
    }
}
```

### 5.2.3 Module Chat

**Components:**
- ChatWsController: WebSocket controller
- ChatService: Business logic chat
- WebSocketConfig: Cấu hình WebSocket

**Flow:**
1. Customer mở chat box
2. WebSocket connection được established
3. Customer gửi tin nhắn
4. Message được broadcast qua WebSocket topic
5. Admin nhận tin nhắn real-time
6. Admin phản hồi
7. Customer nhận phản hồi real-time

**Code key:**
```java
@Controller
public class ChatWsController {
    @MessageMapping("/chat/send")
    @SendTo("/topic/messages")
    public ChatMessage sendMessage(ChatMessage message) {
        message.setCreateTime(new Date());
        chatService.saveMessage(message);
        return message;
    }
}
```

## 5.3 Luồng xử lý dữ liệu

**Luồng Đăng ký:**
1. User nhập thông tin đăng ký
2. Frontend validate input
3. Frontend gọi POST /api/auth/signup
4. AuthService validate (username unique, email unique)
5. Password được hash với BCrypt
6. Account entity được tạo
7. Authority entity được tạo (role USER)
8. Data được lưu vào database
9. Response success được trả về

**Luồng Thanh toán PayOS:**
1. Customer checkout với PayOS
2. Frontend gửi thông tin đơn hàng
3. OrderController tạo order với status PENDING_PAYMENT
4. PayosPaymentService gọi PayOS API
5. PayOS trả về checkout URL
6. Customer redirect đến PayOS
7. Customer thanh toán trên PayOS
8. PayOS callback về /api/orders/payos-return
9. OrderController verify signature
10. Order status được cập nhật thành PLACED_PAID
11. Customer được redirect về trang đơn hàng

**Luồng Chat WebSocket:**
1. Customer mở chat box
2. Frontend establish WebSocket connection
3. Customer subscribe topic "/topic/messages"
4. Customer gửi tin nhắn qua WebSocket
5. ChatWsController nhận message
6. Message được lưu vào database
7. Message được broadcast đến topic
8. Admin (đã subscribe) nhận tin nhắn
9. Admin gửi phản hồi
10. Customer nhận phản hồi

---

# CHƯƠNG 6: KIỂM THỬ VÀ ĐÁNH GIÁ

## 6.1 Test case

### Test Case 1: Đăng ký tài khoản

**Mô tả:** Kiểm tra chức năng đăng ký tài khoản mới

**Input:**
- Username: "testuser"
- Email: "test@example.com"
- Password: "Test@123"
- Fullname: "Test User"
- Phone: "0123456789"
- Address: "123 Test Street"

**Expected Result:** Tài khoản được tạo thành công, redirect đến trang đăng nhập

**Actual Result:** Tài khoản được tạo thành công

**Status:** Pass

---

### Test Case 2: Đăng nhập

**Mô tả:** Kiểm tra chức năng đăng nhập

**Input:**
- Username: "testuser"
- Password: "Test@123"

**Expected Result:** Đăng nhập thành công, JWT token được trả về

**Actual Result:** Đăng nhập thành công, JWT token được trả về

**Status:** Pass

---

### Test Case 3: Thêm sản phẩm vào giỏ hàng

**Mô tả:** Kiểm tra chức năng thêm sản phẩm vào giỏ hàng

**Input:**
- Product ID: 1
- Size ID: 1
- Quantity: 2

**Expected Result:** Sản phẩm được thêm vào giỏ hàng, tổng tiền được cập nhật

**Actual Result:** Sản phẩm được thêm vào giỏ hàng, tổng tiền được cập nhật

**Status:** Pass

---

### Test Case 4: Thanh toán PayOS

**Mô tả:** Kiểm tra chức năng thanh toán PayOS

**Input:**
- Items: 1 sản phẩm
- Địa chỉ: "123 Test Street, HCM"
- Phương thức: PayOS

**Expected Result:** Payment link được tạo, redirect đến PayOS

**Actual Result:** Payment link được tạo, redirect đến PayOS

**Status:** Pass

---

### Test Case 5: Chat WebSocket

**Mô tả:** Kiểm tra chức năng chat real-time

**Input:**
- Tin nhắn: "Xin chào"

**Expected Result:** Tin nhắn được gửi và nhận real-time

**Actual Result:** Tin nhắn được gửi và nhận real-time

**Status:** Pass

## 6.2 Kết quả đạt được

**Functional Requirements:**
- ✅ Quản lý sản phẩm hoàn chỉnh
- ✅ Giỏ hàng hoạt động đúng
- ✅ Thanh toán PayOS tích hợp thành công
- ✅ Quản lý đơn hàng đầy đủ
- ✅ Chat real-time hoạt động
- ✅ Đánh giá sản phẩm hoạt động
- ✅ Báo cáo doanh thu chính xác
- ✅ Authentication JWT hoạt động
- ✅ OAuth2 Google login hoạt động

**Non-functional Requirements:**
- ✅ Response time < 2s cho API
- ✅ UI responsive trên mobile
- ✅ Bảo mật với JWT và OAuth2
- ✅ Database consistency với JPA transactions
- ✅ Code structure theo MVC pattern

## 6.3 Ưu điểm

**Kiến trúc:**
- Tách biệt frontend và backend với RESTful API
- Kiến trúc MVC rõ ràng
- Code organization tốt theo package
- Sử dụng design patterns (Repository, Service, DTO)

**Công nghệ:**
- Sử dụng công nghệ hiện đại và phổ biến
- Spring Boot đơn giản hóa cấu hình
- Vue.js 3 với Composition API linh hoạt
- JPA/Hibernate đơn giản hóa database access

**Chức năng:**
- Tích hợp PayOS thanh toán điện tử
- Chat real-time với WebSocket
- OAuth2 Google login
- Báo cáo doanh thu chi tiết
- Soft delete entities

**Code Quality:**
- Code clean và dễ đọc
- Naming convention nhất quán
- Comment và documentation đầy đủ
- Error handling tốt

## 6.4 Nhược điểm

**Testing:**
- Thiếu unit tests
- Thiếu integration tests
- Không có automated testing

**Performance:**
- Không có caching layer (Redis)
- Không có CDN cho static assets
- Database queries chưa optimize (N+1 problem)

**Security:**
- JWT token không có refresh token rotation
- Không có rate limiting cho API
- Không có input validation toàn diện

**Scalability:**
- Không có load balancing
- Không có database clustering
- Không có message queue cho async processing

**UI/UX:**
- Giao diện admin chưa tối ưu
- Thiếu loading state cho nhiều action
- Không có offline support

---

# CHƯƠNG 7: KẾT LUẬN VÀ HƯỚNG PHÁT TRIỂN

## 7.1 Tổng kết

Dự án "Xây dựng hệ thống E-commerce trực tuyến" đã được triển khai thành công với đầy đủ các chức năng cơ bản của một sàn thương mại điện tử hiện đại. Hệ thống tích hợp các công nghệ tiên tiến như Spring Boot, Vue.js 3, SQL Server, PayOS, WebSocket, và OAuth2, đáp ứng các yêu cầu về chức năng và phi chức năng đã đề ra ban đầu.

Về mặt kiến trúc, hệ thống áp dụng kiến trúc MVC với tách biệt rõ ràng giữa frontend và backend, sử dụng RESTful API để giao tiếp. Code organization tốt theo package, tuân thủ các nguyên tắc thiết kế phần mềm như SOLID, DRY, KISS.

Về mặt chức năng, hệ thống cung cấp đầy đủ các tính năng cần thiết cho một sàn thương mại điện tử: quản lý sản phẩm, giỏ hàng, thanh toán đa phương thức, quản lý đơn hàng, chat hỗ trợ, đánh giá sản phẩm, và báo cáo doanh thu.

Về mặt kỹ thuật, hệ thống đạt được các mục tiêu về bảo mật (JWT, OAuth2), hiệu suất (response time chấp nhận được), và khả năng mở rộng (kiến trúc microservices-ready).

Tuy nhiên, dự án vẫn còn một số hạn chế cần cải thiện trong tương lai như đã nêu ở phần 6.4.

## 7.2 Hướng phát triển

**Ngắn hạn (trong 6 tháng):**

1. **Testing:**
   - Viết unit tests cho Service layer với JUnit 5
   - Viết integration tests cho API với TestContainers
   - Thiết lập CI/CD pipeline với GitHub Actions

2. **Performance Optimization:**
   - Thêm Redis caching layer
   - Optimize database queries với JOIN FETCH
   - Thêm CDN cho static assets
   - Implement lazy loading cho images

3. **Security Enhancement:**
   - Thêm rate limiting với Spring Security
   - Implement refresh token rotation
   - Thêm CSRF protection cho state-changing operations
   - Implement input validation toàn diện

4. **UI/UX Improvement:**
   - Tối ưu giao diện admin dashboard
   - Thêm loading states cho tất cả async operations
   - Implement offline support với Service Worker
   - Thêm dark mode

**Trung hạn (trong 1 năm):**

1. **Advanced Features:**
   - Hệ thống quản lý kho (inventory management)
   - Hệ thống CRM nâng cao
   - Marketing automation (email campaigns, discounts)
   - Recommendation system (gợi ý sản phẩm)

2. **Mobile Application:**
   - Phát triển mobile app với React Native hoặc Flutter
   - Push notifications
   - Offline-first architecture

3. **Analytics:**
   - Integration với Google Analytics
   - A/B testing framework
   - Heatmap và user behavior tracking

4. **Scalability:**
   - Implement load balancing với Nginx
   - Database clustering với SQL Server AlwaysOn
   - Message queue với RabbitMQ hoặc Kafka
   - Containerization với Docker và Kubernetes

**Dài hạn (trong 2 năm):**

1. **Microservices Architecture:**
   - Chia hệ thống thành microservices độc lập
   - Service mesh với Istio
   - Distributed tracing với Jaeger
   - Centralized logging với ELK stack

2. **AI/ML Integration:**
   - Chatbot AI với NLP
   - Product recommendation với Machine Learning
   - Price optimization
   - Fraud detection

3. **Marketplace Integration:**
   - Multi-vendor marketplace
   - Integration với các sàn TMĐT khác (Shopee, Lazada)
   - Dropshipping support

4. **International Expansion:**
   - Multi-language support (i18n)
   - Multi-currency support
   - Local payment methods for different countries

---

**TỔNG KẾT:**
Dự án đã hoàn thành thành công mục tiêu xây dựng một hệ thống e-commerce hoàn chỉnh với các tính năng cơ bản và kiến trúc hiện đại. Dù còn một số hạn chế, hệ thống đã sẵn sàng để sử dụng trong thực tế và có tiềm năng phát triển lớn trong tương lai. Các hướng phát triển đã được đề ra sẽ giúp nâng cao hệ thống về mặt chức năng, hiệu suất, bảo mật, và khả năng mở rộng.

---

**Người thực hiện:** [Tên sinh viên]
**Mã sinh viên:** [Mã sinh viên]
**Lớp:** [Lớp]
**Giảng viên hướng dẫn:** [Tên giảng viên]
**Ngày hoàn thành:** 21/04/2026
