-- ============================================
-- CHECK: Trigger trên bảng products (chữ thường)
-- ============================================

-- 1. Trigger trên bảng 'products'
SELECT 
    t.name AS trigger_name,
    CASE WHEN t.is_disabled = 1 THEN 'Disabled' ELSE 'Enabled' END AS status,
    m.definition AS trigger_sql
FROM sys.triggers t
INNER JOIN sys.tables tb ON t.parent_id = tb.object_id
INNER JOIN sys.sql_modules m ON t.object_id = m.object_id
WHERE tb.name = 'products';

-- 2. Xem 3 sản phẩm mới nhất trong 'products'
SELECT TOP 3 id, name, quantity, price, available, create_date
FROM products
ORDER BY id DESC;

-- 3. So sánh với bảng Product (nếu có dữ liệu)
SELECT TOP 3 id, name, quantity, price, available, create_date
FROM Product
ORDER BY id DESC;
