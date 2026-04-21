/**
 * Example: Using CurrencyInput in Product Form
 * 
 * This demonstrates how to integrate CurrencyInput into a product form
 * with proper form handling and submission to backend.
 */

import { useState, useRef } from 'react';
import CurrencyInput from './CurrencyInput';

// Example Product Form Component
const ProductForm = ({ onSubmit, initialData = {} }) => {
  const [formData, setFormData] = useState({
    name: initialData.name || '',
    price: initialData.price || null,        // Stored as number: 1200000
    discountPrice: initialData.discountPrice || null,
    description: initialData.description || ''
  });
  
  const [isSubmitting, setIsSubmitting] = useState(false);
  const priceRef = useRef();
  const discountRef = useRef();

  // Handle regular input changes
  const handleInputChange = (e) => {
    const { name, value } = e.target;
    setFormData(prev => ({
      ...prev,
      [name]: value
    }));
  };

  // Handle currency input changes
  const handlePriceChange = (number) => {
    setFormData(prev => ({
      ...prev,
      price: number  // Stored as number: 1200000
    }));
  };

  const handleDiscountChange = (number) => {
    setFormData(prev => ({
      ...prev,
      discountPrice: number  // Stored as number
    }));
  };

  // Validate form
  const validateForm = () => {
    const isPriceValid = priceRef.current?.validate();
    const isDiscountValid = discountRef.current?.validate();
    
    if (!isPriceValid || !isDiscountValid) {
      return false;
    }
    
    if (!formData.name.trim()) {
      alert('Vui lòng nhập tên sản phẩm');
      return false;
    }
    
    return true;
  };

  // Handle form submission
  const handleSubmit = async (e) => {
    e.preventDefault();
    
    if (!validateForm()) {
      return;
    }
    
    setIsSubmitting(true);
    
    try {
      // Data sent to backend - price is NUMBER
      const payload = {
        name: formData.name,
        price: formData.price,              // 1200000 (number)
        discountPrice: formData.discountPrice,
        description: formData.description
      };
      
      console.log('Submitting to backend:', payload);
      // Example: await api.post('/products', payload);
      
      await onSubmit?.(payload);
      
    } catch (error) {
      console.error('Submit error:', error);
    } finally {
      setIsSubmitting(false);
    }
  };

  // Format for display (not needed in form, but useful for confirmation)
  const formatDisplay = (number) => {
    if (!number) return '';
    return new Intl.NumberFormat('vi-VN', {
      style: 'currency',
      currency: 'VND'
    }).format(number);
  };

  return (
    <form onSubmit={handleSubmit} className="space-y-6 max-w-2xl">
      {/* Product Name */}
      <div>
        <label className="block text-sm font-medium text-gray-700 mb-2">
          Tên sản phẩm <span className="text-red-500">*</span>
        </label>
        <input
          type="text"
          name="name"
          value={formData.name}
          onChange={handleInputChange}
          placeholder="Nhập tên sản phẩm"
          className="w-full px-4 py-2.5 border border-gray-300 rounded-lg focus:ring-2 focus:ring-blue-500 focus:border-blue-500"
          required
        />
      </div>

      {/* Price - Using CurrencyInput */}
      <div>
        <label className="block text-sm font-medium text-gray-700 mb-2">
          Giá bán <span className="text-red-500">*</span>
        </label>
        <CurrencyInput
          ref={priceRef}
          value={formData.price}
          onChange={handlePriceChange}
          placeholder="Nhập giá bán"
          min={0}
          required
        />
        <p className="mt-1 text-sm text-gray-500">
          Raw value in state: {formData.price !== null ? formData.price : 'null'}
        </p>
      </div>

      {/* Discount Price - Using CurrencyInput */}
      <div>
        <label className="block text-sm font-medium text-gray-700 mb-2">
          Giá khuyến mãi
        </label>
        <CurrencyInput
          ref={discountRef}
          value={formData.discountPrice}
          onChange={handleDiscountChange}
          placeholder="Nhập giá khuyến mãi (nếu có)"
          min={0}
          max={formData.price}  // Không cho nhập cao hơn giá gốc
        />
      </div>

      {/* Description */}
      <div>
        <label className="block text-sm font-medium text-gray-700 mb-2">
          Mô tả
        </label>
        <textarea
          name="description"
          value={formData.description}
          onChange={handleInputChange}
          rows={4}
          placeholder="Nhập mô tả sản phẩm"
          className="w-full px-4 py-2.5 border border-gray-300 rounded-lg focus:ring-2 focus:ring-blue-500 focus:border-blue-500"
        />
      </div>

      {/* Preview */}
      <div className="bg-gray-50 p-4 rounded-lg">
        <h4 className="font-medium text-gray-700 mb-2">Preview dữ liệu gửi lên:</h4>
        <pre className="text-sm text-gray-600 bg-white p-3 rounded border overflow-x-auto">
{JSON.stringify({
  name: formData.name,
  price: formData.price,
  discountPrice: formData.discountPrice,
  priceFormatted: formatDisplay(formData.price),
  discountFormatted: formatDisplay(formData.discountPrice)
}, null, 2)}
        </pre>
      </div>

      {/* Submit Buttons */}
      <div className="flex gap-4">
        <button
          type="submit"
          disabled={isSubmitting}
          className="flex-1 py-2.5 px-6 bg-blue-600 text-white font-medium rounded-lg hover:bg-blue-700 disabled:bg-gray-400 disabled:cursor-not-allowed transition-colors"
        >
          {isSubmitting ? 'Đang lưu...' : 'Lưu sản phẩm'}
        </button>
        <button
          type="button"
          onClick={() => {
            setFormData({
              name: '',
              price: null,
              discountPrice: null,
              description: ''
            });
            priceRef.current?.clearError();
            discountRef.current?.clearError();
          }}
          className="py-2.5 px-6 border border-gray-300 text-gray-700 font-medium rounded-lg hover:bg-gray-50 transition-colors"
        >
          Làm mới
        </button>
      </div>
    </form>
  );
};

// Demo component
const CurrencyInputDemo = () => {
  const [price, setPrice] = useState(null);
  const inputRef = useRef();

  return (
    <div className="p-8 max-w-md mx-auto">
      <h2 className="text-xl font-bold mb-6">CurrencyInput Demo</h2>
      
      <div className="space-y-4">
        <div>
          <label className="block text-sm font-medium text-gray-700 mb-2">
            Test Currency Input
          </label>
          <CurrencyInput
            ref={inputRef}
            value={price}
            onChange={setPrice}
            placeholder="Nhập số tiền"
            min={1000}
          />
        </div>
        
        <div className="bg-blue-50 p-4 rounded-lg">
          <p className="text-sm text-gray-600">State value (number):</p>
          <p className="text-lg font-mono font-bold text-blue-600">
            {price !== null ? price : 'null'}
          </p>
          
          <p className="text-sm text-gray-600 mt-2">Formatted display:</p>
          <p className="text-lg font-bold text-green-600">
            {price !== null 
              ? new Intl.NumberFormat('vi-VN', { style: 'currency', currency: 'VND' }).format(price)
              : '-'
            }
          </p>
        </div>
        
        <div className="flex gap-2">
          <button
            onClick={() => setPrice(1200000)}
            className="px-4 py-2 bg-gray-200 rounded hover:bg-gray-300 text-sm"
          >
            Set 1.200.000₫
          </button>
          <button
            onClick={() => inputRef.current?.validate()}
            className="px-4 py-2 bg-gray-200 rounded hover:bg-gray-300 text-sm"
          >
            Validate
          </button>
        </div>
      </div>
      
      <hr className="my-8" />
      
      <h3 className="text-lg font-bold mb-4">Product Form Example</h3>
      <ProductForm onSubmit={(data) => console.log('Form submitted:', data)} />
    </div>
  );
};

export { ProductForm, CurrencyInputDemo };
export default CurrencyInputDemo;
