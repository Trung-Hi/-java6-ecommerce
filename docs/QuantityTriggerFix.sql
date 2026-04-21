-- ============================================
-- SQL FIX: Update trigger to auto-calculate total_quantity from variants
-- ============================================

-- Drop old trigger if exists
IF EXISTS (SELECT * FROM sys.triggers WHERE name = 'trg_update_product_quantity')
    DROP TRIGGER trg_update_product_quantity;
GO

-- Create NEW trigger that calculates from ProductVariant table
CREATE TRIGGER trg_update_product_quantity
ON ProductVariant
AFTER INSERT, UPDATE, DELETE
AS
BEGIN
    SET NOCOUNT ON;
    
    -- Update Product.total_quantity for affected products
    UPDATE p
    SET p.total_quantity = ISNULL(pv_sum.total_qty, 0)
    FROM Product p
    INNER JOIN (
        -- Get product IDs from inserted/updated/deleted variants
        SELECT DISTINCT product_id FROM inserted
        UNION
        SELECT DISTINCT product_id FROM deleted
    ) affected ON p.id = affected.product_id
    LEFT JOIN (
        -- Calculate sum of quantities from all variants
        SELECT product_id, SUM(quantity) as total_qty
        FROM ProductVariant
        WHERE product_id IN (
            SELECT product_id FROM inserted
            UNION
            SELECT product_id FROM deleted
        )
        GROUP BY product_id
    ) pv_sum ON p.id = pv_sum.product_id;
END;
GO

-- ============================================
-- MANUAL SYNC: Run this to fix existing products with wrong quantity
-- ============================================

-- Update all products to have correct total_quantity based on variants
UPDATE p
SET p.total_quantity = ISNULL(pv.total, 0)
FROM Product p
LEFT JOIN (
    SELECT product_id, SUM(quantity) as total
    FROM ProductVariant
    GROUP BY product_id
) pv ON p.id = pv.product_id
WHERE p.total_quantity != ISNULL(pv.total, 0);
GO

-- Verify the fix
SELECT 
    p.id,
    p.name,
    p.total_quantity as current_total,
    ISNULL(SUM(pv.quantity), 0) as calculated_total,
    CASE WHEN p.total_quantity = ISNULL(SUM(pv.quantity), 0) 
         THEN 'OK' 
         ELSE 'MISMATCH' 
    END as status
FROM Product p
LEFT JOIN ProductVariant pv ON p.id = pv.product_id
GROUP BY p.id, p.name, p.total_quantity
HAVING p.total_quantity != ISNULL(SUM(pv.quantity), 0);
GO
