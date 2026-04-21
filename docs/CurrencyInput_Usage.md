# CurrencyInput Component - Documentation

## 📁 Files Created

| File | Description |
|------|-------------|
| `src/components/CurrencyInput.jsx` | React version |
| `src/components/CurrencyInput.vue` | Vue 3 version |
| `src/components/CurrencyInput.example.jsx` | React example |
| `src/components/CurrencyInput.example.vue` | Vue example |
| `src/views/admin/AdminProductView.vue` | **Updated** with CurrencyInput |

---

## ✨ Features

| Feature | Description |
|---------|-------------|
| **Format VND** | 1.200.000 ₫ (Intl.NumberFormat('vi-VN')) |
| **Input Behavior** | Chỉ nhập số (0-9), auto filter ký tự khác |
| **Focus** | Hiện raw number (1200000) |
| **Blur** | Format lại (1.200.000 ₫) |
| **State** | Lưu number, không chứa ký tự định dạng |
| **Submit** | Gửi number lên backend (1200000) |
| **UI** | Suffix ₫, text-right, Tailwind styling |
| **Validation** | Min/Max, required, error message |

---

## 🎯 Usage (Vue 3)

```vue
<template>
  <form @submit.prevent="submit">
    <!-- Basic usage -->
    <CurrencyInput
      v-model="price"
      placeholder="Nhập giá"
    />
    
    <!-- With validation -->
    <CurrencyInput
      v-model="price"
      placeholder="Giá bán"
      :min="1000"
      :max="10000000"
      required
    />
  </form>
</template>

<script setup>
import { ref } from 'vue';
import CurrencyInput from '@/components/CurrencyInput.vue';

const price = ref(null);  // Stores: 1200000 (number)

const submit = () => {
  // Send to backend
  console.log(price.value);  // 1200000 (number)
};
</script>
```

---

## 🎯 Usage (React)

```jsx
import { useState } from 'react';
import CurrencyInput from '@/components/CurrencyInput';

function ProductForm() {
  const [price, setPrice] = useState(null);
  
  const handleSubmit = () => {
    console.log(price);  // 1200000 (number)
  };
  
  return (
    <form onSubmit={handleSubmit}>
      <CurrencyInput
        value={price}
        onChange={setPrice}
        placeholder="Nhập giá"
        min={1000}
        max={10000000}
        required
      />
    </form>
  );
}
```

---

## 📋 Props

| Prop | Type | Default | Description |
|------|------|---------|-------------|
| `v-model` / `value` | Number/null | null | Giá trị số |
| `placeholder` | String | 'Nhập giá' | Placeholder text |
| `min` | Number | 0 | Giá tối thiểu |
| `max` | Number | undefined | Giá tối đa |
| `disabled` | Boolean | false | Disable input |
| `required` | Boolean | false | Required field |
| `name` | String | '' | Input name |
| `id` | String | auto | Input id |

---

## 🔧 Exposed Methods (Vue ref)

```javascript
const inputRef = ref(null);

// Validate
inputRef.value?.validate();

// Get formatted value
inputRef.value?.getFormattedValue();  // "1.200.000 ₫"

// Get raw value
inputRef.value?.getRawValue();  // 1200000

// Set/Clear error
inputRef.value?.setError('Giá quá cao');
inputRef.value?.clearError();
```

---

## 🎨 UI Preview

### Focus State
```
┌─────────────────────────┐
│ 1,200,000             ₫ │  ← Raw number
└─────────────────────────┘
```

### Blur State
```
┌─────────────────────────┐
│ 1.200.000 ₫           ₫ │  ← Formatted
└─────────────────────────┘
```

### With Error
```
┌─────────────────────────┐
│                     ₫ │  ← Empty
└─────────────────────────┘
⚠ Giá tối thiểu là 1.000 ₫
```

---

## 🧪 Demo

### React
```bash
# In your React component
import CurrencyInputDemo from '@/components/CurrencyInput.example';

function App() {
  return <CurrencyInputDemo />;
}
```

### Vue
```vue
<template>
  <CurrencyInputExample />
</template>

<script setup>
import CurrencyInputExample from '@/components/CurrencyInput.example.vue';
</script>
```

---

## 🚀 Integration in Product Form

CurrencyInput đã được tích hợp vào `AdminProductView.vue`:

1. **Form tạo/sửa sản phẩm** - Giá bán chính
2. **Variant Manager** - Giá điều chỉnh cho từng biến thể

### Code Integration
```vue
<!-- Giá sản phẩm chính -->
<CurrencyInput
  v-model="form.price"
  placeholder="Nhập giá bán"
  :min="0"
  required
/>

<!-- Giá điều chỉnh variant -->
<CurrencyInput
  v-model="variantForm.priceAdjustment"
  placeholder="VD: 5000 hoặc -5000"
/>
```

---

## ✅ Testing

### Test Cases

| Action | Input | Expected |
|--------|-------|----------|
| Type number | 1200000 | Shows "1200000" (focus) |
| Blur | 1200000 | Shows "1.200.000 ₫" |
| Type letters | abc | Ignored, shows empty |
| Paste mixed | 1,200,000abc | Shows "1200000" |
| Type non-digit | Space, -, . | Ignored |
| Submit | - | Sends `1200000` (number) |

---

## 🎓 Key Implementation Details

### Không dùng string replace
```javascript
// ❌ Không làm thế này
value.replace(',', '.')

// ✅ Dùng Intl.NumberFormat
new Intl.NumberFormat('vi-VN', {
  style: 'currency',
  currency: 'VND'
}).format(number)
```

### State là number
```javascript
// ❌ Không lưu string
const price = "1.200.000 ₫"

// ✅ Lưu number
const price = 1200000
```

### Cursor không nhảy
```javascript
// Giữ cursor ở cuối khi nhập
input.setSelectionRange(cursorPosition, cursorPosition);
```

---

## 🔄 Data Flow

```
User Input: 1200000
     ↓
[Filter: chỉ giữ số]
     ↓
State: 1200000 (number)
     ↓
Display (focus): "1200000"
Display (blur): "1.200.000 ₫"
     ↓
Submit to API: { price: 1200000 }
```

---

## 📱 Responsive

Component tự động responsive với Tailwind classes:
- Mobile: Full width
- Tablet: Adaptive
- Desktop: Max width container

---

Ready to use! 🎉
