-- ============================================
-- CHECK: Tìm tất cả trigger trên bảng Product và ProductVariant
-- ============================================

-- 1. Tìm trigger trên bảng Product
SELECT 
    t.name AS trigger_name,
    OBJECT_NAME(t.parent_id) AS table_name,
    CASE WHEN t.is_disabled = 1 THEN 'Disabled' ELSE 'Enabled' END AS status,
    m.definition AS trigger_definition
FROM sys.triggers t
INNER JOIN sys.sql_modules m ON t.object_id = m.object_id
WHERE OBJECT_NAME(t.parent_id) = 'Product' 
   OR OBJECT_NAME(t.parent_id) = 'products'
   OR OBJECT_NAME(t.parent_id) LIKE '%Product%';

-- 2. Tìm trigger trên bảng ProductVariant  
SELECT 
    t.name AS trigger_name,
    OBJECT_NAME(t.parent_id) AS table_name,
    CASE WHEN t.is_disabled = 1 THEN 'Disabled' ELSE 'Enabled' END AS status,
    m.definition AS trigger_definition
FROM sys.triggers t
INNER JOIN sys.sql_modules m ON t.object_id = m.object_id
WHERE OBJECT_NAME(t.parent_id) = 'ProductVariant'
   OR OBJECT_NAME(t.parent_id) LIKE '%ProductVariant%';

-- 3. Xem cấu trúc bảng Product
SELECT 
    COLUMN_NAME,
    DATA_TYPE,
    IS_NULLABLE,
    COLUMN_DEFAULT
FROM INFORMATION_SCHEMA.COLUMNS
WHERE TABLE_NAME = 'Product' OR TABLE_NAME = 'products';

-- 4. Xem dữ liệu mẫu (sản phẩm mới nhất)
SELECT TOP 5 id, name, quantity, total_quantity 
FROM Product 
ORDER BY id DESC;
