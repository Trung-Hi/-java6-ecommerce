import { useState, useEffect, forwardRef, useImperativeHandle } from 'react';

/**
 * CurrencyInput Component - VND Format
 * 
 * Features:
 * - Format hiển thị: 1.200.000 ₫ (chuẩn Việt Nam)
 * - Intl.NumberFormat('vi-VN')
 * - Focus: hiện raw number, Blur: format
 * - Chỉ cho nhập số (0-9)
 * - State lưu number, không chứa ký tự định dạng
 * - Không bị nhảy cursor khi nhập
 */

const CurrencyInput = forwardRef(({
  value,
  onChange,
  placeholder = 'Nhập giá',
  min = 0,
  max,
  disabled = false,
  required = false,
  className = '',
  name,
  id,
  ...props
}, ref) => {
  // Internal state for display value
  const [displayValue, setDisplayValue] = useState('');
  const [isFocused, setIsFocused] = useState(false);
  const [error, setError] = useState(null);

  // Format number to VND string
  const formatVND = (number) => {
    if (number === '' || number === null || number === undefined || isNaN(number)) {
      return '';
    }
    return new Intl.NumberFormat('vi-VN', {
      style: 'currency',
      currency: 'VND',
      minimumFractionDigits: 0,
      maximumFractionDigits: 0
    }).format(number);
  };

  // Extract only digits from string
  const extractDigits = (str) => {
    return str.replace(/\D/g, '');
  };

  // Convert string digits to number
  const digitsToNumber = (digits) => {
    if (!digits) return null;
    const num = parseInt(digits, 10);
    return isNaN(num) ? null : num;
  };

  // Validate value
  const validate = (num) => {
    if (num === null || num === undefined) {
      if (required) {
        setError('Vui lòng nhập giá');
        return false;
      }
      setError(null);
      return true;
    }

    if (num < min) {
      setError(`Giá tối thiểu là ${formatVND(min)}`);
      return false;
    }

    if (max !== undefined && num > max) {
      setError(`Giá tối đa là ${formatVND(max)}`);
      return false;
    }

    setError(null);
    return true;
  };

  // Initialize display value from props
  useEffect(() => {
    if (!isFocused) {
      if (value === null || value === undefined || value === '') {
        setDisplayValue('');
      } else {
        setDisplayValue(formatVND(value));
      }
    }
  }, [value, isFocused]);

  // Handle input change (while typing)
  const handleChange = (e) => {
    const inputValue = e.target.value;
    
    // Extract only digits
    const digits = extractDigits(inputValue);
    
    // Convert to number
    const number = digitsToNumber(digits);
    
    // Update display (raw while focused)
    setDisplayValue(digits);
    
    // Call onChange with number
    onChange?.(number);
    
    // Clear error while typing
    if (error) {
      validate(number);
    }
  };

  // Handle focus
  const handleFocus = (e) => {
    setIsFocused(true);
    
    // Show raw number on focus
    if (value !== null && value !== undefined && !isNaN(value)) {
      setDisplayValue(String(value));
    }
    
    props.onFocus?.(e);
  };

  // Handle blur
  const handleBlur = (e) => {
    setIsFocused(false);
    
    // Format display value
    if (value !== null && value !== undefined && !isNaN(value)) {
      setDisplayValue(formatVND(value));
    } else {
      setDisplayValue('');
    }
    
    // Validate on blur
    validate(value);
    
    props.onBlur?.(e);
  };

  // Handle key down - prevent non-digit characters
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
    if (!/\d/.test(e.key)) {
      e.preventDefault();
    }
    
    props.onKeyDown?.(e);
  };

  // Handle paste - filter only digits
  const handlePaste = (e) => {
    e.preventDefault();
    const pastedText = e.clipboardData.getData('text');
    const digits = extractDigits(pastedText);
    const number = digitsToNumber(digits);
    
    onChange?.(number);
    setDisplayValue(digits);
    
    props.onPaste?.(e);
  };

  // Expose methods via ref
  useImperativeHandle(ref, () => ({
    validate: () => validate(value),
    getFormattedValue: () => formatVND(value),
    getRawValue: () => value,
    setError: (msg) => setError(msg),
    clearError: () => setError(null)
  }));

  return (
    <div className={`currency-input-wrapper ${className}`}>
      <div className="relative">
        <input
          type="text"
          inputMode="numeric"
          pattern="[0-9]*"
          id={id}
          name={name}
          value={displayValue}
          onChange={handleChange}
          onFocus={handleFocus}
          onBlur={handleBlur}
          onKeyDown={handleKeyDown}
          onPaste={handlePaste}
          placeholder={placeholder}
          disabled={disabled}
          required={required}
          className={`
            w-full
            px-4
            py-2.5
            pr-10
            text-right
            text-gray-900
            bg-white
            border
            rounded-lg
            focus:ring-2
            focus:ring-blue-500
            focus:border-blue-500
            transition-all
            duration-200
            outline-none
            placeholder:text-gray-400
            disabled:bg-gray-100
            disabled:cursor-not-allowed
            ${error ? 'border-red-500 focus:ring-red-500 focus:border-red-500' : 'border-gray-300'}
          `}
          {...props}
        />
        {/* Suffix ₫ */}
        <span className="absolute right-3 top-1/2 -translate-y-1/2 text-gray-500 font-medium select-none pointer-events-none">
          ₫
        </span>
      </div>
      
      {/* Error message */}
      {error && (
        <p className="mt-1.5 text-sm text-red-600 flex items-center gap-1">
          <svg className="w-4 h-4" fill="currentColor" viewBox="0 0 20 20">
            <path fillRule="evenodd" d="M18 10a8 8 0 11-16 0 8 8 0 0116 0zm-7 4a1 1 0 11-2 0 1 1 0 012 0zm-1-9a1 1 0 00-1 1v4a1 1 0 102 0V6a1 1 0 00-1-1z" clipRule="evenodd" />
          </svg>
          {error}
        </p>
      )}
      
      {/* Hint text */}
      {!error && (
        <p className="mt-1 text-xs text-gray-400">
          Nhập số, tự động định dạng VNĐ
        </p>
      )}
    </div>
  );
});

CurrencyInput.displayName = 'CurrencyInput';

export default CurrencyInput;
