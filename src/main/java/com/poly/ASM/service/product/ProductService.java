package com.poly.ASM.service.product;

import com.poly.ASM.entity.product.Product;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;
import org.springframework.data.domain.Page;

public interface ProductService {

    List<Product> findAll();

    Page<Product> findAllPage(int page, int size);

    Optional<Product> findById(Integer id);

    Optional<Product> findByIdWithSizes(Integer id);

    List<Product> findTop8ByOrderByCreateDateDesc();

    List<Product> findTop8ByDiscountGreaterThanOrderByDiscountDesc(double discount);

    List<Product> findTop8BestSeller();

    List<Product> findByCategoryId(String categoryId);

    List<Product> findTop4ByCategoryIdAndIdNot(String categoryId, Integer id);

    List<Product> findAllWithSizes();

    List<Product> searchWithFilters(String keyword,
                                    String categoryId,
                                    BigDecimal minPrice,
                                    BigDecimal maxPrice,
                                    String sort);

    Page<Product> searchWithFiltersPage(String keyword,
                                        String categoryId,
                                        BigDecimal minPrice,
                                        BigDecimal maxPrice,
                                        String sort,
                                        int page,
                                        int size);

    Product create(Product product);

    Product update(Product product);

    /**
     * Lưu sản phẩm với variants (Upsert logic)
     * Hỗ trợ cả Thêm mới và Cập nhật
     * - Nếu là Cập nhật: so sánh danh sách size mới với DB, cập nhật số lượng nếu trùng, thêm mới nếu chưa có, xóa nếu không còn trong danh sách mới
     * - Nếu là Thêm mới: tạo sản phẩm và thêm tất cả variants
     */
    @org.springframework.transaction.annotation.Transactional
    Product saveProductWithVariants(Product product);

    void deleteById(Integer id);
}
