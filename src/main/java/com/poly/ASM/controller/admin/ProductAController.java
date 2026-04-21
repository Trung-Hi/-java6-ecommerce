package com.poly.ASM.controller.admin;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.poly.ASM.dto.common.ApiResponse;
import com.poly.ASM.entity.product.Product;
import com.poly.ASM.entity.product.ProductSize;
import com.poly.ASM.entity.product.Size;
import com.poly.ASM.exception.ApiException;
import com.poly.ASM.service.product.CategoryService;
import com.poly.ASM.service.product.ProductService;
import com.poly.ASM.service.product.ProductSizeService;
import com.poly.ASM.service.product.SizeService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.math.BigDecimal;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import java.util.HashMap;

@RestController
@RequestMapping("/api/admin/products")
@RequiredArgsConstructor
public class ProductAController {

    private static final int PAGE_SIZE = 5;

    private final ProductService productService;
    private final CategoryService categoryService;
    private final SizeService sizeService;
    private final ProductSizeService productSizeService;
    private final ObjectMapper objectMapper = new ObjectMapper();

    @GetMapping
    public ResponseEntity<ApiResponse<?>> index(@RequestParam(value = "page", defaultValue = "0") int page,
                                                 @RequestParam(value = "keyword", required = false) String keyword,
                                                 @RequestParam(value = "categoryId", required = false) String categoryId,
                                                 @RequestParam(value = "minPrice", required = false) BigDecimal minPrice,
                                                 @RequestParam(value = "maxPrice", required = false) BigDecimal maxPrice) {
        boolean hasFilter = (keyword != null && !keyword.isBlank())
                || (categoryId != null && !categoryId.isBlank())
                || minPrice != null
                || maxPrice != null;
        Page<Product> productsPage = hasFilter
                ? productService.searchWithFiltersPage(keyword, categoryId, minPrice, maxPrice, null, page, PAGE_SIZE)
                : productService.findAllPage(page, PAGE_SIZE);
        Map<String, Object> data = new HashMap<>();
        data.put("products", productsPage.getContent());
        data.put("currentPage", productsPage.getNumber());
        data.put("totalPages", productsPage.getTotalPages());
        data.put("categories", categoryService.findAll());
        data.put("sizes", sizeService.findAll());
        data.put("keyword", keyword);
        data.put("categoryId", categoryId);
        data.put("minPrice", minPrice);
        data.put("maxPrice", maxPrice);
        return ResponseEntity.ok(ApiResponse.success("Lấy danh sách sản phẩm quản trị thành công", data));
    }

    @PostMapping
    public ResponseEntity<ApiResponse<?>> create(@RequestParam("name") String name,
                                                  @RequestParam("price") BigDecimal price,
                                                  @RequestParam(value = "discount", required = false) BigDecimal discount,
                                                  @RequestParam(value = "imageFile", required = false) MultipartFile imageFile,
                                                  @RequestParam(value = "description", required = false) String description,
                                                  @RequestParam(value = "categoryId", required = false) String categoryId,
                                                  @RequestParam(value = "quantity", required = false) Integer quantity,
                                                  @RequestParam(value = "totalQuantity", required = false) Integer totalQuantity,
                                                  @RequestParam(value = "variants", required = false) String variantsJson,
                                                  @RequestParam Map<String, String> params) {
        if (categoryId == null || categoryId.isBlank()) {
            throw new ApiException(HttpStatus.BAD_REQUEST, "Vui lòng chọn danh mục cho sản phẩm.");
        }

        Product product = new Product();
        product.setName(name);
        product.setPrice(price);
        product.setDiscount(discount);
        String imageName = saveImage(imageFile);
        if (imageName != null) {
            product.setImage(imageName);
        }
        product.setDescription(description);
        categoryService.findById(categoryId).ifPresent(product::setCategory);

        // Xử lý variants từ frontend (format mới: [{sizeName: 'M', quantity: 10}, ...])
        List<ProductSize> productSizes = new ArrayList<>();
        List<Map<String, Object>> variants = null;

        if (variantsJson != null && !variantsJson.isBlank()) {
            try {
                variants = objectMapper.readValue(variantsJson, new TypeReference<List<Map<String, Object>>>() {});
                List<Size> allSizes = sizeService.findAll();
                Map<String, Size> sizeByName = new HashMap<>();
                for (Size s : allSizes) {
                    if (s.getName() != null) {
                        sizeByName.put(s.getName(), s);
                    }
                }

                for (Map<String, Object> variant : variants) {
                    Object sizeNameObj = variant.get("sizeName");
                    Object qtyObj = variant.get("quantity");

                    if (sizeNameObj == null || qtyObj == null) {
                        continue;
                    }

                    String sizeName = sizeNameObj.toString();
                    int qty = ((Number) qtyObj).intValue();

                    Size size = sizeByName.get(sizeName);
                    if (size == null) {
                        // Auto-create size if not exists
                        Size newSize = new Size();
                        newSize.setName(sizeName);
                        size = sizeService.save(newSize);
                        sizeByName.put(sizeName, size);
                    }

                    ProductSize productSize = new ProductSize();
                    productSize.setProduct(product);
                    productSize.setSize(size);
                    productSize.setQuantity(qty);
                    productSizes.add(productSize);
                }
            } catch (Exception e) {
                System.err.println("Failed to parse variants JSON: " + e.getMessage());
                e.printStackTrace();
            }
        }

        // Nếu không có variants từ frontend, dùng legacy sizeQtyMap
        if (productSizes.isEmpty()) {
            Map<Integer, Integer> sizeQtyMap = parseSizeQuantities(params);
            List<Size> allSizes = sizeService.findAll();
            for (Size size : allSizes) {
                int qty = sizeQtyMap.getOrDefault(size.getId(), 0);
                if (qty > 0) {
                    ProductSize productSize = new ProductSize();
                    productSize.setProduct(product);
                    productSize.setSize(size);
                    productSize.setQuantity(qty);
                    productSizes.add(productSize);
                }
            }
        }

        product.setProductSizes(productSizes);
        Product saved = productService.saveProductWithVariants(product);

        return ResponseEntity.status(HttpStatus.CREATED).body(ApiResponse.success("Tạo sản phẩm thành công", null));
    }

    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<?>> edit(@PathVariable("id") Integer id,
                                                @RequestParam(value = "page", defaultValue = "0") int page,
                                                @RequestParam(value = "keyword", required = false) String keyword,
                                                @RequestParam(value = "categoryId", required = false) String categoryId,
                                                @RequestParam(value = "minPrice", required = false) BigDecimal minPrice,
                                                @RequestParam(value = "maxPrice", required = false) BigDecimal maxPrice) {
        Optional<Product> product = productService.findByIdWithSizes(id);
        if (product.isEmpty()) {
            throw new ApiException(HttpStatus.NOT_FOUND, "Không tìm thấy sản phẩm");
        }
        boolean hasFilter = (keyword != null && !keyword.isBlank())
                || (categoryId != null && !categoryId.isBlank())
                || minPrice != null
                || maxPrice != null;
        Page<Product> productsPage = hasFilter
                ? productService.searchWithFiltersPage(keyword, categoryId, minPrice, maxPrice, null, page, PAGE_SIZE)
                : productService.findAllPage(page, PAGE_SIZE);
        Product current = product.get();
        Map<String, Object> data = new HashMap<>();
        data.put("product", current);
        data.put("sizeQtyMap", buildSizeQtyMap(current));
        data.put("products", productsPage.getContent());
        data.put("currentPage", productsPage.getNumber());
        data.put("totalPages", productsPage.getTotalPages());
        data.put("categories", categoryService.findAll());
        data.put("sizes", sizeService.findAll());
        data.put("keyword", keyword);
        data.put("categoryId", categoryId);
        data.put("minPrice", minPrice);
        data.put("maxPrice", maxPrice);
        return ResponseEntity.ok(ApiResponse.success("Lấy chi tiết sản phẩm quản trị thành công", data));
    }

    @PutMapping("/{id}")
    public ResponseEntity<ApiResponse<?>> update(@PathVariable("id") Integer id,
                                                  @RequestParam("name") String name,
                                                  @RequestParam("price") BigDecimal price,
                                                  @RequestParam(value = "discount", required = false) BigDecimal discount,
                                                  @RequestParam(value = "imageFile", required = false) MultipartFile imageFile,
                                                  @RequestParam(value = "description", required = false) String description,
                                                  @RequestParam(value = "categoryId", required = false) String categoryId,
                                                  @RequestParam(value = "quantity", required = false) Integer quantity,
                                                  @RequestParam(value = "totalQuantity", required = false) Integer totalQuantity,
                                                  @RequestParam(value = "variants", required = false) String variantsJson,
                                                  @RequestParam Map<String, String> params) {
        if (categoryId == null || categoryId.isBlank()) {
            throw new ApiException(HttpStatus.BAD_REQUEST, "Vui lòng chọn danh mục cho sản phẩm.");
        }
        
        // PRIORITY 1: Use new variants system if provided
        int finalQuantity = 0;
        List<Map<String, Object>> variants = null;
        
        // Try to parse variants JSON if provided
        if (variantsJson != null && !variantsJson.isBlank()) {
            try {
                variants = objectMapper.readValue(variantsJson, new TypeReference<List<Map<String, Object>>>() {});
                // Calculate total from variants
                finalQuantity = variants.stream()
                    .mapToInt(v -> {
                        Object qty = v.get("quantity");
                        return qty != null ? ((Number) qty).intValue() : 0;
                    })
                    .sum();
                System.out.println("Update using variants JSON, calculated total: " + finalQuantity);
            } catch (Exception e) {
                System.err.println("Failed to parse variants JSON in update: " + e.getMessage());
            }
        }
        
        // If variants parsing failed or not provided, use explicit quantity
        if (finalQuantity == 0 && quantity != null && quantity > 0) {
            finalQuantity = quantity;
            System.out.println("Update using frontend quantity: " + finalQuantity);
        } else if (finalQuantity == 0 && totalQuantity != null && totalQuantity > 0) {
            finalQuantity = totalQuantity;
            System.out.println("Update using frontend totalQuantity: " + finalQuantity);
        }
        
        // If still 0, fallback to legacy sizeQtyMap
        if (finalQuantity == 0) {
            Map<Integer, Integer> sizeQtyMap = parseSizeQuantities(params);
            finalQuantity = sizeQtyMap.values().stream().mapToInt(Integer::intValue).sum();
            System.out.println("Update using legacy sizeQtyMap, total: " + finalQuantity);
        }
        
        Product product = productService.findById(id)
                .orElseThrow(() -> new ApiException(HttpStatus.NOT_FOUND, "Không tìm thấy sản phẩm"));
        product.setName(name);
        product.setPrice(price);
        product.setDiscount(discount);
        product.setAvailable(finalQuantity > 0);
        product.setQuantity(finalQuantity);
        String imageName = saveImage(imageFile);
        if (imageName != null) {
            product.setImage(imageName);
        }
        product.setDescription(description);
        categoryService.findById(categoryId).ifPresent(product::setCategory);
        Product saved = productService.update(product);
        
        // If using legacy system, update sizes
        if (variantsJson == null || variantsJson.isBlank()) {
            Map<Integer, Integer> sizeQtyMap = parseSizeQuantities(params);
            productSizeService.deleteByProductId(saved.getId());
            if (!sizeQtyMap.isEmpty()) {
                saveProductSizes(saved, sizeQtyMap);
            }
        }
        // Sync variants to product_sizes for UI display
        if (variants != null && !variants.isEmpty()) {
            List<Size> allSizes = sizeService.findAll();
            syncVariantsToProductSizes(saved, variants, allSizes);
        }
        
        return ResponseEntity.ok(ApiResponse.success("Cập nhật sản phẩm thành công", null));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<ApiResponse<?>> delete(@PathVariable("id") Integer id) {
        productService.deleteById(id);
        return ResponseEntity.ok(ApiResponse.success("Xóa sản phẩm thành công", null));
    }

    private String saveImage(MultipartFile file) {
        if (file == null || file.isEmpty()) {
            return null;
        }
        String original = file.getOriginalFilename();
        String ext = "";
        if (original != null && original.contains(".")) {
            ext = original.substring(original.lastIndexOf("."));
        }
        String fileName = "product-" + UUID.randomUUID() + ext;
        Path uploadDir = Path.of("uploads");
        try {
            Files.createDirectories(uploadDir);
            Files.write(uploadDir.resolve(fileName), file.getBytes());
            return fileName;
        } catch (IOException e) {
            return null;
        }
    }

    private void saveProductSizes(Product product, Map<Integer, Integer> sizeQtyMap) {
        List<Size> sizes = sizeService.findAll();
        for (Size size : sizes) {
            int qty = sizeQtyMap.getOrDefault(size.getId(), 0);
            if (qty <= 0) {
                continue;
            }
            ProductSize productSize = new ProductSize();
            productSize.setProduct(product);
            productSize.setSize(size);
            productSize.setQuantity(qty);
            productSizeService.save(productSize);
        }
    }

    private Map<Integer, Integer> parseSizeQuantities(Map<String, String> params) {
        List<Size> sizes = sizeService.findAll();
        Map<Integer, Integer> sizeQtyMap = new HashMap<>();
        for (Size size : sizes) {
            String key = "size_" + size.getId();
            if (!params.containsKey(key)) {
                continue;
            }
            int qty;
            try {
                qty = Integer.parseInt(params.get(key));
            } catch (NumberFormatException e) {
                qty = 0;
            }
            sizeQtyMap.put(size.getId(), Math.max(0, qty));
        }
        return sizeQtyMap;
    }

    private Map<Integer, Integer> buildSizeQtyMap(Product product) {
        if (product == null || product.getProductSizes() == null) {
            return Map.of();
        }
        Map<Integer, Integer> map = new java.util.HashMap<>();
        for (ProductSize ps : product.getProductSizes()) {
            if (ps.getSize() != null) {
                map.put(ps.getSize().getId(), ps.getQuantity());
            }
        }
        return map;
    }

    private void syncVariantsToProductSizes(Product product, List<Map<String, Object>> variants, List<Size> allSizes) {
        if (product == null || variants == null || variants.isEmpty()) {
            return;
        }
        Integer productId = product.getId();
        
        // Build size name -> size map
        Map<String, Size> sizeByName = new HashMap<>();
        for (Size s : allSizes) {
            if (s.getName() != null) {
                sizeByName.put(s.getName().toUpperCase(), s);
            }
        }
        
        // Delete all old product sizes for this product
        productSizeService.deleteByProductId(productId);
        System.out.println("Deleted old product_sizes for product " + productId);
        
        // Save new variants as product sizes
        int savedCount = 0;
        int createdCount = 0;
        for (Map<String, Object> variant : variants) {
            Object qtyObj = variant.get("quantity");
            if (qtyObj == null) continue;
            int qty = ((Number) qtyObj).intValue();
            
            // Frontend sends format: {sizeName: 'M', quantity: 10}
            Object sizeNameObj = variant.get("sizeName");
            if (sizeNameObj == null) {
                // Fallback to old format if exists
                @SuppressWarnings("unchecked")
                Map<String, Object> attrs = (Map<String, Object>) variant.get("attributes");
                if (attrs != null) {
                    sizeNameObj = attrs.get("size");
                }
            }
            if (sizeNameObj == null) continue;
            String sizeName = sizeNameObj.toString().toUpperCase();
            
            Size size = sizeByName.get(sizeName);
            // Auto-create size if not exists
            if (size == null) {
                System.out.println("Size not found: " + sizeName + " - Creating new size...");
                Size newSize = new Size();
                newSize.setName(sizeName);
                size = sizeService.save(newSize);
                sizeByName.put(sizeName, size);
                createdCount++;
            }
            
            ProductSize ps = new ProductSize();
            ps.setProduct(product);
            ps.setSize(size);
            ps.setQuantity(qty);
            
            productSizeService.save(ps);
            savedCount++;
        }
        System.out.println("Synced " + savedCount + " variants, created " + createdCount + " new sizes for product " + productId);
    }
}
