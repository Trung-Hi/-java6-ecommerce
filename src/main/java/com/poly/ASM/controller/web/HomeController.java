package com.poly.ASM.controller.web;

import com.poly.ASM.entity.product.Category;
import com.poly.ASM.entity.product.Product;
import com.poly.ASM.dto.common.ApiResponse;
import com.poly.ASM.service.product.CategoryService;
import com.poly.ASM.service.product.ProductService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.math.BigDecimal;
import java.util.List;
import org.springframework.data.domain.Page;
import java.util.Map;
import java.util.HashMap;

@RestController
@RequestMapping("/api/home")
@RequiredArgsConstructor
public class HomeController {

    private final ProductService productService;
    private final CategoryService categoryService;

    @GetMapping("/root")
    public ResponseEntity<ApiResponse<?>> root() {
        return ResponseEntity.ok(ApiResponse.success("Điểm vào API trang chủ", Map.of("indexEndpoint", "/api/home/index")));
    }

    @GetMapping("/index")
    public ResponseEntity<ApiResponse<?>> index(@RequestParam(value = "keyword", required = false) String keyword,
                                                @RequestParam(value = "categoryId", required = false) String categoryId,
                                                @RequestParam(value = "minPrice", required = false) BigDecimal minPrice,
                                                @RequestParam(value = "maxPrice", required = false) BigDecimal maxPrice,
                                                @RequestParam(value = "priceRange", required = false) String priceRange,
                                                @RequestParam(value = "sort", required = false) String sort,
                                                @RequestParam(value = "page", required = false, defaultValue = "0") Integer page,
                                                @RequestParam(value = "size", required = false, defaultValue = "12") Integer size) {
        BigDecimal[] range = applyQuickRange(priceRange, minPrice, maxPrice);
        minPrice = range[0];
        maxPrice = range[1];
        List<Category> categories = categoryService.findAll();
        boolean hasFilter = (keyword != null && !keyword.isBlank())
                || (categoryId != null && !categoryId.isBlank())
                || minPrice != null
                || maxPrice != null
                || (priceRange != null && !priceRange.isBlank())
                || (sort != null && !sort.isBlank());
        List<Product> filteredProducts = List.of();
        Page<Product> filteredPage = null;
        if (hasFilter) {
            filteredPage = productService.searchWithFiltersPage(keyword, categoryId, minPrice, maxPrice, sort, page, size);
            filteredProducts = filteredPage.getContent();
        }
        Map<String, Object> filters = new HashMap<>();
        filters.put("keyword", keyword == null ? "" : keyword);
        filters.put("categoryId", categoryId == null ? "" : categoryId);
        filters.put("minPrice", minPrice);
        filters.put("maxPrice", maxPrice);
        filters.put("priceRange", priceRange == null ? "" : priceRange);
        filters.put("sort", sort == null ? "" : sort);
        filters.put("page", page);
        filters.put("size", size);
        Map<String, Object> data = new HashMap<>();
        data.put("categories", categories);
        data.put("filters", filters);
        data.put("hasFilter", hasFilter);
        data.put("filteredProducts", filteredProducts);
        data.put("currentPage", filteredPage == null ? 0 : filteredPage.getNumber());
        data.put("totalPages", filteredPage == null ? 0 : filteredPage.getTotalPages());
        data.put("newProducts", productService.findTop8ByOrderByCreateDateDesc());
        data.put("discountProducts", productService.findTop8ByDiscountGreaterThanOrderByDiscountDesc(0));
        data.put("bestSellerProducts", productService.findTop8BestSeller());
        return ResponseEntity.ok(ApiResponse.success("Lấy dữ liệu trang chủ thành công", data));
    }

    private BigDecimal[] applyQuickRange(String priceRange, BigDecimal minPrice, BigDecimal maxPrice) {
        if (priceRange == null || priceRange.isBlank()) {
            return new BigDecimal[]{minPrice, maxPrice};
        }
        return switch (priceRange) {
            case "under_500" -> new BigDecimal[]{null, BigDecimal.valueOf(500_000)};
            case "500_1m" -> new BigDecimal[]{BigDecimal.valueOf(500_000), BigDecimal.valueOf(1_000_000)};
            case "1m_2m" -> new BigDecimal[]{BigDecimal.valueOf(1_000_000), BigDecimal.valueOf(2_000_000)};
            case "over_2m" -> new BigDecimal[]{BigDecimal.valueOf(2_000_000), null};
            default -> new BigDecimal[]{minPrice, maxPrice};
        };
    }
}
