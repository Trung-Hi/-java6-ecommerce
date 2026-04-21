-- ============================================
-- CHECK: Trigger trên product_sizes
-- ============================================

-- 1. Trigger trên product_sizes
SELECT 
    t.name AS trigger_name,
    CASE WHEN t.is_disabled = 1 THEN 'Disabled' ELSE 'Enabled' END AS status,
    m.definition AS trigger_sql
FROM sys.triggers t
INNER JOIN sys.tables tb ON t.parent_id = tb.object_id
INNER JOIN sys.sql_modules m ON t.object_id = m.object_id
WHERE tb.name = 'product_sizes';

-- 2. Xem dữ liệu product_sizes cho product_id 1588
SELECT * FROM product_sizes WHERE product_id = 1588;

-- 3. Tổng quantity từ product_sizes
SELECT SUM(quantity) as total FROM product_sizes WHERE product_id = 1588;
