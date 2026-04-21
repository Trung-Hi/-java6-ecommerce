// ============================================
// UPDATED PRODUCT ENTITY - WITH totalQuantity & CASCADE MAPPING
// ============================================

package com.example.ecommerce.entity;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "products")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Product {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @Column(name = "product_code", nullable = false, unique = true, length = 50)
    private String productCode;
    
    @Column(name = "name", nullable = false, length = 255)
    private String name;
    
    @Column(name = "description", columnDefinition = "TEXT")
    private String description;
    
    @Column(name = "base_price", nullable = false, precision = 12, scale = 2)
    private BigDecimal basePrice;
    
    @Column(name = "discount_percent", precision = 5, scale = 2)
    @Builder.Default
    private BigDecimal discountPercent = BigDecimal.ZERO;
    
    @Column(name = "image_url", length = 500)
    private String imageUrl;
    
    @Column(name = "category_id", length = 50)
    private String categoryId;
    
    // CRITICAL: Total quantity field - must be set when creating product
    @Column(name = "total_quantity", nullable = false)
    @Builder.Default
    private Integer totalQuantity = 0;
    
    @Column(name = "is_active", nullable = false)
    @Builder.Default
    private Boolean isActive = true;
    
    // BI-DIRECTIONAL MAPPING: Product -> Variants
    // Cascade ensures variants are managed with product
    // orphanRemoval deletes variants when removed from list
    @OneToMany(mappedBy = "product", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.LAZY)
    @Builder.Default
    private List<ProductVariant> variants = new ArrayList<>();
    
    @CreationTimestamp
    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt;
    
    @UpdateTimestamp
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;
    
    // Helper method to add variant (maintains bi-directional relationship)
    public void addVariant(ProductVariant variant) {
        variants.add(variant);
        variant.setProduct(this);
    }
    
    // Helper method to remove variant
    public void removeVariant(ProductVariant variant) {
        variants.remove(variant);
        variant.setProduct(null);
    }
    
    // Calculate final price after discount
    public BigDecimal getFinalPrice() {
        if (basePrice == null) return BigDecimal.ZERO;
        if (discountPercent == null || discountPercent.compareTo(BigDecimal.ZERO) == 0) {
            return basePrice;
        }
        BigDecimal discountAmount = basePrice.multiply(discountPercent).divide(new BigDecimal("100"), 2, BigDecimal.ROUND_HALF_UP);
        return basePrice.subtract(discountAmount);
    }
    
    // Recalculate total quantity from variants
    public void recalculateTotalQuantity() {
        if (variants != null) {
            this.totalQuantity = variants.stream()
                .mapToInt(ProductVariant::getQuantity)
                .sum();
        } else {
            this.totalQuantity = 0;
        }
    }
}

// ============================================
// PRODUCT VARIANT ENTITY - BI-DIRECTIONAL MAPPING
// ============================================

@Entity
@Table(name = "product_variants")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ProductVariant {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @Column(name = "sku", nullable = false, unique = true, length = 100)
    private String sku;
    
    @Column(name = "quantity", nullable = false)
    @Builder.Default
    private Integer quantity = 0;
    
    @Column(name = "price_adjustment", precision = 12, scale = 2)
    @Builder.Default
    private BigDecimal priceAdjustment = BigDecimal.ZERO;
    
    // BI-DIRECTIONAL: Many variants belong to one product
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "product_id", nullable = false)
    private Product product;
    
    // One variant has many attribute mappings
    @OneToMany(mappedBy = "variant", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.LAZY)
    @Builder.Default
    private List<VariantAttribute> variantAttributes = new ArrayList<>();
    
    @CreationTimestamp
    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt;
    
    @UpdateTimestamp
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;
    
    // Helper to get actual price (base + adjustment)
    public BigDecimal getActualPrice(BigDecimal basePrice) {
        if (basePrice == null) return BigDecimal.ZERO;
        if (priceAdjustment == null) return basePrice;
        return basePrice.add(priceAdjustment);
    }
    
    // Helper to add attribute mapping
    public void addVariantAttribute(VariantAttribute va) {
        variantAttributes.add(va);
        va.setVariant(this);
    }
}

// ============================================
// VARIANT ATTRIBUTE ENTITY - LINKING TABLE
// ============================================

@Entity
@Table(name = "variant_attributes")
@Getter
@Setter
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
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "attribute_value_id", nullable = false)
    private AttributeValue attributeValue;
    
    @CreationTimestamp
    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt;
}

// ============================================
// REPOSITORY - ADD DELETE METHODS FOR UPDATE
// ============================================

interface ProductVariantRepository extends JpaRepository<ProductVariant, Long> {
    List<ProductVariant> findByProductId(Long productId);
    
    // DELETE for update operation
    @Modifying
    @Query("DELETE FROM ProductVariant pv WHERE pv.product.id = :productId")
    void deleteByProductId(@Param("productId") Long productId);
    
    boolean existsBySku(String sku);
}

interface VariantAttributeRepository extends JpaRepository<VariantAttribute, Long> {
    @Modifying
    @Query("DELETE FROM VariantAttribute va WHERE va.variant.id = :variantId")
    void deleteByVariantId(@Param("variantId") Long variantId);
}
