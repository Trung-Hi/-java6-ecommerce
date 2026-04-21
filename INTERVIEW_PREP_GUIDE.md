# HƯỚNG DẪN ÔN THI PHỎNG VẤN BACKEND INTERN
## Huỳnh Đoàn Trung Hiếu - E-Commerce & LMS Project

---

# PHẦN 1: HỆ THỐNG KIẾN THỨC "SÁT SƯỜN"

## 1.1 Luồng Dữ Liệu (Data Flow) trong E-Commerce

### Architecture Overview
```
VueJS Frontend → REST API → Spring Boot Backend → JPA/Hibernate → SQL Server
```

### Chi Tiết Từng Tầng:

#### **Frontend (VueJS)**
- **Axios/Fetch** gọi HTTP Request đến Backend
- **Vuex/Pinia** quản lý state (Giỏ hàng, User session)
- **Interceptor** thêm JWT Token vào Header

#### **Controller Layer**
```java
@RestController
@RequestMapping("/api/orders")
public class OrderController {
    
    @PostMapping
    public ResponseEntity<OrderResponse> createOrder(
            @Valid @RequestBody OrderRequestDTO request,
            @AuthenticationPrincipal UserPrincipal user) {
        // 1. Validate input (DTO pattern)
        // 2. Delegate to Service
        OrderResponse response = orderService.createOrder(request, user.getId());
        // 3. Return ResponseEntity with HTTP 201 CREATED
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }
}
```

#### **Service Layer (Business Logic)**
```java
@Service
@Transactional(rollbackFor = Exception.class)
public class OrderService {
    
    public OrderResponse createOrder(OrderRequest request, Long userId) {
        // 1. Retrieve Cart with Pessimistic Locking
        Cart cart = cartRepository.findByUserIdForUpdate(userId);
        
        // 2. Validate Stock Availability
        for (CartItem item : cart.getItems()) {
            Product product = productRepo.findByIdWithLock(item.getProductId());
            if (product.getStock() < item.getQuantity()) {
                throw new InsufficientStockException("Product out of stock");
            }
        }
        
        // 3. Create Order Entity
        Order order = new Order();
        order.setUser(userRepository.findById(userId));
        order.setStatus(OrderStatus.PENDING);
        order.setTotalAmount(calculateTotal(cart));
        
        // 4. Convert CartItems to OrderDetails (Snapshot pricing)
        List<OrderDetail> details = cart.getItems().stream()
            .map(item -> new OrderDetail(
                order,
                item.getProduct(),
                item.getQuantity(),
                item.getProduct().getPrice() // Snapshot price
            )).collect(Collectors.toList());
        
        order.setOrderDetails(details);
        
        // 5. Save Order (Cascade to OrderDetails)
        Order savedOrder = orderRepository.save(order);
        
        // 6. Decrease Stock
        for (CartItem item : cart.getItems()) {
            productRepo.decreaseStock(item.getProductId(), item.getQuantity());
        }
        
        // 7. Clear Cart
        cartRepository.deleteById(cart.getId());
        
        // 8. Call Payment Gateway (External Service)
        PaymentResult payment = paymentService.process(savedOrder);
        
        if (!payment.isSuccess()) {
            throw new PaymentFailedException("Payment processing failed");
        }
        
        savedOrder.setPaymentStatus(PaymentStatus.PAID);
        return orderMapper.toResponse(savedOrder);
    }
}
```

#### **Repository Layer**
```java
@Repository
public interface OrderRepository extends JpaRepository<Order, Long> {
    
    @Query("SELECT o FROM Order o LEFT JOIN FETCH o.orderDetails WHERE o.id = :id")
    Optional<Order> findByIdWithDetails(@Param("id") Long id);
    
    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("SELECT p FROM Product p WHERE p.id = :id")
    Optional<Product> findByIdWithLock(@Param("id") Long id);
}
```

---

## 1.2 Giải Thích Các Khái Niệm Core

### **@Transactional**

**Định nghĩa:** Annotation đánh dấu một method/class cần được thực thi trong Transaction.

**Cách hoạt động:**
- Spring tạo Proxy (AOP) bao quanh method
- Bắt đầu Transaction trước khi method chạy
- Nếu method throw Exception → Rollback toàn bộ thay đổi
- Nếu method thành công → Commit Transaction

**Các thuộc tính quan trọng:**

| Thuộc tính | Giá trị | Ý nghĩa |
|-----------|---------|---------|
| `propagation` | `REQUIRED` (default) | Dùng Transaction hiện tại hoặc tạo mới |
| `propagation` | `REQUIRES_NEW` | Luôn tạo Transaction mới, suspend cái cũ |
| `isolation` | `READ_COMMITTED` | Chỉ đọc dữ liệu đã commit |
| `isolation` | `SERIALIZABLE` | Lock toàn bộ, an toàn nhất nhưng chậm nhất |
| `rollbackFor` | `Exception.class` | Rollback khi gặp Exception cụ thể |
| `noRollbackFor` | `IllegalArgumentException.class` | Không rollback với Exception này |

**Ví dụ thực tế:**
```java
@Transactional(rollbackFor = PaymentException.class, propagation = Propagation.REQUIRED)
public void processOrder() {
    // Nếu bước này lỗi → Rollback toàn bộ
    orderRepo.save(order);      // INSERT Order
    paymentService.charge();    // Nếu fail → Order không được lưu
    inventoryService.deduct();  // Nếu fail → Payment được hoàn lại
}
```

---

### **Spring Security Filter Chain**

**Định nghĩa:** Chuỗi các Filter xử lý request theo thứ tự trước khi đến Controller.

**Luồng đi:**
```
Client Request
    ↓
SecurityContextPersistenceFilter (Khôi phục SecurityContext)
    ↓
CsrfFilter (Kiểm tra CSRF Token)
    ↓
UsernamePasswordAuthenticationFilter (Xử lý login form)
    ↓
JwtAuthenticationFilter (Custom - Verify JWT Token)
    ↓
AuthorizationFilter (Kiểm tra quyền truy cập)
    ↓
ExceptionTranslationFilter (Xử lý Authentication/AccessDenied)
    ↓
Controller
```

**Cấu hình trong dự án:**
```java
@Configuration
@EnableWebSecurity
public class SecurityConfig {
    
    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        http
            .csrf(csrf -> csrf.disable())  // Disable cho API
            .sessionManagement(session -> 
                session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
            .authorizeHttpRequests(auth -> auth
                .requestMatchers("/api/auth/**").permitAll()
                .requestMatchers("/api/admin/**").hasRole("ADMIN")
                .requestMatchers("/api/orders/**").hasAnyRole("ADMIN", "USER")
                .anyRequest().authenticated()
            )
            .addFilterBefore(jwtAuthFilter(), UsernamePasswordAuthenticationFilter.class);
        
        return http.build();
    }
}
```

---

### **BCrypt Password Encoding**

**Tại sao dùng BCrypt thay vì MD5/SHA1:**
- MD5/SHA1: Dễ bị **Rainbow Table Attack** (tra bảng hash ngược)
- BCrypt: One-way hash với **Salt tự động** + **Adaptive cost factor**

**Cách hoạt động:**
```
BCrypt Hash = $2a$10$N9qo8uLOickgx2ZMRZoMy.MqrHigE6FkKpR2G7rQeOu0jSH3Nprl6

$2a$  → Algorithm identifier (BCrypt)
10    → Cost factor (2^10 = 1024 rounds)
N9qo... → Salt (16 bytes random)
MqrHi... → Hash result
```

**Code trong dự án:**
```java
@Bean
public PasswordEncoder passwordEncoder() {
    return new BCryptPasswordEncoder();  // Default strength = 10
}

// Khi register
String encodedPassword = passwordEncoder.encode(rawPassword);
// Khi login
boolean matches = passwordEncoder.matches(rawPassword, encodedPassword);
```

**Ưu điểm:**
- Mỗi lần encode cùng password cho kết quả khác nhau (do salt random)
- Cost factor có thể tăng theo thời gian (hardware mạnh hơn → tăng round)

---

### **JPA Repository**

**3 Cấp độ Repository trong Spring Data:**

| Cấp độ | Interface | Mục đích |
|--------|-----------|----------|
| 1 | `JpaRepository<T, ID>` | CRUD + Paging + Sorting |
| 2 | `CrudRepository<T, ID>` | Basic CRUD operations |
| 3 | Custom Repository | Complex queries với @Query |

**Ví dụ các method có sẵn:**
```java
// Basic CRUD
T save(T entity);
Optional<T> findById(ID id);
List<T> findAll();
void deleteById(ID id);
boolean existsById(ID id);

// Paging & Sorting
Page<T> findAll(Pageable pageable);
List<T> findAll(Sort sort);
```

**Derived Query Methods:**
```java
// Spring tự động generate query
List<Order> findByStatusAndCreatedAtAfter(OrderStatus status, LocalDateTime date);
// → SELECT * FROM orders WHERE status = ? AND created_at > ?

List<Product> findByNameContainingIgnoreCaseAndPriceLessThan(String name, BigDecimal price);
// → SELECT * FROM products WHERE LOWER(name) LIKE LOWER(?) AND price < ?
```

**Custom Query với @Query:**
```java
@Query("SELECT new com.example.OrderSummary(o.id, o.total, u.fullname) " +
       "FROM Order o JOIN o.user u " +
       "WHERE o.status = :status " +
       "AND o.createdAt BETWEEN :start AND :end")
List<OrderSummary> findOrderSummariesByStatusAndDateRange(
    @Param("status") OrderStatus status,
    @Param("start") LocalDateTime start,
    @Param("end") LocalDateTime end
);
```

---

## 1.3 Database Normalization (1NF, 2NF, 3NF)

### **Bảng Products**

**❌ Trước khi chuẩn hóa:**
```sql
Products (id, name, price, category_id, category_name, supplier_name, supplier_address)
-- Bị lỗi: category_name và supplier_name phụ thuộc transitively
```

**✅ Sau khi chuẩn hóa:**

**1NF - Atomic Values:**
```sql
-- Tách giá thành base_price và discount_price thay vì lưu "100000-10%"
Products (id, name, base_price, discount_percent, category_id)
```

**2NF - Loại bỏ Partial Dependency:**
```sql
-- Tách Category ra bảng riêng
Categories (id, name, description)
Products (id, name, base_price, discount_percent, category_id) -- FK

-- Tách Supplier ra bảng riêng
Suppliers (id, name, address, phone)
Product_Suppliers (product_id, supplier_id, supply_price) -- Junction table
```

**3NF - Loại bỏ Transitive Dependency:**
```sql
-- Không lưu calculated fields
-- Không lưu: total_price = base_price * (1 - discount)
-- Tính toán trong application layer hoặc SQL view
```

---

### **Bảng Orders**

**❌ Trước khi chuẩn hóa:**
```sql
Orders (id, user_id, product_ids, quantities, prices, total, address, status)
-- Bị lỗi: product_ids lưu nhiều giá trị trong 1 cột (vi phạm 1NF)
```

**✅ Sau khi chuẩn hóa 3NF:**

```sql
-- Orders: Chỉ lưu thông tin đơn hàng (không lưu chi tiết sản phẩm)
Orders (
    id PK,
    user_id FK,
    order_date,
    shipping_address_id FK,
    status,           -- PENDING, PROCESSING, SHIPPED, DELIVERED
    payment_status,   -- UNPAID, PAID, REFUNDED
    total_amount      -- Calculated field, có thể lưu để query nhanh
)

-- OrderDetails: Lưu snapshot giá tại thời điểm mua (quan trọng!)
OrderDetails (
    id PK,
    order_id FK,
    product_id FK,
    quantity,
    unit_price,       -- Giá tại thời điểm mua (snapshot)
    subtotal          -- unit_price * quantity
)

-- ShippingAddresses: Tách ra để tái sử dụng
ShippingAddresses (
    id PK,
    user_id FK,
    street,
    city,
    district,
    phone,
    is_default
)
```

**Tại sao cần snapshot giá trong OrderDetails?**
```
Kịch bản:
1. Khách mua iPhone 15 giá 25 triệu (10/01/2024)
2. Apple giảm giá xuống 22 triệu (15/01/2024)
3. Khách xem lại đơn hàng cũ

→ Nếu không có snapshot: Hiển thị giá hiện tại (22 triệu) → Sai!
→ Có snapshot: Hiển thị 25 triệu → Đúng giá lúc mua
```

---

# PHẦN 2: GIẢ LẬP PHỎNG VẤN (MOCK INTERVIEW)

## 🔥 Câu Hỏi 1: Data Flow & Architecture

**"Em hãy mô tả luồng dữ liệu khi người dùng nhấn 'Thêm vào giỏ hàng' cho đến khi sản phẩm được lưu vào Database. Cụ thể:
1. Dữ liệu đi qua những layer nào?
2. Tại sao em không lưu giỏ hàng vào LocalStorage mà lại gọi API?
3. Nếu có 1000 người cùng thêm sản phẩm vào giỏ hàng lúc 12:00 trưa, Backend của em xử lý như thế nào để không bị quá tải?"**

### ✅ Câu Trả Lời Mẫu (Chuyên Nghiệp)

"Vâng, trong dự án E-Commerce của em, luồng 'Thêm vào giỏ hàng' được thiết kế theo **Layered Architecture** với các tầng sau:

**1. Data Flow:**
```
User clicks "Add to Cart" 
  → VueJS Component emits event 
  → Vuex Store dispatches 'addToCart' action 
  → Axios POST /api/cart/add with JWT Token in Header
  → Spring Security Filter Chain validates token
  → CartController receives request
  → CartService processes business logic (validate stock, check existing item)
  → CartRepository.save() via JPA/Hibernate
  → SQL Server INSERT/UPDATE cart_items table
  → Response 200 OK with updated cart
  → Vuex updates state → UI re-renders
```

**2. LocalStorage vs Database:**
Em chọn lưu vào Database vì:
- **Multi-device sync**: Khách hàng thêm trên mobile, checkout trên desktop
- **Persistence**: Giỏ hàng không mất khi clear browser cache
- **Business analytics**: Có thể phân tích abandoned cart để làm marketing
- **Security**: Product price controlled by server, không bị client-side manipulation

**3. Handle 1000 concurrent users:**
Em áp dụng các kỹ thuật **scalability**:
- **Database Connection Pooling** (HikariCP): Giới hạn 20 connections, queue requests
- **Caching** with Redis: Cart data cache 5 minutes, giảm DB load
- **Optimistic Locking**: Không lock record khi read, chỉ check version khi write
- **Async Processing**: Sử dụng @Async cho non-critical operations (ví dụ: send notification)
- **Rate Limiting**: Giới hạn 10 requests/minute per IP để chống DDoS

Em cũng có thể tối ưu thêm bằng cách dùng **Redis as primary cart storage**, đồng bộ với DB asynchronously."

### 📝 Keywords tiếng Anh cần dùng:
- **Layered Architecture**, **Separation of Concerns**
- **State Management** (Vuex/Pinia)
- **Persistence**, **Multi-device Synchronization**
- **Connection Pooling**, **Optimistic/Pessimistic Locking**
- **Asynchronous Processing**, **Eventual Consistency**
- **Scalability**, **High Availability**

---

## 🔥 Câu Hỏi 2: SQL & Database Optimization

**"Với chứng chỉ SQL Intermediate của em, hãy giải thích:
1. Em đã dùng chỉ mục (Index) như thế nào trong bảng Orders? Tại sao?
2. Nếu em có query: `SELECT * FROM orders WHERE user_id = 5 AND status = 'DELIVERED' ORDER BY created_at DESC`, em sẽ tạo index như thế nào để query chạy nhanh nhất?
3. Khác biệt giữa Clustered Index và Non-clustered Index là gì?"**

### ✅ Câu Trả Lời Mẫu (Chuyên Nghiệp)

"Vâng, với kinh nghiệm từ chứng chỉ SQL Intermediate và dự án E-Commerce, em áp dụng indexing strategy như sau:

**1. Indexing Strategy trong dự án:**

```sql
-- B-Tree Index cho frequently queried columns
CREATE INDEX idx_orders_user_id ON orders(user_id);
CREATE INDEX idx_orders_status ON orders(status);
CREATE INDEX idx_orders_created_at ON orders(created_at);

-- Composite Index cho common query patterns
CREATE INDEX idx_orders_user_status_created 
ON orders(user_id, status, created_at DESC);
```

**Lý do:**
- `user_id`: Khách hàng thường xuyên xem lịch sử đơn hàng của mình
- `status`: Admin thường filter theo status (PENDING, DELIVERED)
- `created_at`: Sort theo thời gian (đơn mới nhất lên đầu)

**2. Optimal Index cho query đó:**

Query: `SELECT * FROM orders WHERE user_id = 5 AND status = 'DELIVERED' ORDER BY created_at DESC`

```sql
-- Composite Index với Column Order quan trọng!
CREATE NONCLUSTERED INDEX idx_orders_optimal 
ON orders(user_id, status, created_at DESC);
```

**Giải thích Column Order:**
- `user_id` first: Equality check (= 5) → Most selective
- `status` second: Equality check (= 'DELIVERED')
- `created_at DESC` last: Range/Sort operation

**Query Execution Plan sẽ là:**
1. **Index Seek** trên user_id = 5 (nhanh, O(log n))
2. **Index Scan** nhỏ trên status (filter ngay trên index)
3. **Sort** đã được pre-computed trong index (created_at DESC)
4. **Key Lookup** để lấy các columns còn lại từ Clustered Index

**3. Clustered vs Non-clustered:**

| Đặc điểm | Clustered Index | Non-clustered Index |
|---------|-----------------|---------------------|
| **Data storage** | Chính là bảng data (leaf = data rows) | Leaf chứa pointer đến clustered index |
| **Số lượng** | Mỗi bảng chỉ có 1 | Mỗi bảng có thể có nhiều (999 SQL Server) |
| **Sort order** | Bảng được sắp xếp vật lý theo CI | Không ảnh hưởng thứ tự vật lý |
| **Performance** | Nhanh cho range queries | Nhanh cho exact match lookups |
| **Size** | Không tốn thêm space | Tốn additional storage (20-30% table size) |

**Best Practice em áp dụng:**
- Primary Key mặc định là Clustered Index
- Thường để là `id` (auto-increment) để INSERT nhanh ở cuối bảng
- Không để Clustered Index trên cột hay thay đổi (ví dụ: last_updated) vì sẽ gây page splits, fragment index"

### 📝 Keywords tiếng Anh cần dùng:
- **B-Tree Index**, **Clustered Index**, **Non-clustered Index**
- **Index Seek** vs **Index Scan** vs **Table Scan**
- **Composite Index**, **Covering Index**, **Included Columns**
- **Query Execution Plan**, **Selectivity**, **Cardinality**
- **Page Split**, **Index Fragmentation**
- **Key Lookup** (Bookmark Lookup)

---

## 🔥 Câu Hỏi 3: Spring Security Deep Dive

**"Em đã dùng Spring Security trong dự án. Hãy giải thích sâu hơn:
1. Cơ chế **Filter Chain** hoạt động như thế nào? Nếu em cần thêm custom filter để log mọi request, em sẽ đặt nó ở đâu trong chuỗi?
2. Em lưu JWT Token ở đâu? LocalStorage hay Cookie? Tại sao? Cách nào an toàn hơn?
3. Nếu attacker lấy được JWT Token của user, hệ thống của em có cách nào phát hiện và vô hiệu hóa không?"**

### ✅ Câu Trả Lời Mẫu (Chuyên Nghiệp)

"Vâng, em đã tìm hiểu sâu về Spring Security để implement trong dự án. Chi tiết như sau:

**1. Filter Chain Mechanism:**

```java
// Order of filters (very important!)
1. CorsFilter                    // Handle CORS preflight
2. CsrfFilter                   // Validate CSRF token (disabled for stateless API)
3. UsernamePasswordAuthFilter   // Process /login endpoint
4. JwtAuthenticationFilter      // My custom filter - validate JWT
5. AuthorizationFilter          // Check @PreAuthorize annotations
6. ExceptionTranslationFilter     // Handle auth exceptions
```

**Vị trí Custom Logging Filter:**
```java
// Đặt SAU SecurityContextPersistenceFilter nhưng TRƯỚC các auth filter
// Để có thể log cả request đã auth và chưa auth

@Component
public class RequestLoggingFilter extends OncePerRequestFilter {
    @Override
    protected void doFilterInternal(HttpServletRequest request, 
                                      HttpServletResponse response, 
                                      FilterChain chain) {
        String username = SecurityContextHolder.getContext()
                          .getAuthentication() != null 
                          ? SecurityContextHolder.getContext()
                            .getAuthentication().getName() 
                          : "anonymous";
        
        log.info("Request: {} {} - User: {} - IP: {}", 
            request.getMethod(), 
            request.getRequestURI(),
            username,
            request.getRemoteAddr()
        );
        
        chain.doFilter(request, response);
    }
}

// Configuration
http.addFilterAfter(new RequestLoggingFilter(), 
                    SecurityContextHolderFilter.class);
```

**2. JWT Storage - LocalStorage vs Cookie:**

| Criteria | LocalStorage | HttpOnly Cookie |
|---------|-------------|-----------------|
| **XSS Vulnerability** | ❌ Dễ bị đọc bởi JavaScript | ✅ Không thể đọc bởi JS |
| **CSRF Vulnerability** | ✅ Không bị CSRF | ❌ Cần CSRF Token |
| **Size Limit** | ~5MB | ~4KB |
| **Auto-sent** | Manual (JS fetch) | Auto-sent with every request |
| **Domain scope** | Origin only | Can be configured |

**Em chọn giải pháp kết hợp:**
```
Access Token:  HttpOnly Cookie (15 phút expiry)
Refresh Token: HttpOnly Cookie (7 ngày expiry) + Fingerprinting
```

**Fingerprinting technique:**
```java
// Tạo token binding với device fingerprint
String fingerprint = hash(userAgent + ipAddress + acceptLanguage);
String refreshToken = JWT.create()
    .withClaim("fingerprint", fingerprint)
    .sign(algorithm);

// Khi refresh, verify fingerprint matches
if (!fingerprintFromRequest.equals(fingerprintFromToken)) {
    throw new SecurityException("Suspicious activity detected");
}
```

**3. Token Revocation Strategy:**

Nếu attacker có JWT:

**a) Short-lived token (Access Token: 15 phút):**
- Damage limited: Chỉ có 15 phút để exploit
- Không thể revoke (stateless), đành chờ hết hạn

**b) Token Blacklist với Redis:**
```java
// Khi user logout hoặc phát hiện breach
redisTemplate.opsForSet().add("token:blacklist", tokenId);
redisTemplate.expire("token:blacklist", 15, TimeUnit.MINUTES);

// Trong JwtAuthenticationFilter
String tokenId = decodedJWT.getId();
if (redisTemplate.opsForSet().isMember("token:blacklist", tokenId)) {
    throw new TokenBlacklistedException();
}
```

**c) Version/Token Family:**
```java
// Mỗi user có token version trong DB
// Khi đổi password → increment version → tất cả tokens cũ invalid
```

**d) Rotation Strategy:**
```
1. User login → Get Access Token (AT1) + Refresh Token (RT1)
2. AT1 hết hạn → Dùng RT1 để lấy AT2 + RT2
3. RT1 bị đánh dấu "consumed" → Cannot reuse
4. Nếu attacker dùng RT1 → Server detect reuse → Revoke toàn bộ token family
```"

### 📝 Keywords tiếng Anh cần dùng:
- **Filter Chain**, **Security Context**, **Authentication Flow**
- **HttpOnly Cookie**, **Secure Flag**, **SameSite Attribute**
- **Token Fingerprinting**, **Device Binding**
- **Token Blacklist**, **Token Revocation**, **Token Rotation**
- **Short-lived Access Token**, **Long-lived Refresh Token**
- **Compromised Token Detection**, **Automatic Logout**

---

## 🔥 Câu Hỏi 4: Transaction & Concurrency

**"Một tình huống thực tế: Giả sử có 2 người cùng mua chiếc iPhone 15 cuối cùng (stock = 1) tại cùng thời điểm (12:00:00.000). Cả 2 đều nhấn 'Đặt hàng' cùng lúc.
1. Không có locking mechanism nào, điều gì sẽ xảy ra? (Race condition)
2. Em sẽ dùng **Optimistic Locking** hay **Pessimistic Locking**? Giải thích trade-off.
3. Nếu dùng Pessimistic Lock, em dùng `SELECT FOR UPDATE` hay `UPDATE ... WHERE stock > 0`?"**

### ✅ Câu Trả Lời Mẫu (Chuyên Nghiệp)

"Đây là vấn đề **Race Condition** và **Overselling** rất phổ biến trong E-Commerce. Em phân tích như sau:

**1. Race Condition (Không có locking):**

```
Timeline:
12:00:00.000 - User A: SELECT stock → Result: 1
12:00:00.100 - User B: SELECT stock → Result: 1  (Cùng đọc 1!)
12:00:00.200 - User A: UPDATE stock = 0 → Success
12:00:00.300 - User B: UPDATE stock = -1 → Success (!!!)
              
Kết quả: Stock = -1 (Overselling!) 
Cả 2 đều thấy 'Đặt hàng thành công' nhưng chỉ có 1 sản phẩm!
```

**2. Optimistic vs Pessimistic Locking:**

| Đặc điểm | Optimistic Locking | Pessimistic Locking |
|---------|-------------------|---------------------|
| **Cơ chế** | Dùng version column, check khi update | Lock row/database khi read |
| **Performance** | Tốt (không block), nhưng cần retry | Chậm hơn (block other transactions) |
| **Conflict rate** | Thích hợp khi conflict hiếm | Thích hợp khi conflict cao |
| **Scalability** | Tốt | Kém hơn (deadlock risk) |

**Em chọn Pessimistic Locking cho E-Commerce** vì:
- Stock conflict **rất cao** khi flash sale
- Không thể "retry" đặt hàng (trải nghiệm khách hàng tệ)
- Cần đảm bảo **immediate consistency** về tồn kho

**3. Implementation:**

```java
@Transactional(isolation = Isolation.SERIALIZABLE)
public Order createOrder(Long productId, int quantity) {
    // Pessimistic Lock - Block other transactions
    Product product = productRepo
        .findByIdWithPessimisticLock(productId);
    
    if (product.getStock() < quantity) {
        throw new InsufficientStockException();
    }
    
    // Deduct stock
    product.setStock(product.getStock() - quantity);
    productRepo.save(product);
    
    // Create order...
}

// Repository
@Lock(LockModeType.PESSIMISTIC_WRITE)  // = SELECT FOR UPDATE
@Query("SELECT p FROM Product p WHERE p.id = :id")
Optional<Product> findByIdWithPessimisticLock(@Param("id") Long id);
```

**Alternative: Atomic UPDATE (Optimistic approach không cần lock):**
```sql
-- Cách không dùng lock nhưng vẫn an toàn
UPDATE products 
SET stock = stock - 1 
WHERE id = 123 AND stock >= 1;

-- Check affected rows
-- Nếu affected = 0 → Stock không đủ, rollback
```

```java
int updated = productRepo.decreaseStock(productId, quantity);
if (updated == 0) {
    throw new InsufficientStockException();
}

// Repository
@Modifying
@Query("UPDATE Product p SET p.stock = p.stock - :qty " +
       "WHERE p.id = :id AND p.stock >= :qty")
int decreaseStock(@Param("id") Long id, @Param("qty") int quantity);
```

**Em prefer cách 2 (Atomic UPDATE)** vì:
- Không cần lock → Không có deadlock risk
- Single statement → Atomic by nature
- Check affected rows để biết thành công hay không"

### 📝 Keywords tiếng Anh cần dùng:
- **Race Condition**, **Overselling**, **Lost Update Problem**
- **Optimistic Locking** (@Version), **Pessimistic Locking** (SELECT FOR UPDATE)
- **Serializable Isolation**, **ACID Properties**
- **Atomic Operation**, **Compare-and-Swap (CAS)**
- **Deadlock**, **Lock Timeout**, **Retry Strategy**
- **Immediate Consistency** vs **Eventual Consistency**

---

## 🔥 Câu Hỏi 5: Behavioral & Teamwork (Agile/Scrum)

**"Trong dự án của em, team áp dụng Agile/Scrum với Minh là PO và Vân là SM. Hãy kể một lần em có conflict với Minh hoặc Vân về technical decision, và cách em resolve nó.

Ví dụ: Minh yêu cầu feature phải release vào Friday nhưng em biết có critical bug chưa fix? Hoặc Vân đưa ra solution mà em nghĩ không optimal?"**

### ✅ Câu Trả Lời Mẫu (Chuyên Nghiệp)

"Thực ra trong quá trình làm việc với Minh (PO) và Vân (SM), em học được rất nhiều về **communication** và **negotiation**. Em kể một tình huống cụ thể:

**Tình huống:**
- Sprint Planning: Minh prioritize feature 'Flash Sale' với deadline Friday
- Em đang implement **Pessimistic Locking** cho inventory
- Đến Wednesday, em phát hiện deadlock potential trong high concurrency scenario
- Nếu cứ tiếp tục, risk là production crash trong flash sale event

**Cách em xử lý:**

**1. Prepare Data (Evidence-based):**
```
- Em viết simple load test với JMeter: 100 concurrent users cùng checkout
- Kết quả: 15% requests bị deadlock timeout (10 seconds)
- Document: 'Without fix → potential revenue loss ~500 orders/minute'
```

**2. Schedule 1-on-1 với Minh (không phải trong Daily Scrum):**

'Hi Minh, em cần discuss về Flash Sale feature. Em đã test và phát hiện risk về database locking. Em có data cho thấy có thể mất ~30% orders nếu không optimize. Em propose 2 options:

- Option A: Delay 1 ngày (release Saturday), em implement Atomic UPDATE thay vì Pessimistic Lock
- Option B: Release Friday nhưng disable Flash Sale, chỉ bật sau khi em fix và test xong

Em recommend Option A vì risk-reward tốt hơn. Minh thấy sao?'

**3. Đưa ra technical alternatives:**

Em không chỉ nói 'không được' mà đưa ra **options**:

| Option | Pros | Cons | Business Impact |
|--------|------|------|-----------------|
| A: Delay 1 day | Safe, high performance | Miss Friday marketing | Long-term gain |
| B: Release partial | On-time | No Flash Sale | Short-term visible |
| C: Keep Pessimistic Lock | On-time | 15% failure rate | High risk |

**4. Escalate đúng cách nếu cần:**

Nếu Minh vẫn insist, em suggest: 'Let's bring this to Sprint Retrospective và ask Vân (SM) và team vote. Business value vs Technical debt.'

**Kết quả:**
- Minh chọn Option A sau khi thấy data
- Team release Saturday với 0 incident
- Minh appreciate em vì 'flagging risk early with data'

**Bài học của em:**
- **Don't say no without alternatives**
- **Data-driven discussion** beats opinion-based
- **Timing matters**: Đừng chờ đến Friday mới nói có problem
- **Respect hierarchy** nhưng vẫn advocate cho technical excellence"

### 📝 Keywords tiếng Anh cần dùng:
- **Stakeholder Management**, **Conflict Resolution**
- **Evidence-based Decision Making**
- **Risk Assessment**, **Risk Mitigation**
- **Technical Debt**, **Trade-off Analysis**
- **Negotiation Skills**, **Advocacy**
- **Sprint Planning**, **Backlog Grooming**
- **Retroactive Discussion**, **Lessons Learned"

---

# PHẦN 3: CHIẾN THUẬT "BÁN MÌNH" (PERSONAL BRANDING)

## 3.1 Lồng Ghép Chứng Chỉ SQL Intermediate

### Cách đề cập tự nhiên trong câu trả lời:

**❌ Không nên:**
> "Em có chứng chỉ SQL Intermediate từ HackerRank."

**✅ Nên nói:**
> "Trong quá trình optimize query cho báo cáo doanh thu, em áp dụng kiến thức từ **certification path** em đang theo đuổi. Cụ thể là em dùng **Composite Index** với **Covering Index** để tránh **Key Lookup** - một technique em học được khi deep dive vào **Query Execution Plan** và **Index Internals**."

**Keywords liên quan đến chứng chỉ:**
- **Query Optimization**, **Index Tuning**, **Execution Plan Analysis**
- **Database Normalization**, **Denormalization Strategy**
- **Cardinality**, **Selectivity**, **Histogram Statistics**
- **Partitioning Strategy**, **Sharding Considerations**

---

## 3.2 Giải Thích Về Teamwork & Agile

### Framework: STAR Method (Situation - Task - Action - Result)

**Ví dụ về Git Conflict với Minh:**

**Situation:**
> "Trong Sprint 3, Minh và em cùng sửa file ProductController.java. Minh thêm API search, em thêm API filter."

**Task:**
> "Khi pull code, em gặp merge conflict vì cả 2 đều modify cùng method signature."

**Action:**
> "Em không tự ý resolve. Em:
> 1. Save lại code của mình (stash)
> 2. Call Minh để discuss: 'Anh ơi, cả 2 đều sửa ProductController. Anh thêm search ở line 45, em thêm filter ở line 50. Chúng ta giữ cả 2 được không?'
> 3. Minh đồng ý → Em merge manually, giữ cả 2 method
> 4. Test lại cả 2 API trước khi push"

**Result:**
> "Không có code loss, cả 2 feature đều work. Minh appreciate vì em communicate thay vì assume."

---

## 3.3 Thể Hiện Tư Duy Data Engineering

### Mention các khái niệm Data Engineering:

**Khi nói về E-Commerce:**
> "Em thiết kế schema với **future analytics** trong mind. Ví dụ: em không delete orders mà chỉ mark as **soft-delete** (is_active flag) để phục vụ **data warehouse** và **business intelligence** sau này. Em cũng consider **CDC (Change Data Capture)** pattern để stream order events đến **data lake** cho real-time analytics."

**Keywords Data Engineering:**
- **ETL/ELT Pipeline**, **Data Warehouse**, **Data Lake**
- **Change Data Capture (CDC)**, **Event Streaming**
- **Soft Delete vs Hard Delete**, **Audit Trail**
- **Data Retention Policy**, **GDPR Compliance**
- **Data Partitioning**, **Time-series Data**

---

# PHẦN 4: CHECKLIST TRƯỚC KHI PHỎNG VẤN

## ✅ Technical Preparation:
- [ ] Review code dự án E-Commerce (đặc biệt Order flow, Security config)
- [ ] Practice giải thích bằng tiếng Anh (1-2 phút mỗi concept)
- [ ] Chuẩn bị portfolio/GitHub link để share screen
- [ ] Test microphone và camera

## ✅ Behavioral Preparation:
- [ ] Chuẩn bị 3 câu chuyện STAR (conflict, failure, success)
- [ ] Nghiên cứu công ty (tech stack, products, values)
- [ ] Chuẩn bị câu hỏi ngược lại ("What does success look like for this intern role?")

## ✅ Setup Environment:
- [ ] IDE mở sẵn project
- [ ] Postman/HTTP Client có sẵn API calls
- [ ] Database client (DBeaver/DataGrip) connected
- [ ] Notepad để take notes

---

# TÓM TẮT KEY POINTS

## Điểm Mạnh Cần Nhấn Mạnh:
1. **SQL Intermediate + Data Engineering** → Tư duy database mạnh
2. **Full-stack experience** → Hiểu end-to-end flow
3. **Agile/Scrum teamwork** → Có thể làm việc nhóm
4. **Security mindset** → JWT, BCrypt, Spring Security

## Câu Trả Lời Mẫu Tốt Nhất:
- **Ngắn gọn**: 2-3 phút mỗi câu
- **Có structure**: Layer 1 → Layer 2 → Layer 3
- **Có ví dụ cụ thể**: "Trong dự án E-Commerce của em..."
- **Keywords tiếng Anh**: Lồng ghép tự nhiên

## Điều Cần Tránh:
- ❌ Nói quá nhiều về theory không liên quan project
- ❌ Không biết thì đoán → Thành thật "Em chưa gặp case này nhưng em sẽ..."
- ❌ Nói xấu teammates (kể conflict phải respectful)

---

**Good luck, Hiếu! You've got this! 🚀**
