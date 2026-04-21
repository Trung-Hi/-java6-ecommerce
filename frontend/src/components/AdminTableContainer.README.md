# AdminTableContainer Component

Component dùng chung cho các trang Admin có bảng dữ liệu. Dựa trên thiết kế từ trang Account.

## Props

| Prop | Type | Default | Mô tả |
|------|------|---------|-------|
| `title` | String | required | Tiêu đề trang (ví dụ: "Quản lý sản phẩm") |
| `buttonText` | String | "Thêm mới" | Chữ hiển thị trên nút thêm |
| `loading` | Boolean | false | Trạng thái loading |
| `isEmpty` | Boolean | false | Có đang hiển thị empty state không |
| `emptyMessage` | String | "Không có dữ liệu" | Tin nhắn khi không có dữ liệu |
| `hasActiveFilters` | Boolean | false | Có đang áp dụng filter không |
| `filterResults` | String | "" | Text hiển thị số kết quả (hỗ trợ HTML) |

## Slots

| Slot | Mô tả |
|------|-------|
| `filters` | Khu vực chứa các ô tìm kiếm và dropdown |
| `tableHeader` | Các đầu cột của bảng (`<tr>`) |
| `tableBody` | Nội dung các dòng bảng (`<tr>`) |
| `pagination` | Phân trang (tùy chọn) |

## Events

| Event | Mô tả |
|-------|-------|
| `add` | Click nút thêm mới |
| `refresh` | Click nút làm mới filter |

## Ví dụ sử dụng

### Trang Sản phẩm

```vue
<template>
  <div class="productPage">
    <AdminTableContainer
      title="Quản lý sản phẩm"
      buttonText="Thêm sản phẩm"
      :has-active-filters="hasActiveFilters"
      :filter-results="filterResultsText"
      :is-empty="isFilterEmpty"
      @add="openCreateModal"
      @refresh="resetFilters"
    >
      <!-- Filter Slot -->
      <template #filters>
        <div class="filterBar">
          <div class="filterGroup filterSearch">
            <svg class="filterIcon" ...>
              <!-- Search icon -->
            </svg>
            <input v-model="searchQuery" class="filterInput" placeholder="Tìm sản phẩm...">
          </div>
          <div class="filterGroup">
            <select v-model="filterCategory" class="filterSelect">
              <option value="">Tất cả danh mục</option>
              ...
            </select>
          </div>
        </div>
      </template>
      
      <!-- Table Header Slot -->
      <template #tableHeader>
        <tr>
          <th style="width: 80px;">Ảnh</th>
          <th>Tên</th>
          <th>Giá</th>
          <th>Thao tác</th>
        </tr>
      </template>
      
      <!-- Table Body Slot -->
      <template #tableBody>
        <tr v-for="product in products" :key="product.id">
          <td><img :src="product.image"></td>
          <td>{{ product.name }}</td>
          <td>{{ formatCurrency(product.price) }}</td>
          <td>
            <div class="actionButtons">
              <button class="actionBtn actionEditBtn" @click="edit(product)">✏️</button>
              <button class="actionBtn actionDeleteBtn" @click="delete(product)">🗑️</button>
            </div>
          </td>
        </tr>
      </template>
      
      <!-- Pagination Slot (tùy chọn) -->
      <template #pagination>
        <div class="paginationWrapper">
          <button @click="prevPage">Trước</button>
          <button v-for="p in pages" :key="p">{{ p }}</button>
          <button @click="nextPage">Sau</button>
        </div>
      </template>
    </AdminTableContainer>
  </div>
</template>

<script setup>
import AdminTableContainer from "@/components/AdminTableContainer.vue";
// ... logic của trang
</script>

<style scoped>
.productPage {
  padding: 24px;
}
</style>
```

## CSS Classes có sẵn (Global)

Component cung cấp các CSS classes dùng chung:

### Action Buttons
```css
.actionButtons      /* Container flex cho các nút */
.actionBtn          /* Base style cho nút icon 34x34px */
.actionEditBtn      /* Nút sửa: bg slate-100, hover blue */
.actionDeleteBtn    /* Nút xóa: bg red-50, hover red */
.actionResetBtn     /* Nút reset: bg blue-50, hover blue */
```

### Filter Bar
```css
.filterBar          /* Container glassmorphism */
.filterGroup        /* Group cho từng filter item */
.filterSearch       /* Search input container */
.filterIcon         /* Icon trong input */
.filterInput        /* Input style với icon */
.filterSelect       /* Select dropdown style */
.filterResetBtn     /* Nút reset filter */
```

### Badges
```css
.roleBadge          /* Badge vai trò */
.roleBadgeAdmin     /* Badge Admin: gradient amber */
.roleBadgeUser      /* Badge User: gradient blue */
.statusBadge        /* Badge trạng thái */
```

### Pagination
```css
.paginationWrapper   /* Container căn giữa */
.pagination         /* List flex */
.pageItem           /* Item phân trang */
.pageItem.active    /* Item đang active */
.pageLink           /* Button link */
```

## Design System

### Card Container
- Background: `rgba(255, 255, 255, 0.85)`
- Border radius: `16px`
- Box shadow: `0 10px 40px -10px rgba(0,0,0,0.1)`
- Backdrop filter: `blur(8px)`
- Border: `1px solid rgba(255,255,255,0.5)`

### Table
- Font size: `0.875rem` (14px)
- Header font size: `0.75rem` (12px), uppercase, tracking 0.5px
- Cell padding: `14px 12px`
- Row hover: `rgba(241, 245, 249, 0.5)`
- Border: `1px solid #f1f5f9`

### Filter Bar (Glassmorphism)
- Background: `rgba(255, 255, 255, 0.7)`
- Backdrop filter: `blur(10px)`
- Border radius: `12px`
- Box shadow: `0 4px 20px rgba(0,0,0,0.05)`

### Action Buttons
- Size: `34px x 34px`
- Border radius: `8px`
- Hover: scale và đổi màu background
