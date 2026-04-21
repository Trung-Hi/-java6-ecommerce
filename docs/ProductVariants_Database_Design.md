# Database Structure for Product Variants (product_variants)

## Cấu trúc bảng hiện tại

### Bảng `sizes`
```sql
CREATE TABLE sizes (
    id INT PRIMARY KEY AUTO_INCREMENT,
    name VARCHAR(10) NOT NULL UNIQUE COMMENT 'Tên size: S, M, L, XL, 38, 39, 40...',
    UNIQUE KEY uk_name (name)
) COMMENT='Danh mục size chuẩn';
```

### Bảng `product_sizes`
```sql
CREATE TABLE product_sizes (
    id INT PRIMARY KEY AUTO_INCREMENT,
    product_id INT NOT NULL COMMENT 'ID sản phẩm',
    size_id INT NOT NULL COMMENT 'ID size từ bảng sizes',
    quantity INT NOT NULL COMMENT 'Số lượng tồn kho cho size này',
    created_at DATETIME DEFAULT CURRENT_TIMESTAMP,
    updated_at DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    
    FOREIGN KEY (product_id) REFERENCES products(id) ON DELETE CASCADE,
    FOREIGN KEY (size_id) REFERENCES sizes(id) ON DELETE RESTRICT,
    
    UNIQUE KEY uk_product_size (product_id, size_id) COMMENT 'Ngăn chặn trùng size trên cùng sản phẩm',
    INDEX idx_product_id (product_id),
    INDEX idx_size_id (size_id)
) COMMENT='Biến thể size của sản phẩm';
```

## Tối ưu cho Data Engineering (Thống kê tồn kho)

### 1. Thêm Index cho các truy vấn phổ biến

```sql
-- Index cho truy vấn tồn kho theo size
ALTER TABLE product_sizes ADD INDEX idx_size_quantity (size_id, quantity);

-- Index cho truy vấn sản phẩm có tồn kho
ALTER TABLE product_sizes ADD INDEX idx_quantity (quantity);

-- Composite index cho truy vấn tồn kho theo sản phẩm và size
ALTER TABLE product_sizes ADD INDEX idx_product_size_quantity (product_id, size_id, quantity);
```

### 2. Thêm cột tracking cho Data Engineering (Tùy chọn)

Nếu cần theo dõi lịch tồn kho, có thể thêm:

```sql
ALTER TABLE product_sizes 
ADD COLUMN sold_quantity INT DEFAULT 0 COMMENT 'Số lượng đã bán',
ADD COLUMN restocked_quantity INT DEFAULT 0 COMMENT 'Số lượng đã nhập thêm',
ADD COLUMN last_sold_at DATETIME NULL COMMENT 'Thời gian bán gần nhất',
ADD COLUMN last_restocked_at DATETIME NULL COMMENT 'Thời gian nhập thêm gần nhất';
```

### 3. View cho báo cáo tồn kho

```sql
CREATE VIEW v_inventory_summary AS
SELECT 
    p.id AS product_id,
    p.name AS product_name,
    p.category_id,
    c.name AS category_name,
    s.name AS size_name,
    ps.quantity AS current_quantity,
    ps.sold_quantity,
    (ps.quantity - ps.sold_quantity) AS available_quantity,
    CASE 
        WHEN ps.quantity = 0 THEN 'Hết hàng'
        WHEN ps.quantity <= 10 THEN 'Sắp hết'
        ELSE 'Còn hàng'
    END AS stock_status,
    ps.last_sold_at,
    ps.last_restocked_at
FROM products p
JOIN product_sizes ps ON p.id = ps.product_id
JOIN sizes s ON ps.size_id = s.id
JOIN categories c ON p.category_id = c.id
WHERE p.is_delete = FALSE;
```

### 4. Stored Procedure cho thống kê tồn kho

```sql
DELIMITER //
CREATE PROCEDURE sp_inventory_report_by_size(IN size_name VARCHAR(10))
BEGIN
    SELECT 
        s.name AS size,
        COUNT(DISTINCT p.id) AS total_products,
        SUM(ps.quantity) AS total_quantity,
        SUM(ps.sold_quantity) AS total_sold,
        SUM(ps.quantity - ps.sold_quantity) AS total_available
    FROM sizes s
    JOIN product_sizes ps ON s.id = ps.size_id
    JOIN products p ON ps.product_id = p.id
    WHERE s.name = size_name AND p.is_delete = FALSE
    GROUP BY s.name;
END //
DELIMITER ;
```

## Lợi ích của cấu trúc này

1. **Tách biệt size chuẩn**: Bảng `sizes` chứa các size chuẩn (S, M, L, XL, 38, 39, 40...), dễ dàng quản lý và mở rộng
2. **Ngăn chặn trùng lặp**: UNIQUE KEY (product_id, size_id) đảm bảo mỗi sản phẩm không có 2 dòng cùng size
3. **Hỗ trợ cascade delete**: Khi xóa sản phẩm, tự động xóa tất cả variants
4. **Tối ưu truy vấn**: Các index được thiết kế cho các truy vấn thống kê phổ biến
5. **Flexible cho Data Engineering**: Có thể thêm các cột tracking mà không ảnh hưởng logic chính

## Các truy vấn Data Engineering phổ biến

### 1. Tổng tồn kho theo size
```sql
SELECT s.name AS size, SUM(ps.quantity) AS total_quantity
FROM sizes s
LEFT JOIN product_sizes ps ON s.id = ps.size_id
LEFT JOIN products p ON ps.product_id = p.id AND p.is_delete = FALSE
GROUP BY s.name
ORDER BY s.name;
```

### 2. Sản phẩm sắp hết hàng (quantity <= 10)
```sql
SELECT p.name, s.name AS size, ps.quantity
FROM product_sizes ps
JOIN products p ON ps.product_id = p.id
JOIN sizes s ON ps.size_id = s.id
WHERE p.is_delete = FALSE AND ps.quantity > 0 AND ps.quantity <= 10
ORDER BY ps.quantity ASC;
```

### 3. Top sản phẩm bán chạy (nếu có sold_quantity)
```sql
SELECT p.name, SUM(ps.sold_quantity) AS total_sold
FROM product_sizes ps
JOIN products p ON ps.product_id = p.id
WHERE p.is_delete = FALSE
GROUP BY p.id, p.name
ORDER BY total_sold DESC
LIMIT 10;
```
