# Hướng dẫn Refactor Trang Admin

## Tổng quan

Đã tạo các component dùng chung và refactor các trang admin để đồng nhất UI/UX theo design system của trang Tài khoản.

## Các Component mới

### 1. AdminLayout.vue
**Vị trí:** `src/components/AdminLayout.vue`

**Chức năng:**
- Layout wrapper cho tất cả các trang admin
- Header cố định với logo "FASHION STORE", notification bell, user dropdown
- Sidebar bên trái với AdminNav
- Content area với background xám nhạt (#f8fafc)

**Props:** Không có (sử dụng slot)

**Slot:**
- Default slot: Nội dung trang

**Cách sử dụng:**
```vue
<template>
  <AdminLayout>
    <!-- Nội dung trang -->
  </AdminLayout>
</template>

<script setup>
import AdminLayout from "@/components/AdminLayout.vue";
</script>
```

---

### 2. AdminTableContainer.vue
**Vị trí:** `src/components/AdminTableContainer.vue`

**Chức năng:**
- Container dùng chung cho các trang có bảng dữ liệu
- Glass card bo góc rounded-xl
- Filter bar hàng ngang (glassmorphism)
- Table với hover effect
- Empty state animation
- Pagination slot

**Props:**
| Prop | Type | Default | Mô tả |
|------|------|---------|-------|
| `title` | String | required | Tiêu đề trang |
| `buttonText` | String | "Thêm mới" | Text nút thêm |
| `loading` | Boolean | false | Trạng thái loading |
| `isEmpty` | Boolean | false | Hiển thị empty state |
| `emptyMessage` | String | "Không có dữ liệu" | Tin nhắn empty |
| `hasActiveFilters` | Boolean | false | Có filter active không |
| `filterResults` | String | "" | Text kết quả (hỗ trợ HTML) |

**Slots:**
- `filters`: Filter bar với input, select, buttons
- `tableHeader`: `<tr>` chứa các `<th>`
- `tableBody`: Các `<tr>` dữ liệu
- `pagination`: Phân trang (optional)

**Events:**
- `@add`: Click nút thêm mới
- `@refresh`: Click nút làm mới filter

**Cách sử dụng:**
```vue
<template>
  <AdminTableContainer
    title="Quản lý sản phẩm"
    buttonText="Thêm sản phẩm"
    :has-active-filters="hasFilters"
    :filter-results="resultsText"
    :is-empty="isEmpty"
    @add="openModal"
    @refresh="resetFilters"
  >
    <template #filters>
      <div class="filterBar">
        <input v-model="search" class="filterInput" placeholder="Tìm kiếm...">
      </div>
    </template>
    
    <template #tableHeader>
      <tr>
        <th>Tên</th>
        <th>Giá</th>
      </tr>
    </template>
    
    <template #tableBody>
      <tr v-for="item in items" :key="item.id">
        <td>{{ item.name }}</td>
        <td>{{ item.price }}</td>
      </tr>
    </template>
  </AdminTableContainer>
</template>
```

---

## Các trang đã refactor

### ✅ AdminCategoryView_New.vue
- Sử dụng: `AdminLayout` + `AdminTableContainer`
- Tính năng: 
  - Filter tìm kiếm theo mã/tên danh mục
  - Avatar màu sắc theo danh mục
  - Modal thêm/sửa danh mục
  - Icon buttons (Sửa/Xóa)

### ✅ AdminOrderView_New.vue
- Sử dụng: `AdminLayout` + `AdminTableContainer`
- Tính năng:
  - Filter theo trạng thái đơn hàng
  - Badge trạng thái đồng nhất
  - Modal chi tiết đơn hàng
  - Status: Chờ TT, Đã đặt, Đang giao, Đã giao, Giao thất bại, Đã hủy

### ✅ AdminProductView.vue (đã cập nhật)
- Sử dụng: `AdminLayout` + `AdminTableContainer`
- Tính năng:
  - Debounced search (300ms)
  - Filter theo danh mục
  - Price slider (giữ nguyên)
  - Modal với form sản phẩm

### ✅ AdminAccountView.vue (đã đồng bộ)
- Đã có design chuẩn, làm mẫu cho các trang khác

### ✅ AdminVipView_New.vue
- Sử dụng: `AdminLayout` (layout đặc biệt)
- Tính năng:
  - 2 chế độ xem: Cards và Table
  - Top 3 VIP có hiệu ứng đặc biệt (🥇🥈🥉)
  - Export Excel
  - Card layout responsive

---

## CSS Classes Global (dùng chung)

### Action Buttons
```css
.actionButtons      /* Container flex gap-2 */
.actionBtn          /* Nút icon 34x34px */
.actionEditBtn      /* Nút sửa */
.actionDeleteBtn    /* Nút xóa */
.actionResetBtn     /* Nút reset password */
```

### Filter Bar
```css
.filterBar          /* Glassmorphism container */
.filterGroup        /* Group cho input */
.filterSearch       /* Search container */
.filterIcon         /* Icon trong input */
.filterInput        /* Input style */
.filterSelect       /* Select style */
.filterResetBtn     /* Nút làm mới */
```

### Badges
```css
.statusBadge        /* Badge trạng thái */
.statusDelivered   /* Đã giao - xanh lá */
.statusCancelled    /* Đã hủy - đỏ */
.statusPending      /* Chờ thanh toán - vàng */
.statusShipping     /* Đang giao - xanh dương */
.statusPlaced       /* Đã đặt - xám */

.roleBadge          /* Badge vai trò */
.roleBadgeAdmin     /* Admin - vàng */
.roleBadgeUser      /* User - xanh */
```

### Pagination
```css
.paginationWrapper   /* Container căn giữa */
.pagination         /* List flex */
.pageItem           /* Item */
.pageItem.active    /* Đang active */
.pageLink           /* Button */
```

---

## Design Tokens

### Màu sắc chính
| Tên | Mã màu | Sử dụng |
|-----|--------|---------|
| Primary | `#3b82f6` | Buttons, links, active states |
| Success | `#22c55e` | Đã giao, thành công |
| Danger | `#ef4444` | Xóa, hủy, lỗi |
| Warning | `#f59e0b` | Chờ thanh toán |
| Info | `#6366f1` | Admin role |

### Card
```css
background: rgba(255, 255, 255, 0.85);
border-radius: 16px;
box-shadow: 0 10px 40px -10px rgba(0, 0, 0, 0.1);
backdrop-filter: blur(8px);
border: 1px solid rgba(255, 255, 255, 0.5);
```

### Filter Bar
```css
background: rgba(255, 255, 255, 0.7);
backdrop-filter: blur(10px);
border-radius: 12px;
box-shadow: 0 4px 20px rgba(0, 0, 0, 0.05);
border: 1px solid rgba(226, 232, 240, 0.8);
```

---

## Cách áp dụng cho trang mới

### Bước 1: Import components
```vue
<script setup>
import AdminLayout from "@/components/AdminLayout.vue";
import AdminTableContainer from "@/components/AdminTableContainer.vue";
</script>
```

### Bước 2: Wrap với AdminLayout
```vue
<template>
  <AdminLayout>
    <!-- Nội dung -->
  </AdminLayout>
</template>
```

### Bước 3: Sử dụng AdminTableContainer (nếu có bảng)
```vue
<AdminTableContainer
  title="Tiêu đề trang"
  buttonText="Thêm mới"
  :has-active-filters="hasFilters"
  :filter-results="resultsText"
  @add="openModal"
  @refresh="resetFilters"
>
  <template #filters>...</template>
  <template #tableHeader>...</template>
  <template #tableBody>...</template>
</AdminTableContainer>
```

### Bước 4: Sử dụng CSS classes global
```html
<!-- Filter -->
<div class="filterBar">
  <div class="filterGroup filterSearch">
    <svg class="filterIcon">...</svg>
    <input class="filterInput" placeholder="Tìm kiếm...">
  </div>
</div>

<!-- Action buttons -->
<div class="actionButtons">
  <button class="actionBtn actionEditBtn">✏️</button>
  <button class="actionBtn actionDeleteBtn">🗑️</button>
</div>

<!-- Status badge -->
<span class="statusBadge statusDelivered">Đã giao</span>
```

---

## Lưu ý quan trọng

1. **Giữ nguyên logic:** Tất cả các hàm xử lý API, state, computed đều giữ nguyên
2. **Chỉ thay đổi UI:** Template và CSS classes là phần thay đổi chính
3. **Responsive:** Tất cả các trang đều responsive cho mobile/tablet
4. **Accessibility:** Sử dụng semantic HTML và ARIA labels
5. **Performance:** Debounced search (300ms) để giảm số lần gọi API

---

## Kiểm tra sau refactor

- [ ] Layout hiển thị đúng với AdminLayout
- [ ] Filter bar nằm ngang dưới tiêu đề
- [ ] Bảng có glassmorphism effect
- [ ] Nút Sửa/Xóa là icon buttons
- [ ] Badge trạng thái bo tròn đúng màu
- [ ] Modal có animation và glass effect
- [ ] Responsive trên mobile
- [ ] Không có lỗi console
