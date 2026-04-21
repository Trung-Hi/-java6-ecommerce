# Hướng Dẫn Soft Delete với SweetAlert2

## 📋 Tóm Tắt Tính Năng

Đã triển khai **Soft Delete** cho User Management với SweetAlert2 confirmation dialog.

---

## 🎨 Frontend (Vue.js + SweetAlert2)

### 1. Cài Đặt SweetAlert2

```bash
npm install sweetalert2
```

### 2. Import và Sử Dụng

```javascript
import Swal from "sweetalert2";
import "sweetalert2/dist/sweetalert2.min.css";
```

### 3. Hàm Xử Lý Xóa (handleDelete)

```javascript
const handleDelete = async (u) => {
    if (!u?.username) return;
    
    deletingUser.value = u;
    
    // Hiển thị SweetAlert2 confirmation
    const result = await Swal.fire({
        title: "Bạn có chắc chắn muốn xóa?",
        html: `Tài khoản <strong>"${u.username}"</strong> sẽ bị vô hiệu hóa...`,
        icon: "warning",
        showCancelButton: true,
        confirmButtonText: "Xác nhận xóa",
        cancelButtonText: "Hủy",
        confirmButtonColor: "#ef4444",  // Red
        cancelButtonColor: "#6b7280",     // Gray
        reverseButtons: true,
        focusCancel: true
    });
    
    if (result.isConfirmed) {
        // Show loading
        Swal.fire({
            title: "Đang xử lý...",
            showConfirmButton: false,
            didOpen: () => { Swal.showLoading(); }
        });
        
        // Call API
        await remove(u.username);
        
        // Success toast
        Swal.fire({
            title: "Đã xóa!",
            text: `Tài khoản đã được vô hiệu hóa.`,
            icon: "success",
            timer: 3000,
            timerProgressBar: true
        });
        
        await load(); // Refresh table
    }
};
```

### 4. Lọc Tài Khoản Đã Xóa

```javascript
const visibleRows = computed(() => (rows.value || []).filter((u) => {
    // Exclude current logged-in user
    if (u?.username === currentUsername.value) return false;
    // Exclude soft-deleted accounts (isDelete = true)
    if (u?.isDelete === true || u?.is_delete === true) return false;
    return true;
}));
```

---

## 🔧 Backend (Spring Boot + JPA)

### 1. Entity - Account.java

```java
@Entity
@Table(name = "accounts")
public class Account {
    
    @Id
    private String username;
    
    // ... other fields ...
    
    @Column(name = "is_delete", nullable = false)
    private Boolean isDelete = false;  // Soft delete flag
    
    @PrePersist
    @PreUpdate
    public void prePersist() {
        if (isDelete == null) {
            isDelete = false;
        }
    }
    
    // Getters and Setters
}
```

### 2. Service - Soft Delete Implementation

```java
@Service
public class AccountServiceImpl implements AccountService {
    
    @Autowired
    private AccountRepository accountRepository;
    
    /**
     * Soft delete - Set isDelete = true instead of physical delete
     */
    @Override
    public void deleteByUsername(String username) {
        accountRepository.findById(username).ifPresent(account -> {
            account.setIsDelete(true);  // Mark as deleted
            accountRepository.save(account);
        });
    }
    
    /**
     * Find all active accounts (not soft deleted)
     */
    @Override
    public List<Account> findAll() {
        return accountRepository.findByIsDeleteFalse();
    }
}
```

### 3. Repository - Custom Query

```java
@Repository
public interface AccountRepository extends JpaRepository<Account, String> {
    
    // Find only active accounts
    List<Account> findByIsDeleteFalse();
    
    // Find only deleted accounts
    List<Account> findByIsDeleteTrue();
    
    // Find all including deleted
    @Query("SELECT a FROM Account a")
    List<Account> findAllIncludingDeleted();
}
```

### 4. Controller Endpoint

```java
@RestController
@RequestMapping("/api/admin/accounts")
public class AccountAController {
    
    @DeleteMapping("/{username}")
    public ResponseEntity<ApiResponse<?>> delete(
            @PathVariable("username") String username) {
        
        // Prevent self-delete
        Account currentUser = authService.getUser();
        if (currentUser != null && currentUser.getUsername().equals(username)) {
            throw new ApiException(HttpStatus.BAD_REQUEST, 
                "Không thể xóa tài khoản đang đăng nhập");
        }
        
        // Soft delete
        Account account = accountService.findByUsername(username)
            .orElseThrow(() -> new ApiException(HttpStatus.NOT_FOUND, 
                "Không tìm thấy tài khoản"));
        
        if (Boolean.TRUE.equals(account.getIsDelete())) {
            throw new ApiException(HttpStatus.BAD_REQUEST, 
                "Tài khoản đã bị xóa trước đó");
        }
        
        accountService.deleteByUsername(username);  // Soft delete
        
        return ResponseEntity.ok(
            ApiResponse.success("Xóa tài khoản thành công", null)
        );
    }
}
```

---

## 🗄️ SQL Server Schema

```sql
-- Bảng accounts với soft delete flag
CREATE TABLE accounts (
    username VARCHAR(50) PRIMARY KEY,
    password VARCHAR(255) NOT NULL,
    fullname NVARCHAR(100),
    email VARCHAR(100),
    phone VARCHAR(20),
    address NVARCHAR(255),
    photo VARCHAR(255),
    activated BIT DEFAULT 1,
    is_delete BIT DEFAULT 0,  -- Soft delete flag (0 = active, 1 = deleted)
    created_at DATETIME DEFAULT GETDATE(),
    updated_at DATETIME DEFAULT GETDATE()
);

-- Index cho truy vấn nhanh
CREATE INDEX idx_accounts_is_delete ON accounts(is_delete);
CREATE INDEX idx_accounts_activated ON accounts(activated);
```

---

## ✅ Luồng Hoạt Động

1. **Admin Click Delete** → SweetAlert2 Confirmation Dialog hiện lên
2. **Admin Xác Nhận** → API DELETE `/api/admin/accounts/{username}` được gọi
3. **Backend Soft Delete** → `isDelete = true` được set, record vẫn tồn tại trong DB
4. **Frontend Toast Success** → Hiển thị thông báo thành công
5. **Table Auto Refresh** → Danh sách được reload, tài khoản đã xóa biến mất khỏi UI

---

## 🔒 Bảo Mật

- Không thể tự xóa chính mình
- Tài khoản đã xóa không thể đăng nhập
- Dữ liệu lịch sử (đơn hàng, review...) vẫn được giữ lại
- Có thể khôi phục tài khoản bằng cách set `isDelete = false`

---

## 🔄 Khôi Phục Tài Khoản (Tùy Chọn)

```java
@PutMapping("/{username}/restore")
public ResponseEntity<ApiResponse<?>> restore(@PathVariable String username) {
    Account account = accountService.findByUsername(username)
        .orElseThrow(() -> new ApiException(HttpStatus.NOT_FOUND, 
            "Không tìm thấy tài khoản"));
    
    if (!Boolean.TRUE.equals(account.getIsDelete())) {
        throw new ApiException(HttpStatus.BAD_REQUEST, 
            "Tài khoản chưa bị xóa");
    }
    
    account.setIsDelete(false);  // Restore
    accountService.update(account);
    
    return ResponseEntity.ok(
        ApiResponse.success("Khôi phục tài khoản thành công", null)
    );
}
```
