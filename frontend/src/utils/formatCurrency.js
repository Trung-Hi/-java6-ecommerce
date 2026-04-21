/**
 * Format currency helper - Chuẩn hóa định dạng tiền tệ VND
 * Format: 100.000 đ (chữ "đ" nhỏ và nhạt hơn)
 * @param {number|string} value - Giá trị cần format
 * @param {object} options - Tùy chọn
 * @returns {string} Chuỗi đã format
 */
export const formatCurrency = (value, options = {}) => {
    const { 
        showSymbol = true,      // Hiển thị ký hiệu đ
        symbolSmall = true,     // Chữ đ nhỏ hơn
        symbolLight = true,     // Chữ đ nhạt hơn
        fallback = '-'          // Giá trị mặc định khi null/undefined
    } = options;
    
    if (value === null || value === undefined || value === '') {
        return fallback;
    }
    
    const num = parseInt(value) || 0;
    const formatted = new Intl.NumberFormat('vi-VN').format(num);
    
    if (!showSymbol) {
        return formatted;
    }
    
    // Style cho chữ "đ"
    let symbolStyle = '';
    if (symbolSmall) {
        symbolStyle += 'font-size: 0.85rem; ';
    }
    if (symbolLight) {
        symbolStyle += 'color: #888; ';
    }
    
    return symbolStyle 
        ? `${formatted} <small style="${symbolStyle.trim()}">đ</small>`
        : `${formatted} đ`;
};

/**
 * Format giá gốc (có gạch ngang)
 */
export const formatOldPrice = (value) => {
    return formatCurrency(value, { 
        symbolSmall: true, 
        symbolLight: true 
    });
};

/**
 * Format giá hiện tại (nổi bật hơn)
 */
export const formatCurrentPrice = (value) => {
    return formatCurrency(value, { 
        symbolSmall: false, 
        symbolLight: false 
    });
};
