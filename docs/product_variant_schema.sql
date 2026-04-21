-- ============================================
-- PRODUCT VARIANT SYSTEM - SQL SERVER
-- Production-Ready Schema for E-commerce
-- ============================================

-- Drop tables if exist (for clean setup)
IF OBJECT_ID('dbo.VariantAttribute', 'U') IS NOT NULL DROP TABLE VariantAttribute;
IF OBJECT_ID('dbo.ProductVariant', 'U') IS NOT NULL DROP TABLE ProductVariant;
IF OBJECT_ID('dbo.AttributeValue', 'U') IS NOT NULL DROP TABLE AttributeValue;
IF OBJECT_ID('dbo.Attribute', 'U') IS NOT NULL DROP TABLE Attribute;
IF OBJECT_ID('dbo.Product', 'U') IS NOT NULL DROP TABLE Product;
IF OBJECT_ID('dbo.Category', 'U') IS NOT NULL DROP TABLE Category;
GO

-- ============================================
-- 1. CATEGORY TABLE
-- ============================================
CREATE TABLE Category (
    id VARCHAR(50) PRIMARY KEY,
    name NVARCHAR(100) NOT NULL,
    description NVARCHAR(500),
    created_at DATETIME2 DEFAULT GETDATE(),
    updated_at DATETIME2 DEFAULT GETDATE()
);

-- ============================================
-- 2. PRODUCT TABLE
-- ============================================
CREATE TABLE Product (
    id BIGINT IDENTITY(1,1) PRIMARY KEY,
    product_code VARCHAR(50) NOT NULL UNIQUE,  -- TSHIRT, SHOE-001...
    name NVARCHAR(200) NOT NULL,
    description NVARCHAR(MAX),
    base_price DECIMAL(18,2) NOT NULL DEFAULT 0,
    discount_percent DECIMAL(5,2) DEFAULT 0 CHECK (discount_percent >= 0 AND discount_percent <= 100),
    image_url VARCHAR(500),
    category_id VARCHAR(50),
    is_active BIT DEFAULT 1,
    total_quantity INT DEFAULT 0,  -- Auto-calculated from variants
    created_at DATETIME2 DEFAULT GETDATE(),
    updated_at DATETIME2 DEFAULT GETDATE(),
    
    CONSTRAINT FK_Product_Category 
        FOREIGN KEY (category_id) REFERENCES Category(id)
        ON DELETE SET NULL
);

CREATE INDEX IX_Product_Category ON Product(category_id);
CREATE INDEX IX_Product_Code ON Product(product_code);
CREATE INDEX IX_Product_Active ON Product(is_active) WHERE is_active = 1;

-- ============================================
-- 3. ATTRIBUTE TABLE (Size, Color, Material...)
-- ============================================
CREATE TABLE Attribute (
    id BIGINT IDENTITY(1,1) PRIMARY KEY,
    name VARCHAR(50) NOT NULL UNIQUE,  -- 'size', 'color', 'material'
    display_name NVARCHAR(50) NOT NULL,  -- 'Kích thước', 'Màu sắc'
    data_type VARCHAR(20) DEFAULT 'string',  -- string, number, hex_color
    sort_order INT DEFAULT 0,
    is_active BIT DEFAULT 1,
    created_at DATETIME2 DEFAULT GETDATE()
);

CREATE INDEX IX_Attribute_Name ON Attribute(name);

-- ============================================
-- 4. ATTRIBUTE VALUE TABLE (S, M, Đen, Trắng...)
-- ============================================
CREATE TABLE AttributeValue (
    id BIGINT IDENTITY(1,1) PRIMARY KEY,
    attribute_id BIGINT NOT NULL,
    value VARCHAR(100) NOT NULL,  -- 'S', 'M', 'BLACK'
    display_value NVARCHAR(100) NOT NULL,  -- 'Nhỏ', 'Vừa', 'Đen'
    hex_code VARCHAR(7) NULL,  -- For colors: #000000
    sort_order INT DEFAULT 0,
    is_active BIT DEFAULT 1,
    created_at DATETIME2 DEFAULT GETDATE(),
    
    CONSTRAINT FK_AttributeValue_Attribute 
        FOREIGN KEY (attribute_id) REFERENCES Attribute(id) ON DELETE CASCADE,
    CONSTRAINT UQ_AttributeValue 
        UNIQUE (attribute_id, value)
);

CREATE INDEX IX_AttributeValue_Attribute ON AttributeValue(attribute_id);
CREATE INDEX IX_AttributeValue_Value ON AttributeValue(value);

-- ============================================
-- 5. PRODUCT VARIANT TABLE (SKU Level)
-- ============================================
CREATE TABLE ProductVariant (
    id BIGINT IDENTITY(1,1) PRIMARY KEY,
    product_id BIGINT NOT NULL,
    sku VARCHAR(100) NOT NULL UNIQUE,  -- TSHIRT-S-BLACK
    price_adjustment DECIMAL(18,2) DEFAULT 0,  -- +/- from base_price
    quantity INT NOT NULL DEFAULT 0 CHECK (quantity >= 0),
    is_active BIT DEFAULT 1,
    barcode VARCHAR(100),
    weight_grams INT,  -- For shipping calculation
    created_at DATETIME2 DEFAULT GETDATE(),
    updated_at DATETIME2 DEFAULT GETDATE(),
    
    CONSTRAINT FK_Variant_Product 
        FOREIGN KEY (product_id) REFERENCES Product(id) ON DELETE CASCADE
);

CREATE INDEX IX_Variant_Product ON ProductVariant(product_id);
CREATE INDEX IX_Variant_SKU ON ProductVariant(sku);
CREATE INDEX IX_Variant_Active ON ProductVariant(is_active) WHERE is_active = 1;

-- ============================================
-- 6. VARIANT ATTRIBUTE MAPPING (Many-to-Many)
-- ============================================
CREATE TABLE VariantAttribute (
    id BIGINT IDENTITY(1,1) PRIMARY KEY,
    variant_id BIGINT NOT NULL,
    attribute_value_id BIGINT NOT NULL,
    created_at DATETIME2 DEFAULT GETDATE(),
    
    CONSTRAINT FK_VA_Variant 
        FOREIGN KEY (variant_id) REFERENCES ProductVariant(id) ON DELETE CASCADE,
    CONSTRAINT FK_VA_AttributeValue 
        FOREIGN KEY (attribute_value_id) REFERENCES AttributeValue(id),
    CONSTRAINT UQ_Variant_AttributeValue 
        UNIQUE (variant_id, attribute_value_id)
);

CREATE INDEX IX_VA_Variant ON VariantAttribute(variant_id);
CREATE INDEX IX_VA_AttributeValue ON VariantAttribute(attribute_value_id);

-- ============================================
-- 7. UNIQUE CONSTRAINT: Không trùng attribute combo
-- ============================================
-- Tạo function để kiểm tra duplicate variant
GO

CREATE OR ALTER FUNCTION dbo.GetVariantAttributeHash(@variant_id BIGINT)
RETURNS VARCHAR(MAX)
AS
BEGIN
    DECLARE @hash VARCHAR(MAX);
    
    SELECT @hash = STRING_AGG(
        CAST(av.attribute_id AS VARCHAR) + ':' + av.value, 
        ','
    ) WITHIN GROUP (ORDER BY av.attribute_id, av.value)
    FROM VariantAttribute va
    JOIN AttributeValue av ON va.attribute_value_id = av.id
    WHERE va.variant_id = @variant_id;
    
    RETURN @hash;
END;
GO

-- ============================================
-- 8. TRIGGER: Auto-update product total_quantity
-- ============================================
CREATE OR ALTER TRIGGER trg_UpdateProductQuantity
ON ProductVariant
AFTER INSERT, UPDATE, DELETE
AS
BEGIN
    SET NOCOUNT ON;
    
    DECLARE @product_id BIGINT;
    
    -- Get affected product IDs
    SELECT DISTINCT product_id INTO #AffectedProducts
    FROM (
        SELECT product_id FROM inserted
        UNION
        SELECT product_id FROM deleted
    ) AS affected;
    
    -- Update total quantity for each product
    UPDATE p
    SET 
        p.total_quantity = ISNULL((
            SELECT SUM(quantity) 
            FROM ProductVariant pv 
            WHERE pv.product_id = p.id AND pv.is_active = 1
        ), 0),
        p.updated_at = GETDATE()
    FROM Product p
    INNER JOIN #AffectedProducts ap ON p.id = ap.product_id;
END;
GO

-- ============================================
-- 9. SAMPLE DATA
-- ============================================

-- Insert Categories
INSERT INTO Category (id, name, description) VALUES
('CAT01', N'Áo thun', N'Áo thun nam nữ các loại'),
('CAT02', N'Quần jean', N'Quần jean thời trang'),
('CAT03', N'Giày dép', N'Giày dép thể thao');

-- Insert Attributes
INSERT INTO Attribute (name, display_name, data_type, sort_order) VALUES
('size', N'Kích thước', 'string', 1),
('color', N'Màu sắc', 'hex_color', 2),
('material', N'Chất liệu', 'string', 3);

-- Insert Attribute Values
INSERT INTO AttributeValue (attribute_id, value, display_value, hex_code, sort_order) VALUES
-- Size values
(1, 'S', N'Nhỏ (S)', NULL, 1),
(1, 'M', N'Vừa (M)', NULL, 2),
(1, 'L', N'Lớn (L)', NULL, 3),
(1, 'XL', N'Rất lớn (XL)', NULL, 4),
(1, '36', N'36', NULL, 5),
(1, '37', N'37', NULL, 6),
(1, '38', N'38', NULL, 7),
(1, '39', N'39', NULL, 8),
(1, '40', N'40', NULL, 9),
(1, '41', N'41', NULL, 10),
(1, '42', N'42', NULL, 11),
(1, '43', N'43', NULL, 12),

-- Color values
(2, 'white', N'Trắng', '#FFFFFF', 1),
(2, 'black', N'Đen', '#000000', 2),
(2, 'red', N'Đỏ', '#EF4444', 3),
(2, 'blue', N'Xanh dương', '#3B82F6', 4),
(2, 'green', N'Xanh lá', '#22C55E', 5),
(2, 'yellow', N'Vàng', '#EAB308', 6),
(2, 'gray', N'Xám', '#6B7280', 7),

-- Material values
(3, 'cotton', N'100% Cotton', NULL, 1),
(3, 'polyester', N'Polyester', NULL, 2),
(3, 'blend', N'Cotton-Poly Blend', NULL, 3),
(3, 'leather', N'Da', NULL, 4),
(3, 'canvas', N'Vải canvas', NULL, 5);

GO

-- ============================================
-- 10. STORED PROCEDURES
-- ============================================

-- SP: Create Product with Variants
CREATE OR ALTER PROCEDURE sp_CreateProductWithVariants
    @product_code VARCHAR(50),
    @name NVARCHAR(200),
    @description NVARCHAR(MAX),
    @base_price DECIMAL(18,2),
    @discount_percent DECIMAL(5,2),
    @image_url VARCHAR(500),
    @category_id VARCHAR(50),
    @variants_json NVARCHAR(MAX),  -- JSON array of variants
    @new_product_id BIGINT OUTPUT
AS
BEGIN
    SET NOCOUNT ON;
    BEGIN TRANSACTION;
    
    BEGIN TRY
        -- B1: Insert Product
        INSERT INTO Product (
            product_code, name, description, base_price, 
            discount_percent, image_url, category_id
        )
        VALUES (
            @product_code, @name, @description, @base_price,
            @discount_percent, @image_url, @category_id
        );
        
        SET @new_product_id = SCOPE_IDENTITY();
        
        -- B2: Parse and process variants
        DECLARE @variant_idx INT = 0;
        DECLARE @variant_count INT = JSON_VALUE(@variants_json, '$.length');
        
        WHILE @variant_idx < @variant_count
        BEGIN
            DECLARE @variant_path VARCHAR(100) = '$[' + CAST(@variant_idx AS VARCHAR) + ']';
            DECLARE @quantity INT = JSON_VALUE(@variants_json, @variant_path + '.quantity');
            DECLARE @price_adjustment DECIMAL(18,2) = ISNULL(JSON_VALUE(@variants_json, @variant_path + '.price_adjustment'), 0);
            DECLARE @attributes_json NVARCHAR(MAX) = JSON_QUERY(@variants_json, @variant_path + '.attributes');
            
            -- Generate SKU
            DECLARE @sku VARCHAR(100) = @product_code;
            DECLARE @attr_names VARCHAR(MAX) = '';
            
            -- Build SKU suffix from attributes
            SELECT @sku = @sku + '-' + UPPER(av.value),
                   @attr_names = @attr_names + av.display_value + ' '
            FROM OPENJSON(@attributes_json)
            WITH (
                attribute_name VARCHAR(50) '$.name',
                attribute_value VARCHAR(100) '$.value'
            ) AS attr
            JOIN Attribute a ON a.name = attr.attribute_name
            JOIN AttributeValue av ON av.attribute_id = a.id AND av.value = attr.attribute_value
            ORDER BY a.sort_order;
            
            -- Check if SKU exists, append number if needed
            DECLARE @sku_exists INT = 1;
            DECLARE @sku_counter INT = 1;
            DECLARE @original_sku VARCHAR(100) = @sku;
            
            WHILE @sku_exists = 1
            BEGIN
                IF NOT EXISTS (SELECT 1 FROM ProductVariant WHERE sku = @sku)
                BEGIN
                    SET @sku_exists = 0;
                END
                ELSE
                BEGIN
                    SET @sku = @original_sku + '-' + CAST(@sku_counter AS VARCHAR);
                    SET @sku_counter = @sku_counter + 1;
                END
            END
            
            -- B3: Create Variant
            INSERT INTO ProductVariant (
                product_id, sku, price_adjustment, quantity, barcode
            )
            VALUES (
                @new_product_id, @sku, @price_adjustment, @quantity, 
                @sku  -- Use SKU as default barcode
            );
            
            DECLARE @variant_id BIGINT = SCOPE_IDENTITY();
            
            -- B4: Map Attributes
            INSERT INTO VariantAttribute (variant_id, attribute_value_id)
            SELECT @variant_id, av.id
            FROM OPENJSON(@attributes_json)
            WITH (
                attribute_name VARCHAR(50) '$.name',
                attribute_value VARCHAR(100) '$.value'
            ) AS attr
            JOIN Attribute a ON a.name = attr.attribute_name
            JOIN AttributeValue av ON av.attribute_id = a.id AND av.value = attr.attribute_value;
            
            SET @variant_idx = @variant_idx + 1;
        END
        
        COMMIT TRANSACTION;
    END TRY
    BEGIN CATCH
        ROLLBACK TRANSACTION;
        THROW;
    END CATCH
END;
GO

-- SP: Get Product with Variants
CREATE OR ALTER PROCEDURE sp_GetProductWithVariants
    @product_id BIGINT
AS
BEGIN
    SET NOCOUNT ON;
    
    -- Product info
    SELECT 
        p.id,
        p.product_code,
        p.name,
        p.description,
        p.base_price,
        p.discount_percent,
        p.image_url,
        p.category_id,
        c.name AS category_name,
        p.total_quantity,
        p.is_active
    FROM Product p
    LEFT JOIN Category c ON p.category_id = c.id
    WHERE p.id = @product_id;
    
    -- Variants with attributes
    SELECT 
        pv.id AS variant_id,
        pv.sku,
        pv.price_adjustment,
        pv.quantity,
        pv.barcode,
        pv.is_active,
        (
            SELECT 
                a.name AS attribute_name,
                a.display_name,
                av.value,
                av.display_value,
                av.hex_code
            FROM VariantAttribute va
            JOIN AttributeValue av ON va.attribute_value_id = av.id
            JOIN Attribute a ON av.attribute_id = a.id
            WHERE va.variant_id = pv.id
            FOR JSON PATH
        ) AS attributes
    FROM ProductVariant pv
    WHERE pv.product_id = @product_id
    ORDER BY pv.sku;
END;
GO

-- ============================================
-- 11. VIEWS
-- ============================================

-- View: All Variants with Details
CREATE OR ALTER VIEW vw_ProductVariantDetails
AS
SELECT 
    p.id AS product_id,
    p.product_code,
    p.name AS product_name,
    p.base_price,
    p.discount_percent,
    pv.id AS variant_id,
    pv.sku,
    pv.price_adjustment,
    (p.base_price + pv.price_adjustment) AS final_price,
    pv.quantity,
    pv.is_active AS variant_active,
    STRING_AGG(a.display_name + ': ' + av.display_value, ', ') AS variant_attributes,
    p.is_active AS product_active,
    p.image_url
FROM Product p
JOIN ProductVariant pv ON p.id = pv.product_id
LEFT JOIN VariantAttribute va ON pv.id = va.variant_id
LEFT JOIN AttributeValue av ON va.attribute_value_id = av.id
LEFT JOIN Attribute a ON av.attribute_id = a.id
GROUP BY 
    p.id, p.product_code, p.name, p.base_price, p.discount_percent,
    pv.id, pv.sku, pv.price_adjustment, pv.quantity, pv.is_active,
    p.is_active, p.image_url;
GO

PRINT 'Product Variant Schema Created Successfully!';
