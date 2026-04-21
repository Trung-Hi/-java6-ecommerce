package com.poly.ASM.controller.admin;

import com.poly.ASM.dto.common.ApiResponse;
import com.poly.ASM.entity.product.Category;
import com.poly.ASM.exception.ApiException;
import com.poly.ASM.service.product.CategoryService;
import lombok.RequiredArgsConstructor;
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

import java.util.Optional;

@RestController
@RequestMapping("/api/admin/categories")
@RequiredArgsConstructor
public class CategoryAController {

    private final CategoryService categoryService;

    @GetMapping
    public ResponseEntity<ApiResponse<?>> index() {
        return ResponseEntity.ok(ApiResponse.success("Lấy danh sách danh mục thành công", categoryService.findAll()));
    }

    @PostMapping
    public ResponseEntity<ApiResponse<?>> create(@RequestParam(value = "id", required = false) String id,
                                                  @RequestParam("name") String name) {
        if (id == null || id.isBlank()) {
            id = buildNextCategoryId();
        }
        Category category = new Category();
        category.setId(id);
        category.setName(name);
        Category saved = categoryService.create(category);
        return ResponseEntity.status(HttpStatus.CREATED).body(ApiResponse.success("Tạo danh mục thành công", saved));
    }

    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<?>> edit(@PathVariable("id") String id) {
        Optional<Category> category = categoryService.findById(id);
        if (category.isEmpty()) {
            throw new ApiException(HttpStatus.NOT_FOUND, "Không tìm thấy danh mục");
        }
        return ResponseEntity.ok(ApiResponse.success("Lấy chi tiết danh mục thành công", category.get()));
    }

    @PutMapping("/{id}")
    public ResponseEntity<ApiResponse<?>> update(@PathVariable("id") String id,
                                                  @RequestParam("name") String name) {
        Category category = categoryService.findById(id)
                .orElseThrow(() -> new ApiException(HttpStatus.NOT_FOUND, "Không tìm thấy danh mục"));
        category.setName(name);
        Category saved = categoryService.update(category);
        return ResponseEntity.ok(ApiResponse.success("Cập nhật danh mục thành công", saved));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<ApiResponse<?>> delete(@PathVariable("id") String id) {
        categoryService.deleteById(id);
        return ResponseEntity.ok(ApiResponse.success("Xóa danh mục thành công", null));
    }

    private String buildNextCategoryId() {
        int max = 0;
        for (Category category : categoryService.findAll()) {
            String currentId = category.getId();
            if (currentId == null || !currentId.startsWith("CAT")) {
                continue;
            }
            String numberPart = currentId.substring(3);
            if (!numberPart.matches("\\d+")) {
                continue;
            }
            int value = Integer.parseInt(numberPart);
            if (value > max) {
                max = value;
            }
        }
        return String.format("CAT%02d", max + 1);
    }
}
