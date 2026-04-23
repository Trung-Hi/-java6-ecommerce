package com.poly.ASM.controller.admin;

import com.poly.ASM.dto.common.ApiResponse;
import com.poly.ASM.dto.order.AnalyticsDTO;
import com.poly.ASM.dto.order.AnalyticsSummaryDTO;
import com.poly.ASM.dto.order.OrderStatusDTO;
import com.poly.ASM.dto.order.TopProductDTO;
import com.poly.ASM.service.order.AnalyticsService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/admin/analytics")
@RequiredArgsConstructor
public class AnalyticsAController {

    private final AnalyticsService analyticsService;

    @GetMapping("/revenue-trend")
    public ResponseEntity<?> getRevenueTrend() {
        try {
            List<AnalyticsDTO> data = analyticsService.getRevenueTrend();
            return ResponseEntity.ok(ApiResponse.success("Lấy xu hướng doanh thu thành công", data));
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.status(500).body("Error in getRevenueTrend: " + e.getMessage());
        }
    }

    @GetMapping("/top-products")
    public ResponseEntity<?> getTopProducts() {
        try {
            List<TopProductDTO> data = analyticsService.getTopProducts();
            return ResponseEntity.ok(ApiResponse.success("Lấy top sản phẩm bán chạy thành công", data));
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.status(500).body("Error in getTopProducts: " + e.getMessage());
        }
    }

    @GetMapping("/order-status")
    public ResponseEntity<?> getOrderStatusCounts() {
        try {
            List<OrderStatusDTO> data = analyticsService.getOrderStatusCounts();
            return ResponseEntity.ok(ApiResponse.success("Lấy số lượng đơn hàng theo trạng thái thành công", data));
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.status(500).body("Error in getOrderStatusCounts: " + e.getMessage());
        }
    }

    @GetMapping("/summary")
    public ResponseEntity<?> getAnalyticsSummary() {
        try {
            AnalyticsSummaryDTO data = analyticsService.getAnalyticsSummary();
            return ResponseEntity.ok(ApiResponse.success("Lấy tóm tắt thống kê thành công", data));
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.status(500).body("Error in getAnalyticsSummary: " + e.getMessage());
        }
    }
}
