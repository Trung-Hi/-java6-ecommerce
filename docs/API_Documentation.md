# Product Variant System - API Documentation

## Flow Overview

```
UI (Vue.js) → API Request → Spring Boot Controller → Service Layer 
    → Transaction (Product + Variants + Attributes) 
    → SQL Server Database
```

## 1. CREATE PRODUCT WITH VARIANTS

### Request
```http
POST /api/products
Content-Type: application/json

{
  "productCode": "TSHIRT-001",
  "name": "Áo thun nam cotton",
  "description": "Áo thun chất liệu cotton 100%, thoáng mát",
  "basePrice": 250000,
  "discountPercent": 10,
  "imageUrl": "/images/tshirt-001.jpg",
  "categoryId": "CAT01",
  "variants": [
    {
      "quantity": 15,
      "priceAdjustment": 0,
      "attributes": {
        "size": "S",
        "color": "white"
      }
    },
    {
      "quantity": 20,
      "priceAdjustment": 0,
      "attributes": {
        "size": "M",
        "color": "white"
      }
    },
    {
      "quantity": 10,
      "priceAdjustment": 20000,
      "attributes": {
        "size": "L",
        "color": "black"
      }
    },
    {
      "quantity": 8,
      "priceAdjustment": 20000,
      "attributes": {
        "size": "XL",
        "color": "black",
        "material": "blend"
      }
    }
  ]
}
```

### Response (201 Created)
```json
{
  "id": 1,
  "productCode": "TSHIRT-001",
  "name": "Áo thun nam cotton",
  "description": "Áo thun chất liệu cotton 100%, thoáng mát",
  "basePrice": 250000,
  "discountPercent": 10,
  "finalPrice": 225000,
  "imageUrl": "/images/tshirt-001.jpg",
  "categoryId": "CAT01",
  "totalQuantity": 53,
  "isActive": true,
  "variants": [
    {
      "id": 1,
      "sku": "TSHIRT-001-S-WHITE",
      "priceAdjustment": 0,
      "finalPrice": 225000,
      "quantity": 15,
      "isActive": true,
      "barcode": "TSHIRT-001-S-WHITE",
      "attributes": [
        {
          "attributeName": "size",
          "attributeDisplayName": "Kích thước",
          "value": "S",
          "displayValue": "Nhỏ (S)",
          "hexCode": null
        },
        {
          "attributeName": "color",
          "attributeDisplayName": "Màu sắc",
          "value": "white",
          "displayValue": "Trắng",
          "hexCode": "#FFFFFF"
        }
      ]
    },
    {
      "id": 2,
      "sku": "TSHIRT-001-M-WHITE",
      "priceAdjustment": 0,
      "finalPrice": 225000,
      "quantity": 20,
      "isActive": true,
      "barcode": "TSHIRT-001-M-WHITE",
      "attributes": [
        {
          "attributeName": "size",
          "attributeDisplayName": "Kích thước",
          "value": "M",
          "displayValue": "Vừa (M)"
        },
        {
          "attributeName": "color",
          "attributeDisplayName": "Màu sắc",
          "value": "white",
          "displayValue": "Trắng",
          "hexCode": "#FFFFFF"
        }
      ]
    },
    {
      "id": 3,
      "sku": "TSHIRT-001-L-BLACK",
      "priceAdjustment": 20000,
      "finalPrice": 245000,
      "quantity": 10,
      "isActive": true,
      "barcode": "TSHIRT-001-L-BLACK",
      "attributes": [
        {
          "attributeName": "size",
          "attributeDisplayName": "Kích thước",
          "value": "L",
          "displayValue": "Lớn (L)"
        },
        {
          "attributeName": "color",
          "attributeDisplayName": "Màu sắc",
          "value": "black",
          "displayValue": "Đen",
          "hexCode": "#000000"
        }
      ]
    },
    {
      "id": 4,
      "sku": "TSHIRT-001-XL-BLACK-BLEND",
      "priceAdjustment": 20000,
      "finalPrice": 245000,
      "quantity": 8,
      "isActive": true,
      "barcode": "TSHIRT-001-XL-BLACK-BLEND",
      "attributes": [
        {
          "attributeName": "size",
          "attributeDisplayName": "Kích thước",
          "value": "XL",
          "displayValue": "Rất lớn (XL)"
        },
        {
          "attributeName": "color",
          "attributeDisplayName": "Màu sắc",
          "value": "black",
          "displayValue": "Đen",
          "hexCode": "#000000"
        },
        {
          "attributeName": "material",
          "attributeDisplayName": "Chất liệu",
          "value": "blend",
          "displayValue": "Cotton-Poly Blend"
        }
      ]
    }
  ]
}
```

## 2. GET PRODUCT BY ID

### Request
```http
GET /api/products/1
```

### Response (200 OK)
```json
{
  "id": 1,
  "productCode": "TSHIRT-001",
  "name": "Áo thun nam cotton",
  "description": "Áo thun chất liệu cotton 100%, thoáng mát",
  "basePrice": 250000,
  "discountPercent": 10,
  "finalPrice": 225000,
  "imageUrl": "/images/tshirt-001.jpg",
  "categoryId": "CAT01",
  "totalQuantity": 53,
  "isActive": true,
  "variants": [
    {
      "id": 1,
      "sku": "TSHIRT-001-S-WHITE",
      "priceAdjustment": 0,
      "finalPrice": 225000,
      "quantity": 15,
      "isActive": true,
      "barcode": "TSHIRT-001-S-WHITE",
      "attributes": [
        {
          "attributeName": "size",
          "attributeDisplayName": "Kích thước",
          "value": "S",
          "displayValue": "Nhỏ (S)"
        },
        {
          "attributeName": "color",
          "attributeDisplayName": "Màu sắc",
          "value": "white",
          "displayValue": "Trắng",
          "hexCode": "#FFFFFF"
        }
      ]
    }
    // ... more variants
  ]
}
```

## 3. GET ALL PRODUCTS

### Request
```http
GET /api/products
```

### Response (200 OK)
```json
[
  {
    "id": 1,
    "productCode": "TSHIRT-001",
    "name": "Áo thun nam cotton",
    "basePrice": 250000,
    "discountPercent": 10,
    "finalPrice": 225000,
    "totalQuantity": 53,
    "isActive": true,
    "variants": [...]
  },
  {
    "id": 2,
    "productCode": "SHOE-001",
    "name": "Giày thể thao nam",
    "basePrice": 850000,
    "discountPercent": 0,
    "finalPrice": 850000,
    "totalQuantity": 25,
    "isActive": true,
    "variants": [...]
  }
]
```

## 4. UPDATE PRODUCT

### Request
```http
PUT /api/products/1
Content-Type: application/json

{
  "productCode": "TSHIRT-001",
  "name": "Áo thun nam cotton (Updated)",
  "description": "Updated description",
  "basePrice": 275000,
  "discountPercent": 15,
  "imageUrl": "/images/tshirt-001-new.jpg",
  "categoryId": "CAT01",
  "variants": [...]  // Optional: can handle variant updates separately
}
```

### Response (200 OK)
```json
{
  "id": 1,
  "productCode": "TSHIRT-001",
  "name": "Áo thun nam cotton (Updated)",
  "basePrice": 275000,
  "discountPercent": 15,
  "finalPrice": 233750,
  // ... rest of response
}
```

## 5. DELETE PRODUCT

### Request
```http
DELETE /api/products/1
```

### Response (204 No Content)

## 6. ERROR RESPONSES

### 400 Bad Request
```json
{
  "error": "Business Error",
  "message": "Product code already exists: TSHIRT-001",
  "timestamp": "2024-01-15T10:30:00"
}
```

### 404 Not Found
```json
{
  "error": "Not Found",
  "message": "Product not found: 999",
  "timestamp": "2024-01-15T10:30:00"
}
```

### 400 Validation Error
```json
{
  "error": "Validation Error",
  "message": "Quantity must be >= 0",
  "timestamp": "2024-01-15T10:30:00"
}
```

## Business Logic Flow

### Create Product Flow

```
┌─────────────────────────────────────────────────────────────┐
│  STEP 1: Validate Input                                      │
│  - Check product_code unique                                │
│  - Validate base_price >= 0                                 │
│  - Validate discount 0-100                                  │
│  - Validate variants not empty                              │
└─────────────────────────────────────────────────────────────┘
                              ↓
┌─────────────────────────────────────────────────────────────┐
│  STEP 2: Insert Product                                     │
│  INSERT INTO Product (product_code, name, ...)              │
│  RETURN product_id                                          │
└─────────────────────────────────────────────────────────────┘
                              ↓
┌─────────────────────────────────────────────────────────────┐
│  STEP 3: Loop Through Variants                             │
│  For each variant:                                          │
│    - Validate quantity >= 0                                 │
│    - Check duplicate attributes combo                       │
│    - Generate SKU (PRODUCTCODE-SIZE-COLOR)                 │
│    - Ensure SKU unique (append -1, -2 if needed)           │
└─────────────────────────────────────────────────────────────┘
                              ↓
┌─────────────────────────────────────────────────────────────┐
│  STEP 4: Create Variant                                     │
│  INSERT INTO ProductVariant (sku, quantity, ...)            │
│  RETURN variant_id                                        │
└─────────────────────────────────────────────────────────────┘
                              ↓
┌─────────────────────────────────────────────────────────────┐
│  STEP 5: Map Attributes                                     │
│  For each attribute in variant:                            │
│    - Find Attribute by name (size, color...)                │
│    - Find AttributeValue by attribute_id + value            │
│    - INSERT INTO VariantAttribute                          │
│      (variant_id, attribute_value_id)                        │
└─────────────────────────────────────────────────────────────┘
                              ↓
┌─────────────────────────────────────────────────────────────┐
│  STEP 6: Update Product Total Quantity                      │
│  (Handled by Trigger)                                        │
│  UPDATE Product SET total_quantity = SUM(variant.qty)      │
└─────────────────────────────────────────────────────────────┘
                              ↓
┌─────────────────────────────────────────────────────────────┐
│  STEP 7: Return Response                                    │
│  - Product with full variants                              │
│  - Generated SKUs                                           │
│  - Final prices                                             │
└─────────────────────────────────────────────────────────────┘
```

### SKU Generation Logic

```java
Input:  productCode = "TSHIRT-001"
        attributes = {size: "M", color: "black", material: "cotton"}

Step 1: Sort attributes by attribute name
        [color, material, size]

Step 2: Build SKU
        TSHIRT-001-BLACK-COTTON-M

Step 3: Check if SKU exists
        IF exists: TSHIRT-001-BLACK-COTTON-M-1
        IF still exists: TSHIRT-001-BLACK-COTTON-M-2
        ...

Output: TSHIRT-001-BLACK-COTTON-M
```

## Database Transaction Handling

```java
@Transactional(rollbackFor = Exception.class)
public ProductResponse createProduct(CreateProductRequest request) {
    try {
        // All DB operations in single transaction
        // If any step fails, ALL changes rollback
        
        Product product = createProduct(...);
        // ... create variants ...
        // ... map attributes ...
        
        return product;
    } catch (Exception e) {
        // Transaction automatically rolled back
        throw new RuntimeException("Failed to create product: " + e.getMessage());
    }
}
```

## Extension Points

### Add New Attribute (e.g., "material")

```sql
-- Insert new attribute
INSERT INTO Attribute (name, display_name, data_type, sort_order)
VALUES ('material', N'Chất liệu', 'string', 3);

-- Insert attribute values
INSERT INTO AttributeValue (attribute_id, value, display_value)
VALUES 
    (3, 'cotton', N'100% Cotton'),
    (3, 'polyester', N'Polyester'),
    (3, 'blend', N'Cotton-Poly Blend');
```

### Add Variant with Multiple Attributes

```json
{
  "quantity": 10,
  "priceAdjustment": 5000,
  "attributes": {
    "size": "L",
    "color": "red",
    "material": "cotton"
  }
}

// Generated SKU: TSHIRT-001-COTTON-L-RED
```
