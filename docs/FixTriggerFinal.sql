-- ============================================
-- FIX: Trigger chỉ cập nhật khi có ProductVariant thực sự
-- ============================================

-- 1. Enable lại trigger trước khi sửa
ENABLE TRIGGER ALL ON products;
GO

-- 2. Xóa trigger cũ (nếu có)
IF EXISTS (SELECT * FROM sys.triggers WHERE name = 'trg_update_product_quantity')
    DROP TRIGGER trg_update_product_quantity;
GO

-- 3. Tạo trigger MỚI - Chỉ chạy khi có ProductVariant
-- Lưu ý: Trigger này đặt trên bảng ProductVariant, không phải products
IF EXISTS (SELECT * FROM sys.tables WHERE name = 'ProductVariant')
BEGIN
    EXEC('
    CREATE TRIGGER trg_update_product_quantity
    ON ProductVariant
    AFTER INSERT, UPDATE, DELETE
    AS
    BEGIN
        SET NOCOUNT ON;
        
        -- Chỉ cập nhật sản phẩm có biến thể thực sự
        UPDATE p
        SET p.quantity = ISNULL(pv_sum.total_qty, 0)
        FROM products p
        INNER JOIN (
            SELECT DISTINCT product_id FROM inserted
            UNION
            SELECT DISTINCT product_id FROM deleted
        ) affected ON p.id = affected.product_id
        INNER JOIN (
            -- Chỉ tính nếu có ít nhất 1 variant
            SELECT product_id, SUM(quantity) as total_qty
            FROM ProductVariant
            WHERE product_id IN (
                SELECT product_id FROM inserted
                UNION
                SELECT product_id FROM deleted
            )
            GROUP BY product_id
            HAVING COUNT(*) > 0  -- Chỉ khi có variant
        ) pv_sum ON p.id = pv_sum.product_id;
    END;
    ');
    PRINT 'Trigger created on ProductVariant';
END
ELSE
BEGIN
    PRINT 'ProductVariant table not found. No trigger created.';
END;
GO

-- 4. Kiểm tra trigger đã tạo
SELECT 
    t.name AS trigger_name,
    OBJECT_NAME(t.parent_id) AS table_name,
    CASE WHEN t.is_disabled = 1 THEN 'Disabled' ELSE 'Enabled' END AS status
FROM sys.triggers t
WHERE t.name = 'trg_update_product_quantity';
GO

-- 5. Test: Cập nhật số liệu cho sản phẩm cũ (nếu cần)
-- UPDATE p SET p.quantity = 100 FROM products p WHERE p.id = 1604;
