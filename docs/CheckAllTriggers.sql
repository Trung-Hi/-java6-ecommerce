-- ============================================
-- CHECK: Tất cả trigger trong database
-- ============================================

-- 1. Tất cả trigger và bảng liên quan
SELECT 
    t.name AS trigger_name,
    OBJECT_NAME(t.parent_id) AS table_name,
    CASE WHEN t.is_disabled = 1 THEN 'Disabled' ELSE 'Enabled' END AS status
FROM sys.triggers t
WHERE t.parent_id > 0
ORDER BY table_name, trigger_name;

-- 2. Check DEFAULT constraint trên Product.quantity
SELECT 
    t.name AS table_name,
    c.name AS column_name,
    dc.definition AS default_value
FROM sys.tables t
INNER JOIN sys.columns c ON t.object_id = c.object_id
LEFT JOIN sys.default_constraints dc ON c.default_object_id = dc.object_id
WHERE (t.name = 'Product' OR t.name = 'products')
AND c.name = 'quantity';

-- 3. Xem sản phẩm mới nhất (vừa tạo)
SELECT TOP 3 id, name, quantity, description, create_date
FROM Product 
ORDER BY id DESC;
