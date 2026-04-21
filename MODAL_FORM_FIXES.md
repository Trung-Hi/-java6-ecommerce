# 🔧 Fix Modal "Thêm tài khoản" - Tóm Tắt

## ✅ Các Thay Đổi Đã Thực Hiện

### 1. Modal Không Đóng Khi Click Outside Hoặc Nhấn Esc

**Trước:**
```html
<div class="userModalBackdrop" @click.self="closeModal">
```

**Sau:**
```html
<div class="userModalBackdrop userModalStatic">
```

**CSS thêm:**
```css
.userModalBackdrop.userModalStatic {
    pointer-events: none; /* Cho click xuyên qua backdrop */
}

.userModalBackdrop.userModalStatic .userModalPanel {
    pointer-events: auto; /* Nhưng bắt click trên panel */
}
```

**Xử lý phím Escape:**
```javascript
const handleEscKey = (e) => {
    if (e.key === 'Escape' && modalOpen.value) {
        e.preventDefault();
        e.stopPropagation();
        // Không làm gì - modal vẫn mở
    }
};

// Thêm listener khi mở modal
document.addEventListener('keydown', handleEscKey);

// Xóa listener khi đóng modal
const enhancedCloseModal = () => {
    document.removeEventListener('keydown', handleEscKey);
    originalCloseModal();
};
```

---

### 2. Xử Lý Lỗi Với SweetAlert2 (Z-Index Cao)

**Hàm handleSubmit mới:**
```javascript
const handleSubmit = async () => {
    try {
        // Validation với SweetAlert
        if (!form.phone?.trim()) {
            await Swal.fire({
                title: "Lỗi!",
                text: "Số điện thoại là bắt buộc",
                icon: "error",
                confirmButtonColor: "#ef4444",
                customClass: { popup: "swal-high-zindex" }  // ← Z-index cao
            });
            return;
        }
        
        // Loading
        Swal.fire({
            title: "Đang tạo...",
            allowOutsideClick: false,
            customClass: { popup: "swal-high-zindex" },
            didOpen: () => Swal.showLoading()
        });
        
        // Call API
        await api.admin.accounts.create(form);
        
        // Success
        Swal.close();
        await Swal.fire({
            title: "Tạo tài khoản thành công!",
            icon: "success",
            customClass: { popup: "swal-high-zindex" }
        });
        
        // Đóng modal và reload
        await load();
        enhancedCloseModal();
        
    } catch (error) {
        // QUAN TRỌNG: Không đóng modal, giữ nguyên form data!
        Swal.close();
        
        // Trích xuất lỗi từ Spring Boot
        let errorMessage = "Có lỗi xảy ra. Vui lòng thử lại.";
        if (error?.response?.data?.message) {
            errorMessage = error.response.data.message;  // ← Từ Spring Boot
        } else if (error?.message) {
            errorMessage = error.message;
        }
        
        // Hiển thị lỗi với z-index cao hơn modal
        await Swal.fire({
            title: "Lỗi!",
            html: `<div style="max-width: 400px;">${errorMessage}</div>`,
            icon: "error",
            customClass: { 
                popup: "swal-high-zindex",  // ← Đè lên modal
                confirmButton: "swal-error-btn"
            }
        });
        
        // Modal vẫn mở, form data được giữ!
    }
};
```

**CSS Z-Index cao cho SweetAlert:**
```css
.swal-high-zindex {
    z-index: 99999 !important;
}

.swal2-container.swal2-shown {
    z-index: 99999 !important;
}

.swal2-popup.swal-high-zindex {
    z-index: 999999 !important;
}
```

---

### 3. Fix Autocomplete Warning Chrome/Edge

**Trước:**
```html
<input type="password" v-model="form.password" class="userFormInput">
```

**Sau:**
```html
<input 
    type="password" 
    v-model="form.password" 
    class="userFormInput"
    autocomplete="new-password"
    name="password"
>
```

---

### 4. Backend Integration - Bắt Lỗi Spring Boot

**Spring Boot trả về lỗi:**
```java
@PostMapping
public ResponseEntity<ApiResponse<?>> create(@RequestParam... ) {
    // Kiểm tra username đã tồn tại
    if (accountService.existsByUsername(username)) {
        throw new ApiException(HttpStatus.BAD_REQUEST, 
            "Username đã tồn tại trong hệ thống");
    }
    // ...
}
```

**Frontend bắt lỗi:**
```javascript
} catch (error) {
    // error.response.data.message chứa "Username đã tồn tại..."
    let errorMessage = error?.response?.data?.message 
                    || error?.message 
                    || "Có lỗi xảy ra";
}
```

---

## 📋 Luồng Hoạt Động

1. **Admin click "Thêm tài khoản"**
   - Gọi `enhancedOpenCreate()`
   - Thêm Escape key listener
   - Mở modal

2. **Admin nhập dữ liệu và bấm "Lưu"**
   - Gọi `handleSubmit()`
   - Validation hiển thị bằng SweetAlert

3. **Validation lỗi (Frontend)**
   - SweetAlert hiện lỗi (z-index cao)
   - Modal không đóng
   - Form data giữ nguyên

4. **API trả về lỗi (Backend)**
   - VD: Username đã tồn tại
   - `error.response.data.message` chứa lỗi
   - SweetAlert hiện lỗi đè lên modal
   - Modal không đóng
   - Admin có thể sửa và submit lại

5. **Thành công**
   - SweetAlert success
   - Modal đóng
   - Table reload

---

## 🎯 Điểm Quan Trọng

| Yêu Cầu | Cách Thực Hiện |
|---------|---------------|
| Không đóng khi click outside | `pointer-events: none` trên backdrop |
| Không đóng khi nhấn Esc | `handleEscKey` listener preventDefault |
| Chỉ đóng khi bấm Hủy/X | Nút gọi `enhancedCloseModal()` |
| Giữ form data khi lỗi | Không gọi closeModal() trong catch |
| SweetAlert đè lên modal | `swal-high-zindex` với `z-index: 99999` |
| Bắt lỗi Spring Boot | `error.response.data.message` |
| Fix autocomplete warning | `autocomplete="new-password"` |

---

## 🔧 File Đã Chỉnh Sửa

- `@AdminAccountView.vue` - Modal, form, CSS, JavaScript
