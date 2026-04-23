package com.poly.ASM.service.order.impl;

import com.poly.ASM.dto.order.AnalyticsDTO;
import com.poly.ASM.dto.order.AnalyticsSummaryDTO;
import com.poly.ASM.dto.order.OrderStatusDTO;
import com.poly.ASM.dto.order.TopProductDTO;
import com.poly.ASM.service.order.AnalyticsService;
import lombok.RequiredArgsConstructor;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.List;

@Service
@RequiredArgsConstructor
public class AnalyticsServiceImpl implements AnalyticsService {

    private final JdbcTemplate jdbcTemplate;

    @Override
    public List<AnalyticsDTO> getRevenueTrend() {
        return jdbcTemplate.query(
                "SELECT MONTH(o.create_date) as month, SUM(od.price * od.quantity) as revenue, COUNT(DISTINCT o.id) as totalOrders " +
                        "FROM orders o JOIN order_details od ON o.id = od.order_id " +
                        "WHERE YEAR(o.create_date) = YEAR(GETDATE()) AND o.status != 'CANCELLED' " +
                        "GROUP BY MONTH(o.create_date) ORDER BY month",
                (rs, rowNum) -> new AnalyticsDTO(
                        rs.getInt("month"),
                        rs.getBigDecimal("revenue"),
                        rs.getInt("totalOrders")
                )
        );
    }

    @Override
    public List<TopProductDTO> getTopProducts() {
        return jdbcTemplate.query(
                "SELECT TOP 5 p.name, SUM(od.quantity) as totalSold, SUM(od.price * od.quantity) as revenue " +
                        "FROM order_details od JOIN products p ON od.product_id = p.id " +
                        "JOIN orders o ON od.order_id = o.id WHERE o.status != 'CANCELLED' " +
                        "GROUP BY p.name ORDER BY totalSold DESC",
                (rs, rowNum) -> new TopProductDTO(
                        rs.getString("name"),
                        rs.getInt("totalSold"),
                        rs.getBigDecimal("revenue")
                )
        );
    }

    @Override
    public List<OrderStatusDTO> getOrderStatusCounts() {
        return jdbcTemplate.query(
                "SELECT status, COUNT(*) as count FROM orders GROUP BY status",
                (rs, rowNum) -> new OrderStatusDTO(
                        rs.getString("status"),
                        rs.getInt("count")
                )
        );
    }

    @Override
    public AnalyticsSummaryDTO getAnalyticsSummary() {
        BigDecimal revenue = jdbcTemplate.queryForObject(
                "SELECT ISNULL(SUM(od.price * od.quantity), 0) FROM order_details od JOIN orders o ON od.order_id = o.id WHERE o.status NOT IN ('CANCELLED')",
                BigDecimal.class);
        Integer orders = jdbcTemplate.queryForObject("SELECT COUNT(*) FROM orders", Integer.class);
        Integer customers = jdbcTemplate.queryForObject("SELECT COUNT(*) FROM accounts WHERE is_delete = 0", Integer.class);
        Integer products = jdbcTemplate.queryForObject("SELECT COUNT(*) FROM products WHERE is_delete = 0", Integer.class);
        return new AnalyticsSummaryDTO(revenue, orders, customers, products);
    }
}
