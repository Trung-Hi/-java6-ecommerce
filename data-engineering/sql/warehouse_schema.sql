-- ========================================
-- E-Commerce Data Platform v2.0
-- Data Warehouse Schema - Star Schema
-- ========================================
-- Database: ASM_Java5
-- Schema: ecommerce_warehouse
-- Tác giả: Data Engineering Team
-- Mô tả: Schema warehouse cho phân tích dữ liệu thương mại điện tử
-- ========================================

-- Tạo schema warehouse nếu chưa tồn tại
IF NOT EXISTS (SELECT * FROM sys.schemas WHERE name = 'ecommerce_warehouse')
BEGIN
    EXEC('CREATE SCHEMA ecommerce_warehouse');
    PRINT 'Đã tạo schema: ecommerce_warehouse';
END
ELSE
BEGIN
    PRINT 'Schema ecommerce_warehouse đã tồn tại';
END
GO

-- ========================================
-- DIMENSION TABLES
-- ========================================

PRINT '========================================';
PRINT 'BẮT ĐẦU TẠO CÁC BẢNG DIMENSION';
PRINT '========================================';
GO

-- ========================================
-- 1. dim_date - Dimension Ngày Tháng
-- ========================================
-- Mục đích: Lưu trữ thông tin về ngày tháng để phân tích theo thời gian
-- Khóa chính: date_key (YYYYMMDD format)
IF OBJECT_ID('ecommerce_warehouse.dim_date', 'U') IS NOT NULL
    DROP TABLE ecommerce_warehouse.dim_date;
GO

CREATE TABLE ecommerce_warehouse.dim_date (
    date_key INT PRIMARY KEY,
    full_date DATE NOT NULL,
    day INT NOT NULL,                    -- Ngày trong tháng (1-31)
    month INT NOT NULL,                  -- Tháng (1-12)
    year INT NOT NULL,                   -- Năm
    quarter INT NOT NULL,                -- Quý (1-4)
    weekday_name NVARCHAR(20) NOT NULL,  -- Tên ngày trong tuần
    is_weekend BIT NOT NULL,             -- Có phải cuối tuần không
    is_holiday BIT DEFAULT 0,            -- Có phải ngày lễ không
    week_of_year INT,                    -- Tuần trong năm
    CONSTRAINT UQ_dim_date UNIQUE (full_date)
);
GO

PRINT '✅ Đã tạo bảng: ecommerce_warehouse.dim_date';
GO

-- ========================================
-- 2. dim_product - Dimension Sản Phẩm
-- ========================================
-- Mục đích: Lưu trữ thông tin chi tiết về sản phẩm
-- Khóa chính: product_key (surrogate key)
IF OBJECT_ID('ecommerce_warehouse.dim_product', 'U') IS NOT NULL
    DROP TABLE ecommerce_warehouse.dim_product;
GO

CREATE TABLE ecommerce_warehouse.dim_product (
    product_key INT IDENTITY(1,1) PRIMARY KEY,
    product_id INT NOT NULL,              -- ID sản phẩm từ hệ thống nguồn
    name NVARCHAR(200) NOT NULL,
    image NVARCHAR(255),
    category_id NVARCHAR(20),            -- Category ID là varchar trong ASM_Java5
    category_name NVARCHAR(100),
    price DECIMAL(12,2),
    discount DECIMAL(5,2),
    stock_quantity INT DEFAULT 0,
    is_active BIT DEFAULT 1,
    created_at DATETIME DEFAULT GETDATE(),
    updated_at DATETIME DEFAULT GETDATE(),
    CONSTRAINT UQ_dim_product UNIQUE (product_id)
);
GO

PRINT '✅ Đã tạo bảng: ecommerce_warehouse.dim_product';
GO

-- ========================================
-- 3. dim_customer - Dimension Khách Hàng
-- ========================================
-- Mục đích: Lưu trữ thông tin chi tiết về khách hàng
-- Khóa chính: customer_key (surrogate key)
-- ASM_Java5: sử dụng username từ bảng accounts
IF OBJECT_ID('ecommerce_warehouse.dim_customer', 'U') IS NOT NULL
    DROP TABLE ecommerce_warehouse.dim_customer;
GO

CREATE TABLE ecommerce_warehouse.dim_customer (
    customer_key INT IDENTITY(1,1) PRIMARY KEY,
    username NVARCHAR(50) NOT NULL,      -- Username từ bảng accounts
    fullname NVARCHAR(100),
    email NVARCHAR(100),
    phone NVARCHAR(20),
    address NVARCHAR(255),
    join_date DATE,
    join_year INT,
    join_month INT,
    is_active BIT DEFAULT 1,
    customer_segment NVARCHAR(50),      -- Phân khúc khách hàng
    total_orders INT DEFAULT 0,
    total_spent DECIMAL(18,2) DEFAULT 0,
    created_at DATETIME DEFAULT GETDATE(),
    updated_at DATETIME DEFAULT GETDATE(),
    CONSTRAINT UQ_dim_customer UNIQUE (username)
);
GO

PRINT '✅ Đã tạo bảng: ecommerce_warehouse.dim_customer';
GO

-- ========================================
-- 4. dim_category - Dimension Danh Mục
-- ========================================
-- Mục đích: Lưu trữ thông tin về danh mục sản phẩm
-- Khóa chính: category_key (surrogate key)
IF OBJECT_ID('ecommerce_warehouse.dim_category', 'U') IS NOT NULL
    DROP TABLE ecommerce_warehouse.dim_category;
GO

CREATE TABLE ecommerce_warehouse.dim_category (
    category_key INT IDENTITY(1,1) PRIMARY KEY,
    category_id NVARCHAR(20) NOT NULL,   -- Category ID là varchar trong ASM_Java5
    name NVARCHAR(100) NOT NULL,
    parent_id NVARCHAR(20),
    is_active BIT DEFAULT 1,
    created_at DATETIME DEFAULT GETDATE(),
    CONSTRAINT UQ_dim_category UNIQUE (category_id)
);
GO

PRINT '✅ Đã tạo bảng: ecommerce_warehouse.dim_category';
GO

-- ========================================
-- FACT TABLES
-- ========================================

PRINT '========================================';
PRINT 'BẮT ĐẦU TẠO CÁC BẢNG FACT';
PRINT '========================================';
GO

-- ========================================
-- 5. fact_orders - Fact Đơn Hàng
-- ========================================
-- Mục đích: Lưu trữ các giao dịch bán hàng (measurements)
-- Khóa chính: order_id
-- Khóa ngoại: product_key, customer_key, date_key
IF OBJECT_ID('ecommerce_warehouse.fact_orders', 'U') IS NOT NULL
    DROP TABLE ecommerce_warehouse.fact_orders;
GO

CREATE TABLE ecommerce_warehouse.fact_orders (
    order_id BIGINT NOT NULL,              -- order_id là bigint trong ASM_Java5
    order_detail_id BIGINT PRIMARY KEY,    -- Unique ID for each order line item
    product_key INT NOT NULL,
    customer_key INT NOT NULL,
    date_key INT NOT NULL,
    size_id INT,                         -- Size ID từ order_details
    quantity INT NOT NULL DEFAULT 1,
    unit_price DECIMAL(12,2) NOT NULL,   -- price từ order_details
    revenue DECIMAL(12,2) NOT NULL,      -- Doanh thu = unit_price * quantity
    final_total DECIMAL(18,2),           -- Tổng đơn hàng từ orders
    status NVARCHAR(20),
    payment_method NVARCHAR(50),
    phone NVARCHAR(20),
    address NVARCHAR(255),
    order_date DATE NOT NULL,
    order_time TIME,
    is_returned BIT DEFAULT 0,
    created_at DATETIME DEFAULT GETDATE(),
    
    -- Foreign Keys
    CONSTRAINT FK_fact_orders_product 
        FOREIGN KEY (product_key) 
        REFERENCES ecommerce_warehouse.dim_product(product_key),
    CONSTRAINT FK_fact_orders_customer 
        FOREIGN KEY (customer_key) 
        REFERENCES ecommerce_warehouse.dim_customer(customer_key),
    CONSTRAINT FK_fact_orders_date 
        FOREIGN KEY (date_key) 
        REFERENCES ecommerce_warehouse.dim_date(date_key)
);
GO

-- Tạo index cho các cột thường dùng trong query
CREATE INDEX IX_fact_orders_date_key ON ecommerce_warehouse.fact_orders(date_key);
CREATE INDEX IX_fact_orders_customer_key ON ecommerce_warehouse.fact_orders(customer_key);
CREATE INDEX IX_fact_orders_product_key ON ecommerce_warehouse.fact_orders(product_key);
CREATE INDEX IX_fact_orders_status ON ecommerce_warehouse.fact_orders(status);
CREATE INDEX IX_fact_orders_order_date ON ecommerce_warehouse.fact_orders(order_date);
GO

PRINT '✅ Đã tạo bảng: ecommerce_warehouse.fact_orders';
GO

-- ========================================
-- KẾT THÚC TẠO SCHEMA
-- ========================================

PRINT '========================================';
PRINT '✅ HOÀN THÀNH TẠO DATA WAREHOUSE SCHEMA';
PRINT '========================================';
PRINT '';
PRINT 'Các bảng đã tạo:';
PRINT '  - ecommerce_warehouse.dim_date';
PRINT '  - ecommerce_warehouse.dim_product';
PRINT '  - ecommerce_warehouse.dim_customer';
PRINT '  - ecommerce_warehouse.dim_category';
PRINT '  - ecommerce_warehouse.fact_orders';
PRINT '========================================';
GO
