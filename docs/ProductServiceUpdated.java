// ============================================
// UPDATED PRODUCT SERVICE - FIX QUANTITY CALCULATION
// ============================================

package com.example.ecommerce.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.math.BigDecimal;
import java.util.*;
import java.util.stream.Collectors;

@Service
@Transactional
@RequiredArgsConstructor
public class ProductServiceImpl implements ProductService {
    
    private final ProductRepository productRepository;
    private final ProductVariantRepository variantRepository;
    private final AttributeRepository attributeRepository;
    private final AttributeValueRepository attributeValueRepository;
    private final VariantAttributeRepository variantAttributeRepository;
    private final SKUGenerator skuGenerator;
    
    // ============================================
    // CREATE PRODUCT - WITH QUANTITY CALCULATION
    // ============================================
    @Override
    @Transactional(rollbackFor = Exception.class)
    public ProductResponse createProduct(CreateProductRequest request) {
        // Validate product code unique
        if (productRepository.existsByProductCode(request.getProductCode())) {
            throw new RuntimeException("Product code already exists: " + request.getProductCode());
        }
        
        // Validate at least one variant
        if (request.getVariants() == null || request.getVariants().isEmpty()) {
            throw new RuntimeException("At least one variant is required");
        }
        
        // CALCULATE TOTAL QUANTITY FROM VARIANTS (Backend safety check)
        int calculatedTotalQuantity = request.getVariants().stream()
            .mapToInt(v -> v.getQuantity() != null ? v.getQuantity() : 0)
            .sum();
        
        // Use frontend totalQuantity if provided, otherwise use calculated
        int finalTotalQuantity = request.getTotalQuantity() != null 
            ? request.getTotalQuantity() 
            : calculatedTotalQuantity;
        
        // B1: Create Product with calculated quantity
        Product product = Product.builder()
            .productCode(request.getProductCode().toUpperCase())
            .name(request.getName())
            .description(request.getDescription())
            .basePrice(request.getBasePrice())
            .discountPercent(request.getDiscountPercent() != null ? request.getDiscountPercent() : BigDecimal.ZERO)
            .imageUrl(request.getImageUrl())
            .categoryId(request.getCategoryId())
            .isActive(true)
            .totalQuantity(finalTotalQuantity)  // SET CALCULATED QUANTITY
            .build();
        
        product = productRepository.save(product);
        final Long productId = product.getId();
        
        // B2: Process each variant
        Set<String> existingSKUs = new HashSet<>();
        Set<String> existingAttributeCombos = new HashSet<>();
        int variantTotalQty = 0; // Track sum for verification
        
        for (VariantRequest variantReq : request.getVariants()) {
            // Validate quantity
            if (variantReq.getQuantity() == null || variantReq.getQuantity() < 0) {
                throw new RuntimeException("Quantity must be >= 0");
            }
            
            variantTotalQty += variantReq.getQuantity();
            
            // Build attribute combo key to check duplicates
            String attrComboKey = buildAttributeComboKey(productId, variantReq.getAttributes());
            if (existingAttributeCombos.contains(attrComboKey)) {
                throw new RuntimeException("Duplicate variant attributes: " + variantReq.getAttributes());
            }
            existingAttributeCombos.add(attrComboKey);
            
            // Generate SKU
            String sku = skuGenerator.generateSKU(
                product.getProductCode(), 
                productId, 
                variantReq.getAttributes()
            );
            
            if (existingSKUs.contains(sku)) {
                throw new RuntimeException("Duplicate SKU generated: " + sku);
            }
            existingSKUs.add(sku);
            
            // B3: Create ProductVariant
            ProductVariant variant = ProductVariant.builder()
                .sku(sku)
                .quantity(variantReq.getQuantity())
                .priceAdjustment(variantReq.getPriceAdjustment() != null ? variantReq.getPriceAdjustment() : BigDecimal.ZERO)
                .product(product)  // BI-DIRECTIONAL MAPPING
                .build();
            
            variant = variantRepository.save(variant);
            
            // B4: Create VariantAttribute mappings
            for (Map.Entry<String, String> attrEntry : variantReq.getAttributes().entrySet()) {
                String attrName = attrEntry.getKey();
                String attrValue = attrEntry.getValue();
                
                // Find or create attribute
                Attribute attribute = attributeRepository.findByName(attrName)
                    .orElseGet(() -> {
                        Attribute newAttr = Attribute.builder()
                            .name(attrName)
                            .displayName(attrName.substring(0, 1).toUpperCase() + attrName.substring(1))
                            .build();
                        return attributeRepository.save(newAttr);
                    });
                
                // Find or create attribute value
                AttributeValue attributeValue = attributeValueRepository
                    .findByAttributeIdAndValue(attribute.getId(), attrValue)
                    .orElseGet(() -> {
                        AttributeValue newVal = AttributeValue.builder()
                            .attribute(attribute)
                            .value(attrValue)
                            .display(attrValue.substring(0, 1).toUpperCase() + attrValue.substring(1))
                            .build();
                        return attributeValueRepository.save(newVal);
                    });
                
                // Create mapping
                VariantAttribute va = VariantAttribute.builder()
                    .variant(variant)
                    .attributeValue(attributeValue)
                    .build();
                variantAttributeRepository.save(va);
            }
        }
        
        // VERIFY: Ensure product total quantity matches sum of variant quantities
        if (product.getTotalQuantity() != variantTotalQty) {
            // Auto-correct if mismatch
            product.setTotalQuantity(variantTotalQty);
            product = productRepository.save(product);
        }
        
        return mapToProductResponse(product);
    }
    
    // ============================================
    // UPDATE PRODUCT - WITH QUANTITY RECALCULATION
    // ============================================
    @Override
    @Transactional(rollbackFor = Exception.class)
    public ProductResponse updateProduct(Long id, CreateProductRequest request) {
        Product product = productRepository.findById(id)
            .orElseThrow(() -> new RuntimeException("Product not found: " + id));
        
        // CALCULATE TOTAL QUANTITY FROM VARIANTS
        int calculatedTotalQuantity = request.getVariants().stream()
            .mapToInt(v -> v.getQuantity() != null ? v.getQuantity() : 0)
            .sum();
        
        // Update product fields
        product.setName(request.getName());
        product.setDescription(request.getDescription());
        product.setBasePrice(request.getBasePrice());
        product.setDiscountPercent(request.getDiscountPercent() != null ? request.getDiscountPercent() : BigDecimal.ZERO);
        product.setImageUrl(request.getImageUrl());
        product.setCategoryId(request.getCategoryId());
        product.setTotalQuantity(calculatedTotalQuantity); // RECALCULATE QUANTITY
        
        // Delete existing variants and recreate
        List<ProductVariant> existingVariants = variantRepository.findByProductId(id);
        for (ProductVariant v : existingVariants) {
            variantAttributeRepository.deleteByVariantId(v.getId());
        }
        variantRepository.deleteByProductId(id);
        
        // Recreate variants (similar logic to create)
        Set<String> existingSKUs = new HashSet<>();
        Set<String> existingAttributeCombos = new HashSet<>();
        int variantTotalQty = 0;
        
        for (VariantRequest variantReq : request.getVariants()) {
            if (variantReq.getQuantity() == null || variantReq.getQuantity() < 0) {
                throw new RuntimeException("Quantity must be >= 0");
            }
            
            variantTotalQty += variantReq.getQuantity();
            
            String attrComboKey = buildAttributeComboKey(id, variantReq.getAttributes());
            if (existingAttributeCombos.contains(attrComboKey)) {
                throw new RuntimeException("Duplicate variant attributes");
            }
            existingAttributeCombos.add(attrComboKey);
            
            String sku = skuGenerator.generateSKU(product.getProductCode(), id, variantReq.getAttributes());
            if (existingSKUs.contains(sku)) {
                throw new RuntimeException("Duplicate SKU: " + sku);
            }
            existingSKUs.add(sku);
            
            ProductVariant variant = ProductVariant.builder()
                .sku(sku)
                .quantity(variantReq.getQuantity())
                .priceAdjustment(variantReq.getPriceAdjustment() != null ? variantReq.getPriceAdjustment() : BigDecimal.ZERO)
                .product(product)
                .build();
            
            variant = variantRepository.save(variant);
            
            // Create attribute mappings
            for (Map.Entry<String, String> attrEntry : variantReq.getAttributes().entrySet()) {
                Attribute attribute = attributeRepository.findByName(attrEntry.getKey())
                    .orElseThrow(() -> new RuntimeException("Attribute not found: " + attrEntry.getKey()));
                
                AttributeValue attributeValue = attributeValueRepository
                    .findByAttributeIdAndValue(attribute.getId(), attrEntry.getValue())
                    .orElseThrow(() -> new RuntimeException("Attribute value not found"));
                
                VariantAttribute va = VariantAttribute.builder()
                    .variant(variant)
                    .attributeValue(attributeValue)
                    .build();
                variantAttributeRepository.save(va);
            }
        }
        
        // Final verification
        if (product.getTotalQuantity() != variantTotalQty) {
            product.setTotalQuantity(variantTotalQty);
        }
        
        product = productRepository.save(product);
        return mapToProductResponse(product);
    }
    
    // Helper method to build attribute combo key
    private String buildAttributeComboKey(Long productId, Map<String, String> attributes) {
        return productId + "-" + attributes.entrySet().stream()
            .sorted(Map.Entry.comparingByKey())
            .map(e -> e.getKey() + ":" + e.getValue())
            .collect(Collectors.joining("-"));
    }
    
    // Mapping methods
    private ProductResponse mapToProductResponse(Product product) {
        // Implementation...
        return ProductResponse.builder()
            .id(product.getId())
            .productCode(product.getProductCode())
            .name(product.getName())
            .description(product.getDescription())
            .basePrice(product.getBasePrice())
            .discountPercent(product.getDiscountPercent())
            .finalPrice(calculateFinalPrice(product.getBasePrice(), product.getDiscountPercent()))
            .imageUrl(product.getImageUrl())
            .categoryId(product.getCategoryId())
            .totalQuantity(product.getTotalQuantity())
            .isActive(product.getIsActive())
            .variants(mapToVariantResponses(product.getVariants()))
            .build();
    }
    
    private BigDecimal calculateFinalPrice(BigDecimal basePrice, BigDecimal discountPercent) {
        if (basePrice == null) return BigDecimal.ZERO;
        if (discountPercent == null || discountPercent.compareTo(BigDecimal.ZERO) == 0) {
            return basePrice;
        }
        BigDecimal discountAmount = basePrice.multiply(discountPercent).divide(new BigDecimal("100"));
        return basePrice.subtract(discountAmount);
    }
    
    private List<VariantResponse> mapToVariantResponses(List<ProductVariant> variants) {
        if (variants == null) return new ArrayList<>();
        return variants.stream().map(v -> VariantResponse.builder()
            .id(v.getId())
            .sku(v.getSku())
            .quantity(v.getQuantity())
            .priceAdjustment(v.getPriceAdjustment())
            .attributes(mapVariantAttributes(v.getVariantAttributes()))
            .build()
        ).collect(Collectors.toList());
    }
    
    private Map<String, String> mapVariantAttributes(List<VariantAttribute> vas) {
        Map<String, String> attrs = new HashMap<>();
        if (vas != null) {
            for (VariantAttribute va : vas) {
                if (va.getAttributeValue() != null && va.getAttributeValue().getAttribute() != null) {
                    attrs.put(
                        va.getAttributeValue().getAttribute().getName(),
                        va.getAttributeValue().getValue()
                    );
                }
            }
        }
        return attrs;
    }
}

// ============================================
// UPDATED DTO - WITH totalQuantity FIELD
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
    
    // ADDED: Explicit total quantity field
    private Integer totalQuantity;
    
    @NotEmpty(message = "At least one variant is required")
    private List<VariantRequest> variants;
}
