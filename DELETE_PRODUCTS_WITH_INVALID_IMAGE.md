# Xóa Sản Phẩm Có Ảnh Lỗi (Null hoặc Rỗng)

## 📝 Tóm Tắt

Triển khai chức năng xóa sản phẩm có `image` là `null` hoặc chuỗi rỗng, với xử lý ngoại lệ khi sản phẩm bị ràng buộc bởi bảng OrderDetail.

---

## 1️⃣ ProductRepository - Thêm Method Tìm Sản Phẩm

**File:** `src/main/java/com/poly/ASM/repository/product/ProductRepository.java`

Thêm vào cuối file (trước dấu `}` cuối cùng):

```java
    /**
     * Tìm sản phẩm có image là null hoặc rỗng
     * @return Danh sách sản phẩm có ảnh lỗi
     */
    @Query("""
            select p 
            from Product p 
            where p.isDelete = false 
              and (p.image is null or trim(p.image) = '')
            """)
    List<Product> findProductsWithInvalidImage();
    
    /**
     * Kiểm tra xem sản phẩm có bị ràng buộc bởi OrderDetail không
     * @param productId ID của sản phẩm
     * @return true nếu có đơn hàng liên quan
     */
    @Query("""
            select case when count(od) > 0 then true else false end
            from OrderDetail od
            where od.product.id = :productId
            """)
    boolean hasOrderDetails(@Param("productId") Integer productId);
```

---

## 2️⃣ ProductService - Thêm Interface Method

**File:** `src/main/java/com/poly/ASM/service/product/ProductService.java`

Thêm vào cuối interface (trước dấu `}` cuối cùng):

```java
    /**
     * Xóa các sản phẩm có ảnh lỗi (null hoặc rỗng)
     * @return Map chứa kết quả xóa (số lượng thành công, danh sách lỗi)
     */
    Map<String, Object> deleteProductsWithInvalidImage();
```

---

## 3️⃣ ProductServiceImpl - Implement Xử Lý Xóa

**File:** `src/main/java/com/poly/ASM/service/product/impl/ProductServiceImpl.java`

Thêm các import:

```java
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.dao.EmptyResultDataAccessException;
```

Thêm method vào cuối class (trước dấu `}` cuối cùng):

```java
    /**
     * Xóa các sản phẩm có ảnh lỗi (null hoặc rỗng)
     * Xử lý ngoại lệ khi sản phẩm bị ràng buộc bởi OrderDetail
     * @return Map chứa kết quả xóa
     */
    @Override
    @Transactional
    public Map<String, Object> deleteProductsWithInvalidImage() {
        List<Product> invalidProducts = productRepository.findProductsWithInvalidImage();
        
        int successCount = 0;
        int errorCount = 0;
        List<Map<String, Object>> errors = new ArrayList<>();
        
        for (Product product : invalidProducts) {
            try {
                // Kiểm tra xem sản phẩm có bị ràng buộc bởi OrderDetail không
                boolean hasOrders = productRepository.hasOrderDetails(product.getId());
                
                if (hasOrders) {
                    // Không xóa, ghi nhận lỗi
                    Map<String, Object> error = new HashMap<>();
                    error.put("productId", product.getId());
                    error.put("productName", product.getName());
                    error.put("reason", "Sản phẩm đã có đơn hàng, không thể xóa");
                    errors.add(error);
                    errorCount++;
                    continue;
                }
                
                // Thực hiện xóa (soft delete hoặc hard delete)
                // Nếu dùng soft delete:
                product.setIsDelete(true);
                productRepository.save(product);
                
                // Nếu dùng hard delete (bỏ comment dòng dưới):
                // productRepository.deleteById(product.getId());
                
                successCount++;
                
            } catch (DataIntegrityViolationException e) {
                // Bắt lỗi ràng buộc khóa ngoại từ SQL Server
                Map<String, Object> error = new HashMap<>();
                error.put("productId", product.getId());
                error.put("productName", product.getName());
                error.put("reason", "Sản phẩm đang bị ràng buộc bởi dữ liệu khác (đơn hàng, giỏ hàng, v.v.)");
                error.put("exception", e.getMostSpecificCause().getMessage());
                errors.add(error);
                errorCount++;
                
            } catch (Exception e) {
                // Bắt các lỗi khác
                Map<String, Object> error = new HashMap<>();
                error.put("productId", product.getId());
                error.put("productName", product.getName());
                error.put("reason", "Lỗi không xác định: " + e.getMessage());
                errors.add(error);
                errorCount++;
            }
        }
        
        Map<String, Object> result = new HashMap<>();
        result.put("totalFound", invalidProducts.size());
        result.put("deleted", successCount);
        result.put("failed", errorCount);
        result.put("errors", errors);
        
        return result;
    }
```

---

## 4️⃣ ProductAController - Thêm API Endpoint

**File:** `src/main/java/com/poly/ASM/controller/admin/ProductAController.java`

Thêm endpoint mới (thêm vào sau các method hiện có):

```java
    /**
     * API xóa các sản phẩm có ảnh lỗi (null hoặc rỗng)
     * @return ResponseEntity với kết quả xóa
     */
    @DeleteMapping("/invalid-images")
    public ResponseEntity<ApiResponse<?>> deleteProductsWithInvalidImages() {
        Map<String, Object> result = productService.deleteProductsWithInvalidImage();
        
        int deleted = (int) result.get("deleted");
        int failed = (int) result.get("failed");
        List<Map<String, Object>> errors = (List<Map<String, Object>>) result.get("errors");
        
        // Tạo thông báo
        String message;
        if (deleted > 0 && failed == 0) {
            message = String.format("Đã xóa thành công %d sản phẩm có ảnh lỗi", deleted);
        } else if (deleted > 0 && failed > 0) {
            message = String.format("Đã xóa %d sản phẩm, %d sản phẩm không thể xóa do bị ràng buộc", 
                deleted, failed);
        } else if (failed > 0) {
            message = String.format("Không thể xóa %d sản phẩm do bị ràng buộc bởi đơn hàng", failed);
        } else {
            message = "Không tìm thấy sản phẩm nào có ảnh lỗi";
        }
        
        return ResponseEntity.ok(ApiResponse.success(message, result));
    }
    
    /**
     * API xem danh sách sản phẩm có ảnh lỗi (chưa xóa)
     * @return Danh sách sản phẩm có ảnh null hoặc rỗng
     */
    @GetMapping("/invalid-images")
    public ResponseEntity<ApiResponse<?>> getProductsWithInvalidImages() {
        List<Product> invalidProducts = productRepository.findProductsWithInvalidImage();
        
        // Chuyển đổi sang DTO để tránh vòng lặp JSON
        List<Map<String, Object>> productDTOs = invalidProducts.stream()
            .map(p -> {
                Map<String, Object> map = new HashMap<>();
                map.put("id", p.getId());
                map.put("name", p.getName());
                map.put("image", p.getImage());
                map.put("price", p.getPrice());
                map.put("quantity", p.getQuantity());
                map.put("available", p.getAvailable());
                map.put("categoryName", p.getCategory() != null ? p.getCategory().getName() : null);
                return map;
            })
            .toList();
        
        Map<String, Object> data = new HashMap<>();
        data.put("count", productDTOs.size());
        data.put("products", productDTOs);
        
        String message = productDTOs.isEmpty() 
            ? "Không có sản phẩm nào có ảnh lỗi" 
            : String.format("Tìm thấy %d sản phẩm có ảnh lỗi", productDTOs.size());
        
        return ResponseEntity.ok(ApiResponse.success(message, data));
    }
```

**Lưu ý:** Nếu `productRepository` chưa được inject trong Controller, thêm:

```java
private final ProductRepository productRepository;
```

Vào đầu class và đảm bảo constructor có:

```java
public ProductAController(ProductService productService, 
                          CategoryService categoryService,
                          SizeService sizeService,
                          ProductSizeService productSizeService,
                          ProductRepository productRepository) {
    this.productService = productService;
    this.categoryService = categoryService;
    this.sizeService = sizeService;
    this.productSizeService = productSizeService;
    this.productRepository = productRepository;
}
```

---

## 5️⃣ Frontend Vue 3 - Gọi API

**File:** `frontend/src/views/admin/AdminProductView.vue` hoặc tạo component mới

### API Service (api.js)

Thêm vào `api.admin.products`:

```javascript
admin: {
    products: {
        // ... existing methods ...
        deleteInvalidImages: () => request("/api/admin/products/invalid-images", {method: "DELETE"}),
        getInvalidImages: () => request("/api/admin/products/invalid-images"),
    }
}
```

### Component Vue

```vue
<template>
    <div class="card mb-3">
        <div class="card-header d-flex justify-content-between align-items-center">
            <h5 class="mb-0">Xóa Sản Phẩm Có Ảnh Lỗi</h5>
            <div>
                <button class="btn btn-info me-2" @click="checkInvalidProducts" :disabled="loading">
                    <span v-if="loading">Đang kiểm tra...</span>
                    <span v-else>Kiểm tra</span>
                </button>
                <button class="btn btn-danger" @click="deleteInvalidProducts" :disabled="loading || invalidCount === 0">
                    Xóa Sản Phẩm Lỗi
                </button>
            </div>
        </div>
        <div class="card-body">
            <div v-if="invalidProducts.length > 0" class="alert alert-warning">
                <strong>Tìm thấy {{ invalidCount }} sản phẩm có ảnh lỗi:</strong>
                <ul class="mb-0 mt-2">
                    <li v-for="p in invalidProducts" :key="p.id">
                        ID: {{ p.id }} - {{ p.name }} (Ảnh: {{ p.image || 'NULL' }})
                    </li>
                </ul>
            </div>
            <div v-else-if="checked" class="alert alert-success">
                Không có sản phẩm nào có ảnh lỗi!
            </div>
            
            <!-- Kết quả xóa -->
            <div v-if="deleteResult" class="mt-3">
                <div v-if="deleteResult.deleted > 0" class="alert alert-success">
                    ✅ Đã xóa {{ deleteResult.deleted }} sản phẩm thành công!
                </div>
                <div v-if="deleteResult.failed > 0" class="alert alert-danger">
                    ❌ Không thể xóa {{ deleteResult.failed }} sản phẩm:
                    <ul class="mb-0 mt-2">
                        <li v-for="err in deleteResult.errors" :key="err.productId">
                            {{ err.productName }}: {{ err.reason }}
                        </li>
                    </ul>
                </div>
            </div>
        </div>
    </div>
</template>

<script setup>
import { ref } from 'vue';
import { api } from '@/api';
import Swal from 'sweetalert2';

const loading = ref(false);
const checked = ref(false);
const invalidCount = ref(0);
const invalidProducts = ref([]);
const deleteResult = ref(null);

const checkInvalidProducts = async () => {
    loading.value = true;
    try {
        const res = await api.admin.products.getInvalidImages();
        invalidProducts.value = res.data?.products || [];
        invalidCount.value = res.data?.count || 0;
        checked.value = true;
    } catch (e) {
        Swal.fire('Lỗi', e.message, 'error');
    } finally {
        loading.value = false;
    }
};

const deleteInvalidProducts = async () => {
    const confirm = await Swal.fire({
        title: 'Xác nhận xóa?',
        html: `Bạn có chắc muốn xóa <strong>${invalidCount.value}</strong> sản phẩm có ảnh lỗi?<br>
               <span class="text-danger">Sản phẩm có đơn hàng sẽ không bị xóa!</span>`,
        icon: 'warning',
        showCancelButton: true,
        confirmButtonText: 'Xóa',
        cancelButtonText: 'Hủy',
        confirmButtonColor: '#dc3545'
    });
    
    if (!confirm.isConfirmed) return;
    
    loading.value = true;
    try {
        const res = await api.admin.products.deleteInvalidImages();
        deleteResult.value = res.data;
        
        // Reload danh sách nếu có sản phẩm bị xóa
        if (res.data?.deleted > 0) {
            await checkInvalidProducts();
        }
        
        Swal.fire({
            title: res.data?.deleted > 0 ? 'Thành công!' : 'Hoàn tất',
            text: res.message,
            icon: res.data?.failed > 0 ? 'warning' : 'success'
        });
    } catch (e) {
        Swal.fire('Lỗi', e.message, 'error');
    } finally {
        loading.value = false;
    }
};
</script>
```

---

## 🧪 Test API

### 1. Kiểm tra sản phẩm có ảnh lỗi

```bash
curl http://localhost:8080/api/admin/products/invalid-images
```

### 2. Xóa sản phẩm có ảnh lỗi

```bash
curl -X DELETE http://localhost:8080/api/admin/products/invalid-images
```

---

## 🔒 Lưu Ý Bảo Mật

1. **Chỉ Admin được phép gọi API** - Endpoint đã có trong `/api/admin/products`
2. **Luôn kiểm tra ràng buộc** trước khi xóa
3. **Log lại các sản phẩm bị xóa** để audit
4. **Soft delete (khuyến nghị)** - Set `isDelete = true` thay vì xóa vật lý

---

## 📝 SQL Kiểm Tra

```sql
-- Tìm sản phẩm có ảnh lỗi
SELECT id, name, image, price, quantity
FROM products
WHERE is_delete = 0
  AND (image IS NULL OR LTRIM(RTRIM(image)) = '');

-- Kiểm tra sản phẩm có đơn hàng không
SELECT p.id, p.name, COUNT(od.id) as order_count
FROM products p
LEFT JOIN order_details od ON p.id = od.product_id
WHERE p.id = ?
GROUP BY p.id, p.name;
```
