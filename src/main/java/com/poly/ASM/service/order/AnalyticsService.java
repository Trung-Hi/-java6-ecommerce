package com.poly.ASM.service.order;

import com.poly.ASM.dto.order.AnalyticsDTO;
import com.poly.ASM.dto.order.AnalyticsSummaryDTO;
import com.poly.ASM.dto.order.OrderStatusDTO;
import com.poly.ASM.dto.order.TopProductDTO;

import java.util.List;

public interface AnalyticsService {
    List<AnalyticsDTO> getRevenueTrend();
    List<TopProductDTO> getTopProducts();
    List<OrderStatusDTO> getOrderStatusCounts();
    AnalyticsSummaryDTO getAnalyticsSummary();
}
