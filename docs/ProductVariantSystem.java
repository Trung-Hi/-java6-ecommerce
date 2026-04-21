// ============================================
// PRODUCT VARIANT SYSTEM - JAVA SPRING BOOT
// Production-Ready Implementation
// ============================================

package com.example.ecommerce.product;

import jakarta.persistence.*;
import jakarta.validation.constraints.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

// ============================================
// ENTITY CLASSES
// ============================================

@Entity
@Table(name = "product")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Product {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @Column(name = "product_code", nullable = false, unique = true, length = 50)
    private String productCode;
    
    @Column(nullable = false, length = 200)
    private String name;
    
    @Column(columnDefinition = "NVARCHAR(MAX)")
    private String description;
    
    @Column(name = "base_price", nullable = false, precision = 18, scale = 2)
    private BigDecimal basePrice;
    
    @Column(name = "discount_percent", precision = 5, scale = 2)
    private BigDecimal discountPercent = BigDecimal.ZERO;
    
    @Column(name = "image_url", length = 500)
    private String imageUrl;
    
    @Column(name = "category_id", length = 50)
    private String categoryId;
    
    @Column(name = "is_active")
    private Boolean isActive = true;
    
    @Column(name = "total_quantity")
    private Integer totalQuantity = 0;
    
    @OneToMany(mappedBy = "product", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.LAZY)
    @Builder.Default
    private List<ProductVariant> variants = new ArrayList<>();
    
    @CreationTimestamp
    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt;
    
    @UpdateTimestamp
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;
    
    // Helper methods
    public void addVariant(ProductVariant variant) {
        variants.add(variant);
        variant.setProduct(this);
    }
    
    public void removeVariant(ProductVariant variant) {
        variants.remove(variant);
        variant.setProduct(null);
    }
    
    public BigDecimal getFinalPrice() {
        if (discountPercent == null || discountPercent.compareTo(BigDecimal.ZERO) == 0) {
            return basePrice;
        }
        BigDecimal discount = basePrice.multiply(discountPercent).divide(new BigDecimal("100"));
        return basePrice.subtract(discount);
    }
}

@Entity
@Table(name = "product_variant")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ProductVariant {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "product_id", nullable = false)
    private Product product;
    
    @Column(nullable = false, unique = true, length = 100)
    private String sku;
    
    @Column(name = "price_adjustment", precision = 18, scale = 2)
    private BigDecimal priceAdjustment = BigDecimal.ZERO;
    
    @Column(nullable = false)
    private Integer quantity = 0;
    
    @Column(name = "is_active")
    private Boolean isActive = true;
    
    @Column(length = 100)
    private String barcode;
    
    @Column(name = "weight_grams")
    private Integer weightGrams;
    
    @OneToMany(mappedBy = "variant", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.EAGER)
    @Builder.Default
    private List<VariantAttribute> variantAttributes = new ArrayList<>();
    
    @CreationTimestamp
    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt;
    
    @UpdateTimestamp
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;
    
    // Helper methods
    public void addVariantAttribute(VariantAttribute va) {
        variantAttributes.add(va);
        va.setVariant(this);
    }
    
    public BigDecimal getFinalPrice(BigDecimal basePrice) {
        return basePrice.add(priceAdjustment != null ? priceAdjustment : BigDecimal.ZERO);
    }
    
    public Map<String, String> getAttributesMap() {
        return variantAttributes.stream()
            .collect(Collectors.toMap(
                va -> va.getAttributeValue().getAttribute().getName(),
                va -> va.getAttributeValue().getValue(),
                (v1, v2) -> v1
            ));
    }
}

@Entity
@Table(name = "attribute")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Attribute {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @Column(nullable = false, unique = true, length = 50)
    private String name;  // 'size', 'color'
    
    @Column(name = "display_name", nullable = false, length = 50)
    private String displayName;  // 'Kích thước', 'Màu sắc'
    
    @Column(name = "data_type", length = 20)
    private String dataType = "string";
    
    @Column(name = "sort_order")
    private Integer sortOrder = 0;
    
    @Column(name = "is_active")
    private Boolean isActive = true;
    
    @OneToMany(mappedBy = "attribute", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    @Builder.Default
    private List<AttributeValue> values = new ArrayList<>();
    
    @CreationTimestamp
    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt;
}

@Entity
@Table(name = "attribute_value")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class AttributeValue {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "attribute_id", nullable = false)
    private Attribute attribute;
    
    @Column(nullable = false, length = 100)
    private String value;  // 'S', 'M', 'BLACK'
    
    @Column(name = "display_value", nullable = false, length = 100)
    private String displayValue;  // 'Nhỏ', 'Vừa', 'Đen'
    
    @Column(name = "hex_code", length = 7)
    private String hexCode;  // For colors
    
    @Column(name = "sort_order")
    private Integer sortOrder = 0;
    
    @Column(name = "is_active")
    private Boolean isActive = true;
    
    @CreationTimestamp
    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt;
}

@Entity
@Table(name = "variant_attribute")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class VariantAttribute {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "variant_id", nullable = false)
    private ProductVariant variant;
    
    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "attribute_value_id", nullable = false)
    private AttributeValue attributeValue;
    
    @CreationTimestamp
    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt;
}

// ============================================
// REPOSITORIES
// ============================================

@Repository
public interface ProductRepository extends JpaRepository<Product, Long> {
    Optional<Product> findByProductCode(String productCode);
    
    boolean existsByProductCode(String productCode);
    
    @Query("SELECT p FROM Product p LEFT JOIN FETCH p.variants WHERE p.id = :id")
    Optional<Product> findByIdWithVariants(@Param("id") Long id);
}

@Repository
public interface ProductVariantRepository extends JpaRepository<ProductVariant, Long> {
    List<ProductVariant> findByProductId(Long productId);
    
    Optional<ProductVariant> findBySku(String sku);
    
    boolean existsBySku(String sku);
    
    @Query("SELECT DISTINCT pv FROM ProductVariant pv " +
           "JOIN pv.variantAttributes va " +
           "JOIN va.attributeValue av " +
           "WHERE av.value = :sizeValue AND av.attribute.name = 'size'")
    List<ProductVariant> findBySize(@Param("sizeValue") String sizeValue);
}

@Repository
public interface AttributeRepository extends JpaRepository<Attribute, Long> {
    Optional<Attribute> findByName(String name);
    
    boolean existsByName(String name);
}

@Repository
public interface AttributeValueRepository extends JpaRepository<AttributeValue, Long> {
    Optional<AttributeValue> findByAttributeIdAndValue(Long attributeId, String value);
    
    List<AttributeValue> findByAttributeId(Long attributeId);
    
    @Query("SELECT av FROM AttributeValue av WHERE av.attribute.name = :attributeName AND av.value = :value")
    Optional<AttributeValue> findByAttributeNameAndValue(@Param("attributeName") String attributeName, 
                                                         @Param("value") String value);
}

@Repository
public interface VariantAttributeRepository extends JpaRepository<VariantAttribute, Long> {
    @Query("SELECT va FROM VariantAttribute va " +
           "WHERE va.variant.product.id = :productId " +
           "AND va.attributeValue.id IN :attributeValueIds")
    List<VariantAttribute> findByProductAndAttributeValues(@Param("productId") Long productId,
                                                            @Param("attributeValueIds") List<Long> attributeValueIds);
}

// ============================================
// DTO CLASSES
// ============================================

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
class CreateProductRequest {
    @NotBlank(message = "Product code is required")
    private String productCode;
    
    @NotBlank(message = "Name is required")
    private String name;
    
    private String description;
    
    @NotNull(message = "Base price is required")
    @Min(0)
    private BigDecimal basePrice;
    
    @Min(0)
    @Max(100)
    private BigDecimal discountPercent;
    
    private String imageUrl;
    private String categoryId;
    
    @NotEmpty(message = "At least one variant is required")
    private List<VariantRequest> variants;
}

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
class VariantRequest {
    @Min(0)
    private Integer quantity;
    
    private BigDecimal priceAdjustment;
    
    @NotEmpty(message = "Variant attributes are required")
    private Map<String, String> attributes;  // {"size": "M", "color": "black"}
}

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
class ProductResponse {
    private Long id;
    private String productCode;
    private String name;
    private String description;
    private BigDecimal basePrice;
    private BigDecimal discountPercent;
    private BigDecimal finalPrice;
    private String imageUrl;
    private String categoryId;
    private Integer totalQuantity;
    private Boolean isActive;
    private List<VariantResponse> variants;
}

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
class VariantResponse {
    private Long id;
    private String sku;
    private BigDecimal priceAdjustment;
    private BigDecimal finalPrice;
    private Integer quantity;
    private Boolean isActive;
    private String barcode;
    private List<AttributeValueResponse> attributes;
}

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
class AttributeValueResponse {
    private String attributeName;
    private String attributeDisplayName;
    private String value;
    private String displayValue;
    private String hexCode;
}

// ============================================
// SKU GENERATOR SERVICE
// ============================================

interface SKUGenerator {
    String generateSKU(String productCode, Map<String, String> attributes);
}

@Service
class DefaultSKUGenerator implements SKUGenerator {
    
    @Override
    public String generateSKU(String productCode, Map<String, String> attributes) {
        StringBuilder sku = new StringBuilder(productCode.toUpperCase());
        
        // Sort attributes by name for consistent SKU
        attributes.entrySet().stream()
            .sorted(Map.Entry.comparingByKey())
            .forEach(entry -> {
                sku.append("-").append(entry.getValue().toUpperCase());
            });
        
        return sku.toString();
    }
}

// ============================================
// PRODUCT SERVICE - CORE BUSINESS LOGIC
// ============================================

interface ProductService {
    ProductResponse createProduct(CreateProductRequest request);
    ProductResponse getProduct(Long id);
    List<ProductResponse> getAllProducts();
    ProductResponse updateProduct(Long id, CreateProductRequest request);
    void deleteProduct(Long id);
}

@Service
@Transactional
@RequiredArgsConstructor
class ProductServiceImpl implements ProductService {
    
    private final ProductRepository productRepository;
    private final ProductVariantRepository variantRepository;
    private final AttributeRepository attributeRepository;
    private final AttributeValueRepository attributeValueRepository;
    private final VariantAttributeRepository variantAttributeRepository;
    private final SKUGenerator skuGenerator;
    
    // ============================================
    // FLOW: CREATE PRODUCT WITH VARIANTS
    // ============================================
    @Override
    @Transactional(rollbackFor = Exception.class)
    public ProductResponse createProduct(CreateProductRequest request) {
        // Validate product code unique
        if (productRepository.existsByProductCode(request.getProductCode())) {
            throw new RuntimeException("Product code already exists: " + request.getProductCode());
        }
        
        // B1: Create Product
        Product product = Product.builder()
            .productCode(request.getProductCode().toUpperCase())
            .name(request.getName())
            .description(request.getDescription())
            .basePrice(request.getBasePrice())
            .discountPercent(request.getDiscountPercent())
            .imageUrl(request.getImageUrl())
            .categoryId(request.getCategoryId())
            .isActive(true)
            .build();
        
        product = productRepository.save(product);
        final Long productId = product.getId();
        
        // B2: Process each variant
        Set<String> existingSKUs = new HashSet<>();
        Set<String> existingAttributeCombos = new HashSet<>();
        
        for (VariantRequest variantReq : request.getVariants()) {
            // Validate quantity
            if (variantReq.getQuantity() == null || variantReq.getQuantity() < 0) {
                throw new RuntimeException("Quantity must be >= 0");
            }
            
            // Build attribute combo key to check duplicates
            String attrComboKey = buildAttributeComboKey(productId, variantReq.getAttributes());
            if (existingAttributeCombos.contains(attrComboKey)) {
                throw new RuntimeException("Duplicate variant attributes: " + variantReq.getAttributes());
            }
            existingAttributeCombos.add(attrComboKey);
            
            // Generate SKU
            String baseSku = skuGenerator.generateSKU(request.getProductCode(), variantReq.getAttributes());
            String uniqueSku = generateUniqueSKU(baseSku, existingSKUs);
            existingSKUs.add(uniqueSku);
            
            // B3: Create Variant
            ProductVariant variant = ProductVariant.builder()
                .product(product)
                .sku(uniqueSku)
                .priceAdjustment(variantReq.getPriceAdjustment())
                .quantity(variantReq.getQuantity())
                .barcode(uniqueSku)  // Default barcode = SKU
                .isActive(true)
                .build();
            
            product.addVariant(variant);
            
            // B4: Map Attributes (find or create)
            for (Map.Entry<String, String> attrEntry : variantReq.getAttributes().entrySet()) {
                String attrName = attrEntry.getKey();
                String attrValue = attrEntry.getValue();
                
                // Find or create Attribute
                Attribute attribute = attributeRepository.findByName(attrName)
                    .orElseThrow(() -> new RuntimeException("Attribute not found: " + attrName));
                
                // Find or create AttributeValue
                AttributeValue attributeValue = attributeValueRepository
                    .findByAttributeIdAndValue(attribute.getId(), attrValue)
                    .orElseThrow(() -> new RuntimeException(
                        "Attribute value not found: " + attrName + "=" + attrValue));
                
                // Create mapping
                VariantAttribute va = VariantAttribute.builder()
                    .variant(variant)
                    .attributeValue(attributeValue)
                    .build();
                
                variant.addVariantAttribute(va);
            }
        }
        
        // Recalculate total quantity
        int totalQty = product.getVariants().stream()
            .mapToInt(v -> v.getQuantity())
            .sum();
        product.setTotalQuantity(totalQty);
        
        product = productRepository.save(product);
        
        return mapToProductResponse(product);
    }
    
    // Helper: Build attribute combo key for duplicate checking
    private String buildAttributeComboKey(Long productId, Map<String, String> attributes) {
        return productId + ":" + attributes.entrySet().stream()
            .sorted(Map.Entry.comparingByKey())
            .map(e -> e.getKey() + "=" + e.getValue())
            .collect(Collectors.joining("&"));
    }
    
    // Helper: Generate unique SKU (handle concurrent requests)
    private String generateUniqueSKU(String baseSku, Set<String> existingSkus) {
        String sku = baseSku;
        int counter = 1;
        
        while (existingSkus.contains(sku) || variantRepository.existsBySku(sku)) {
            sku = baseSku + "-" + counter;
            counter++;
        }
        
        return sku;
    }
    
    @Override
    @Transactional(readOnly = true)
    public ProductResponse getProduct(Long id) {
        Product product = productRepository.findByIdWithVariants(id)
            .orElseThrow(() -> new RuntimeException("Product not found: " + id));
        return mapToProductResponse(product);
    }
    
    @Override
    @Transactional(readOnly = true)
    public List<ProductResponse> getAllProducts() {
        return productRepository.findAll().stream()
            .map(this::mapToProductResponse)
            .collect(Collectors.toList());
    }
    
    @Override
    @Transactional(rollbackFor = Exception.class)
    public ProductResponse updateProduct(Long id, CreateProductRequest request) {
        Product product = productRepository.findById(id)
            .orElseThrow(() -> new RuntimeException("Product not found: " + id));
        
        // Update basic fields
        product.setName(request.getName());
        product.setDescription(request.getDescription());
        product.setBasePrice(request.getBasePrice());
        product.setDiscountPercent(request.getDiscountPercent());
        product.setImageUrl(request.getImageUrl());
        product.setCategoryId(request.getCategoryId());
        
        // Note: Variant updates would be handled separately for better control
        
        product = productRepository.save(product);
        return mapToProductResponse(product);
    }
    
    @Override
    @Transactional
    public void deleteProduct(Long id) {
        if (!productRepository.existsById(id)) {
            throw new RuntimeException("Product not found: " + id);
        }
        productRepository.deleteById(id);
    }
    
    // Mapper
    private ProductResponse mapToProductResponse(Product product) {
        return ProductResponse.builder()
            .id(product.getId())
            .productCode(product.getProductCode())
            .name(product.getName())
            .description(product.getDescription())
            .basePrice(product.getBasePrice())
            .discountPercent(product.getDiscountPercent())
            .finalPrice(product.getFinalPrice())
            .imageUrl(product.getImageUrl())
            .categoryId(product.getCategoryId())
            .totalQuantity(product.getTotalQuantity())
            .isActive(product.getIsActive())
            .variants(product.getVariants().stream()
                .map(this::mapToVariantResponse)
                .collect(Collectors.toList()))
            .build();
    }
    
    private VariantResponse mapToVariantResponse(ProductVariant variant) {
        return VariantResponse.builder()
            .id(variant.getId())
            .sku(variant.getSku())
            .priceAdjustment(variant.getPriceAdjustment())
            .finalPrice(variant.getFinalPrice(variant.getProduct().getBasePrice()))
            .quantity(variant.getQuantity())
            .isActive(variant.getIsActive())
            .barcode(variant.getBarcode())
            .attributes(variant.getVariantAttributes().stream()
                .map(va -> AttributeValueResponse.builder()
                    .attributeName(va.getAttributeValue().getAttribute().getName())
                    .attributeDisplayName(va.getAttributeValue().getAttribute().getDisplayName())
                    .value(va.getAttributeValue().getValue())
                    .displayValue(va.getAttributeValue().getDisplayValue())
                    .hexCode(va.getAttributeValue().getHexCode())
                    .build())
                .collect(Collectors.toList()))
            .build();
    }
}

// ============================================
// REST CONTROLLER
// ============================================

@RestController
@RequestMapping("/api/products")
@RequiredArgsConstructor
class ProductController {
    
    private final ProductService productService;
    
    @PostMapping
    public ResponseEntity<ProductResponse> createProduct(
            @Valid @RequestBody CreateProductRequest request) {
        ProductResponse response = productService.createProduct(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }
    
    @GetMapping("/{id}")
    public ResponseEntity<ProductResponse> getProduct(@PathVariable Long id) {
        return ResponseEntity.ok(productService.getProduct(id));
    }
    
    @GetMapping
    public ResponseEntity<List<ProductResponse>> getAllProducts() {
        return ResponseEntity.ok(productService.getAllProducts());
    }
    
    @PutMapping("/{id}")
    public ResponseEntity<ProductResponse> updateProduct(
            @PathVariable Long id,
            @Valid @RequestBody CreateProductRequest request) {
        return ResponseEntity.ok(productService.updateProduct(id, request));
    }
    
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteProduct(@PathVariable Long id) {
        productService.deleteProduct(id);
        return ResponseEntity.noContent().build();
    }
}

// ============================================
// EXCEPTION HANDLER
// ============================================

@RestControllerAdvice
class GlobalExceptionHandler {
    
    @ExceptionHandler(RuntimeException.class)
    public ResponseEntity<Map<String, Object>> handleRuntimeException(RuntimeException ex) {
        Map<String, Object> error = new HashMap<>();
        error.put("error", "Business Error");
        error.put("message", ex.getMessage());
        error.put("timestamp", LocalDateTime.now());
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(error);
    }
}
