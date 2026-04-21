-- ============================================
-- CHECK: Xem cấu trúc chính xác của bảng products
-- ============================================

-- 1. Xem tất cả bảng có tên giống Product
SELECT name FROM sys.tables WHERE name LIKE '%product%';

-- 2. Xem cột chi tiết của bảng products
SELECT 
    COLUMN_NAME,
    DATA_TYPE,
    IS_NULLABLE,
    COLUMN_DEFAULT
FROM INFORMATION_SCHEMA.COLUMNS
WHERE TABLE_NAME = 'products';

-- 3. Hoặc dùng sys.columns
SELECT 
    c.name AS column_name,
    t.name AS data_type,
    c.is_nullable,
    dc.definition AS default_value
FROM sys.columns c
INNER JOIN sys.types t ON c.user_type_id = t.user_type_id
LEFT JOIN sys.default_constraints dc ON c.default_object_id = dc.object_id
WHERE OBJECT_NAME(c.object_id) = 'products';
