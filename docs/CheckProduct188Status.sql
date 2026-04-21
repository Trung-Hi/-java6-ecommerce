-- ============================================
-- CHECK: Sản phẩm 188 có bị ẩn không?
-- ============================================

-- 1. Chi tiết sản phẩm 188
SELECT id, name, quantity, price, category_id, create_date, is_delete, available
FROM products
WHERE id = 1588;

-- 2. Sản phẩm 178 (đang hiển thị quantity 100)
SELECT id, name, quantity, price, category_id, create_date, is_delete, available
FROM products
WHERE id = 1578;

-- 3. Tất cả sản phẩm CAT08 không bị xóa
SELECT TOP 20 id, name, quantity, create_date, is_delete
FROM products
WHERE category_id = 'CAT08' AND is_delete = 0
ORDER BY create_date DESC;
