-- ============================================
-- CHECK: Sản phẩm 188 có quantity bao nhiêu?
-- ============================================

-- 1. Xem chi tiết sản phẩm 188
SELECT id, name, quantity, price, available, create_date, description
FROM products
WHERE name LIKE '%188%'
ORDER BY id DESC;

-- 2. Kiểm tra có ProductVariant nào cho sản phẩm này không (nếu có)
SELECT * FROM ProductVariant WHERE product_id = 
    (SELECT id FROM products WHERE name = 'Sản phẩm 188' AND create_date >= DATEADD(MINUTE, -10, GETDATE()));

-- 3. Hoặc lấy theo ID mới nhất
SELECT TOP 1 * FROM products ORDER BY id DESC;
