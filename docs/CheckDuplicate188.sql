-- ============================================
-- CHECK: Có bao nhiêu sản phẩm tên "Sản phẩm 188"?
-- ============================================

-- 1. Tất cả sản phẩm tên "Sản phẩm 188"
SELECT id, name, quantity, price, available, create_date, is_delete
FROM products
WHERE name = 'Sản phẩm 188'
ORDER BY create_date DESC;

-- 2. Kiểm tra sản phẩm Giày da (danh mục CAT08)
SELECT id, name, quantity, price, category_id, create_date
FROM products
WHERE category_id = 'CAT08'
ORDER BY create_date DESC;

-- 3. Tổng hợp sản phẩm mới nhất (5 sản phẩm)
SELECT TOP 5 id, name, quantity, price, create_date
FROM products
ORDER BY create_date DESC;
