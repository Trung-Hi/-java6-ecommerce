package com.poly.ASM.controller.web;

import com.poly.ASM.dto.common.ApiResponse;
import com.poly.ASM.service.cart.CartService;
import jakarta.servlet.http.HttpSession;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;
import java.util.HashMap;

@RestController
@RequestMapping("/api/cart")
@RequiredArgsConstructor
public class CartController {

    private final CartService cartService;
    private final HttpSession session;

    @GetMapping
    public ResponseEntity<ApiResponse<?>> index() {
        Map<String, Object> data = new HashMap<>();
        data.put("items", cartService.getCartItems());
        data.put("totalPrice", cartService.getTotalPrice());
        Object message = session.getAttribute("CART_MESSAGE");
        if (message instanceof String msg && !msg.isBlank()) {
            data.put("message", msg);
            session.removeAttribute("CART_MESSAGE");
        }
        return ResponseEntity.ok(ApiResponse.success("Lấy giỏ hàng thành công", data));
    }

    @PostMapping("/items/{id}")
    public ResponseEntity<ApiResponse<?>> add(@PathVariable("id") Integer productId,
                                               @RequestParam(value = "sizeId", required = false) Integer sizeId) {
        if (sizeId == null) {
            return ResponseEntity.badRequest().body(ApiResponse.error("Vui lòng chọn size trước khi thêm vào giỏ hàng.", null));
        }
        boolean ok = cartService.add(productId, sizeId, 1);
        if (!ok) {
            return ResponseEntity.badRequest().body(ApiResponse.error("Size đã chọn đã hết hàng hoặc không đủ tồn kho.", null));
        }
        return ResponseEntity.status(HttpStatus.CREATED).body(ApiResponse.success("Thêm sản phẩm vào giỏ hàng thành công", null));
    }

    @PostMapping("/items")
    public ResponseEntity<ApiResponse<?>> addDetail(@RequestParam("productId") Integer productId,
                                                    @RequestParam("sizeId") Integer sizeId,
                                                    @RequestParam(value = "quantity", defaultValue = "1") Integer quantity) {
        if (sizeId == null) {
            return ResponseEntity.badRequest().body(ApiResponse.error("Vui lòng chọn size trước khi thêm vào giỏ hàng.", null));
        }
        boolean ok = cartService.addToCart(productId, sizeId, quantity);
        if (!ok) {
            return ResponseEntity.badRequest().body(ApiResponse.error("Số lượng vượt quá tồn kho của size đã chọn.", null));
        }
        long distinctCount = cartService.getDistinctProductCount();
        return ResponseEntity.ok(ApiResponse.success("Đã thêm vào giỏ hàng.", Map.of(
                "distinctCount", distinctCount,
                "productId", productId
        )));
    }

    @DeleteMapping("/items/{id}")
    public ResponseEntity<ApiResponse<?>> remove(@PathVariable("id") Integer productId,
                                                  @RequestParam("sizeId") Integer sizeId) {
        cartService.remove(productId, sizeId);
        return ResponseEntity.ok(ApiResponse.success("Xóa sản phẩm khỏi giỏ hàng thành công", null));
    }

    @PutMapping("/items")
    public ResponseEntity<ApiResponse<?>> update(@RequestParam("productId") Integer productId,
                                                  @RequestParam("sizeId") Integer sizeId,
                                                  @RequestParam("quantity") Integer quantity) {
        boolean ok = cartService.update(productId, sizeId, quantity);
        if (!ok) {
            return ResponseEntity.badRequest().body(ApiResponse.error("Số lượng vượt quá tồn kho của size đã chọn.", null));
        }
        return ResponseEntity.ok(ApiResponse.success("Cập nhật giỏ hàng thành công", null));
    }

    @DeleteMapping("/clear")
    public ResponseEntity<ApiResponse<?>> clear() {
        cartService.clear();
        return ResponseEntity.ok(ApiResponse.success("Xóa toàn bộ giỏ hàng thành công", null));
    }
}
