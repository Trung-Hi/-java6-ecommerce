-- ============================================
-- DEBUG FINAL: Kiểm tra trigger và dữ liệu
-- ============================================

-- 1. Tất cả trigger trên bảng 'products'
SELECT 
    t.name AS trigger_name,
    CASE WHEN t.is_disabled = 1 THEN 'Disabled' ELSE 'Enabled' END AS status
FROM sys.triggers t
INNER JOIN sys.tables tb ON t.parent_id = tb.object_id
WHERE tb.name = 'products';

-- 2. Sản phẩm mới nhất (vừa tạo sau khi disable trigger)
SELECT TOP 5 id, name, quantity, available, create_date
FROM products
ORDER BY id DESC;

-- 3. Kiểm tra sản phẩm vừa tạo có quantity = 0 không
SELECT id, name, quantity, 
       CASE WHEN quantity IS NULL THEN 'NULL' 
            WHEN quantity = 0 THEN 'ZERO' 
            ELSE 'HAS VALUE: ' + CAST(quantity AS VARCHAR) 
       END as qty_status
FROM products 
WHERE create_date >= DATEADD(MINUTE, -5, GETDATE())
ORDER BY id DESC;
