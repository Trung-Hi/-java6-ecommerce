<template>
  <div class="currency-input-wrapper">
    <!-- Unified input block with suffix -->
    <div class="currency-input-block" :class="{ error: error }">
      <input
        type="text"
        :id="id"
        :name="name"
        :value="displayValue"
        @input="handleInput"
        @focus="handleFocus"
        @blur="handleBlur"
        @keydown="handleKeyDown"
        @paste="handlePaste"
        placeholder="0"
        :disabled="disabled"
        :required="required"
        class="currency-input"
        autocomplete="off"
        autocorrect="off"
        autocapitalize="off"
        spellcheck="false"
      />
      <!-- Suffix đ - integrated as part of input block -->
      <span class="currency-suffix"><small>đ</small></span>
    </div>
    
    <!-- Compact hint/error text -->
    <p v-if="error" class="currency-hint currency-hint-error">
      <svg class="hint-icon" fill="currentColor" viewBox="0 0 20 20">
        <path fill-rule="evenodd" d="M18 10a8 8 0 11-16 0 8 8 0 0116 0zm-7 4a1 1 0 11-2 0 1 1 0 012 0zm-1-9a1 1 0 00-1 1v4a1 1 0 102 0V6a1 1 0 00-1-1z" clip-rule="evenodd" />
      </svg>
      {{ error }}
    </p>
    <p v-else class="currency-hint">
      Nhập số, tự động định dạng <small>đ</small>
    </p>
  </div>
</template>

<script setup>
import { ref, computed, watch, nextTick } from 'vue';

/**
 * CurrencyInput Vue Component - VND Format
 * 
 * Features:
 * - Format hiển thị: 1.200.000 ₫ (chuẩn Việt Nam)
 * - Intl.NumberFormat('vi-VN')
 * - Focus: hiện raw number, Blur: format
 * - Chỉ cho nhập số (0-9)
 * - State lưu number, không chứa ký tự định dạng
 * - Không bị nhảy cursor khi nhập
 */

const props = defineProps({
  modelValue: {
    type: [Number, String, null],
    default: null,
    validator(value) {
      return value === null || value === '' || typeof value === 'number' || typeof value === 'string';
    }
  },
  placeholder: {
    type: String,
    default: 'Nhập giá'
  },
  min: {
    type: Number,
    default: 0
  },
  max: {
    type: Number,
    default: undefined
  },
  disabled: {
    type: Boolean,
    default: false
  },
  required: {
    type: Boolean,
    default: false
  },
  name: {
    type: String,
    default: ''
  },
  id: {
    type: String,
    default: () => `currency-${Math.random().toString(36).substr(2, 9)}`
  }
});

const emit = defineEmits(['update:modelValue', 'change', 'focus', 'blur']);

// Internal state
const isFocused = ref(false);
const error = ref(null);
const internalValue = ref('');

// Format number to VND string (without ₫ symbol - shown separately in suffix)
const formatVND = (number) => {
  if (number === '' || number === null || number === undefined || isNaN(number)) {
    return '';
  }
  // Use decimal format instead of currency to avoid the ₫ symbol
  return new Intl.NumberFormat('vi-VN', {
    style: 'decimal',
    minimumFractionDigits: 0,
    maximumFractionDigits: 0
  }).format(number);
};

// Extract digits only (no negative numbers for currency)
const extractDigits = (str) => {
  // Remove all non-digit characters
  return str.replace(/[^\d]/g, '');
};

// Convert string to number
const digitsToNumber = (digits) => {
  if (!digits) return null;
  const num = parseInt(digits, 10);
  return isNaN(num) ? null : num;
};

// Format number with đ for display in error messages
const formatVNDWithSymbol = (number) => {
  if (!number && number !== 0) return '';
  return new Intl.NumberFormat('vi-VN').format(number) + ' <small>đ</small>';
};

// Validate value
const validate = () => {
  const num = props.modelValue;
  
  if (num === null || num === undefined) {
    if (props.required) {
      error.value = 'Vui lòng nhập giá';
      return false;
    }
    error.value = null;
    return true;
  }

  if (num < props.min) {
    error.value = `Giá tối thiểu là ${formatVNDWithSymbol(props.min)}`;
    return false;
  }

  if (props.max !== undefined && num > props.max) {
    error.value = `Giá tối đa là ${formatVNDWithSymbol(props.max)}`;
    return false;
  }

  error.value = null;
  return true;
};

// Computed display value
const displayValue = computed(() => {
  if (isFocused.value) {
    // Show raw number when focused
    if (props.modelValue !== null && props.modelValue !== undefined && !isNaN(props.modelValue)) {
      return String(props.modelValue);
    }
    return internalValue.value;
  } else {
    // Show formatted when blurred
    if (props.modelValue !== null && props.modelValue !== undefined && !isNaN(props.modelValue)) {
      return formatVND(props.modelValue);
    }
    return '';
  }
});

// Handle input event
const handleInput = (e) => {
  const inputValue = e.target.value;
  
  // Extract only digits
  const digits = extractDigits(inputValue);
  
  // Convert to number
  const number = digitsToNumber(digits);
  
  // Update internal value for display while focused
  internalValue.value = digits;
  
  // Emit update
  emit('update:modelValue', number);
  emit('change', number);
  
  // Clear error while typing
  if (error.value) {
    validate();
  }
  
  // Update cursor position
  nextTick(() => {
    const input = e.target;
    const cursorPosition = digits.length;
    input.setSelectionRange(cursorPosition, cursorPosition);
  });
};

// Handle focus
const handleFocus = (e) => {
  isFocused.value = true;
  
  // Show raw number on focus
  if (props.modelValue !== null && props.modelValue !== undefined && !isNaN(props.modelValue)) {
    internalValue.value = String(props.modelValue);
  }
  
  emit('focus', e);
};

// Handle blur
const handleBlur = (e) => {
  isFocused.value = false;
  
  // Clear internal value
  internalValue.value = '';
  
  // Validate on blur
  validate();
  
  emit('blur', e);
};

// Handle keydown - prevent non-digit characters
const handleKeyDown = (e) => {
  // Allow: backspace, delete, tab, escape, enter, arrows
  const allowedKeys = [
    'Backspace', 'Delete', 'Tab', 'Escape', 'Enter',
    'ArrowLeft', 'ArrowRight', 'Home', 'End'
  ];
  
  if (allowedKeys.includes(e.key)) {
    return;
  }
  
  // Allow Ctrl+A, Ctrl+C, Ctrl+V, Ctrl+X
  if ((e.ctrlKey || e.metaKey) && ['a', 'c', 'v', 'x'].includes(e.key.toLowerCase())) {
    return;
  }
  
  // Only allow digits
  if (!/[\d]/.test(e.key)) {
    e.preventDefault();
  }
};

// Handle paste - filter only digits
const handlePaste = (e) => {
  e.preventDefault();
  const pastedText = e.clipboardData.getData('text');
  const digits = extractDigits(pastedText);
  const number = digitsToNumber(digits);
  
  emit('update:modelValue', number);
  emit('change', number);
  
  internalValue.value = digits;
};

// Watch for external value changes
watch(() => props.modelValue, (newVal) => {
  // If value is a string with non-digit characters, extract and emit clean number
  if (typeof newVal === 'string') {
    const cleanNum = digitsToNumber(extractDigits(newVal));
    if (cleanNum !== null && cleanNum !== newVal) {
      emit('update:modelValue', cleanNum);
    }
  }
  
  if (!isFocused.value) {
    if (newVal === null || newVal === undefined || isNaN(newVal)) {
      internalValue.value = '';
    }
  }
}, { immediate: true });

// Expose methods for parent component
defineExpose({
  validate,
  getFormattedValue: () => formatVNDWithSymbol(props.modelValue),
  getRawValue: () => props.modelValue,
  setError: (msg) => { error.value = msg; },
  clearError: () => { error.value = null; }
});
</script>

<style scoped>
.currency-input-wrapper {
  width: 100%;
  display: flex;
  flex-direction: column;
}

/* Unified input block with border */
.currency-input-block {
  display: flex;
  align-items: center;
  background: white;
  border: 1px solid #d1d5db;
  border-radius: 0.5rem;
  overflow: hidden;
  transition: all 0.2s ease;
}

.currency-input-block:focus-within {
  border-color: #3b82f6;
  box-shadow: 0 0 0 2px rgba(59, 130, 246, 0.2);
}

/* Error state on the block */
.currency-input-block.error {
  border-color: #ef4444;
}

.currency-input-block.error:focus-within {
  box-shadow: 0 0 0 2px rgba(239, 68, 68, 0.2);
}

/* Input field - no border since parent has border */
.currency-input {
  flex: 1;
  padding: 0.625rem 0.75rem;
  padding-right: 0.5rem;
  border: none;
  outline: none;
  text-align: right;
  font-size: 0.9375rem;
  font-weight: 400;
  color: #111827;
  background: transparent;
  font-variant-numeric: tabular-nums;
  min-width: 0;
}

.currency-input::placeholder {
  color: #9ca3af;
  font-weight: 400;
}

.currency-input:disabled {
  background: #f3f4f6;
  cursor: not-allowed;
  color: #6b7280;
}

/* Suffix đ - part of the unified block */
.currency-suffix {
  padding: 0.625rem 0.75rem;
  padding-left: 0.25rem;
  font-size: 0.9375rem;
  font-weight: 600;
  color: #9ca3af; /* Lighter color */
  background: #f9fafb;
  border-left: 1px solid #e5e7eb;
  user-select: none;
}

.currency-suffix small {
  font-size: 0.75rem; /* Smaller đ */
  font-weight: 500;
}

/* Compact hint text below input */
.currency-hint {
  margin: 0;
  margin-top: 0.25rem;
  font-size: 0.75rem;
  line-height: 1.25;
  color: #9ca3af;
  display: flex;
  align-items: center;
  gap: 0.25rem;
  min-height: 1rem;
}

.currency-hint-error {
  color: #ef4444;
}

.hint-icon {
  width: 0.875rem;
  height: 0.875rem;
  flex-shrink: 0;
}

/* Remove text decoration and prevent any pseudo elements */
input {
  text-decoration: none !important;
}

input::before,
input::after {
  content: none !important;
  display: none !important;
}

/* Remove spinners from number input on browsers that show them */
input::-webkit-outer-spin-button,
input::-webkit-inner-spin-button {
  -webkit-appearance: none;
  margin: 0;
}

input[type=number] {
  -moz-appearance: textfield;
}
</style>
