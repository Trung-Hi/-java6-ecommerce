-- ============================================
-- CHECK: Tất cả trigger liên quan đến quantity
-- ============================================

-- 1. Tất cả trigger trong database
SELECT 
    t.name AS trigger_name,
    OBJECT_NAME(t.parent_id) AS table_name,
    CASE WHEN t.is_disabled = 1 THEN 'Disabled' ELSE 'Enabled' END AS status
FROM sys.triggers t
WHERE t.parent_id > 0
ORDER BY table_name;

-- 2. Tìm trong definition có từ 'quantity'
SELECT 
    t.name AS trigger_name,
    OBJECT_NAME(t.parent_id) AS table_name,
    m.definition
FROM sys.triggers t
INNER JOIN sys.sql_modules m ON t.object_id = m.object_id
WHERE m.definition LIKE '%quantity%';
