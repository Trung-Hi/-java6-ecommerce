<template>
  <div class="max-w-2xl mx-auto p-6">
    <h2 class="text-xl font-bold mb-6">CurrencyInput Vue Demo</h2>
    
    <!-- Demo Section -->
    <div class="space-y-4 mb-8">
      <div>
        <label class="block text-sm font-medium text-gray-700 mb-2">
          Test Currency Input
        </label>
        <CurrencyInput
          v-model="demoPrice"
          placeholder="Nhập số tiền"
          :min="1000"
        />
      </div>
      
      <div class="bg-blue-50 p-4 rounded-lg">
        <p class="text-sm text-gray-600">State value (number):</p>
        <p class="text-lg font-mono font-bold text-blue-600">
          {{ demoPrice !== null ? demoPrice : 'null' }}
        </p>
        
        <p class="text-sm text-gray-600 mt-2">Formatted display:</p>
        <p class="text-lg font-bold text-green-600">
          {{ demoPrice !== null ? formatDisplay(demoPrice) : '-' }}
        </p>
      </div>
      
      <div class="flex gap-2">
        <button
          @click="demoPrice = 1200000"
          class="px-4 py-2 bg-gray-200 rounded hover:bg-gray-300 text-sm"
        >
          Set 1.200.000₫
        </button>
        <button
          @click="$refs.demoInput?.validate()"
          class="px-4 py-2 bg-gray-200 rounded hover:bg-gray-300 text-sm"
        >
          Validate
        </button>
      </div>
    </div>
    
    <hr class="my-8" />
    
    <!-- Product Form Example -->
    <h3 class="text-lg font-bold mb-4">Product Form Example</h3>
    
    <form @submit.prevent="handleSubmit" class="space-y-6">
      <!-- Product Name -->
      <div>
        <label class="block text-sm font-medium text-gray-700 mb-2">
          Tên sản phẩm <span class="text-red-500">*</span>
        </label>
        <input
          v-model="formData.name"
          type="text"
          name="name"
          placeholder="Nhập tên sản phẩm"
          class="w-full px-4 py-2.5 border border-gray-300 rounded-lg focus:ring-2 focus:ring-blue-500 focus:border-blue-500"
          required
        />
      </div>

      <!-- Price - Using CurrencyInput -->
      <div>
        <label class="block text-sm font-medium text-gray-700 mb-2">
          Giá bán <span class="text-red-500">*</span>
        </label>
        <CurrencyInput
          ref="priceInput"
          v-model="formData.price"
          placeholder="Nhập giá bán"
          :min="0"
          required
        />
        <p class="mt-1 text-sm text-gray-500">
          Raw value in state: {{ formData.price !== null ? formData.price : 'null' }}
        </p>
      </div>

      <!-- Discount Price - Using CurrencyInput -->
      <div>
        <label class="block text-sm font-medium text-gray-700 mb-2">
          Giá khuyến mãi
        </label>
        <CurrencyInput
          ref="discountInput"
          v-model="formData.discountPrice"
          placeholder="Nhập giá khuyến mãi (nếu có)"
          :min="0"
          :max="formData.price"
        />
      </div>

      <!-- Description -->
      <div>
        <label class="block text-sm font-medium text-gray-700 mb-2">
          Mô tả
        </label>
        <textarea
          v-model="formData.description"
          name="description"
          rows="4"
          placeholder="Nhập mô tả sản phẩm"
          class="w-full px-4 py-2.5 border border-gray-300 rounded-lg focus:ring-2 focus:ring-blue-500 focus:border-blue-500"
        />
      </div>

      <!-- Preview -->
      <div class="bg-gray-50 p-4 rounded-lg">
        <h4 class="font-medium text-gray-700 mb-2">Preview dữ liệu gửi lên:</h4>
        <pre class="text-sm text-gray-600 bg-white p-3 rounded border overflow-x-auto">{{ previewData }}</pre>
      </div>

      <!-- Submit Buttons -->
      <div class="flex gap-4">
        <button
          type="submit"
          :disabled="isSubmitting"
          class="flex-1 py-2.5 px-6 bg-blue-600 text-white font-medium rounded-lg hover:bg-blue-700 disabled:bg-gray-400 disabled:cursor-not-allowed transition-colors"
        >
          {{ isSubmitting ? 'Đang lưu...' : 'Lưu sản phẩm' }}
        </button>
        <button
          type="button"
          @click="resetForm"
          class="py-2.5 px-6 border border-gray-300 text-gray-700 font-medium rounded-lg hover:bg-gray-50 transition-colors"
        >
          Làm mới
        </button>
      </div>
    </form>
  </div>
</template>

<script setup>
import { ref, computed } from 'vue';
import CurrencyInput from './CurrencyInput.vue';

// Demo price
const demoPrice = ref(null);
const demoInput = ref(null);

// Form data
const formData = ref({
  name: '',
  price: null,        // Stored as number: 1200000
  discountPrice: null,
  description: ''
});

const isSubmitting = ref(false);
const priceInput = ref(null);
const discountInput = ref(null);

// Format for display
const formatDisplay = (number) => {
  if (!number) return '';
  return new Intl.NumberFormat('vi-VN', {
    style: 'currency',
    currency: 'VND'
  }).format(number);
};

// Preview data
const previewData = computed(() => {
  return JSON.stringify({
    name: formData.value.name,
    price: formData.value.price,
    discountPrice: formData.value.discountPrice,
    priceFormatted: formatDisplay(formData.value.price),
    discountFormatted: formatDisplay(formData.value.discountPrice)
  }, null, 2);
});

// Validate form
const validateForm = () => {
  const isPriceValid = priceInput.value?.validate();
  const isDiscountValid = discountInput.value?.validate();
  
  if (!isPriceValid || !isDiscountValid) {
    return false;
  }
  
  if (!formData.value.name.trim()) {
    alert('Vui lòng nhập tên sản phẩm');
    return false;
  }
  
  return true;
};

// Handle form submission
const handleSubmit = async () => {
  if (!validateForm()) {
    return;
  }
  
  isSubmitting.value = true;
  
  try {
    // Data sent to backend - price is NUMBER
    const payload = {
      name: formData.value.name,
      price: formData.value.price,              // 1200000 (number)
      discountPrice: formData.value.discountPrice,
      description: formData.value.description
    };
    
    console.log('Submitting to backend:', payload);
    // Example: await api.post('/products', payload);
    
    alert('Form submitted! Check console for data.');
    
  } catch (error) {
    console.error('Submit error:', error);
  } finally {
    isSubmitting.value = false;
  }
};

// Reset form
const resetForm = () => {
  formData.value = {
    name: '',
    price: null,
    discountPrice: null,
    description: ''
  };
  priceInput.value?.clearError();
  discountInput.value?.clearError();
};
</script>
