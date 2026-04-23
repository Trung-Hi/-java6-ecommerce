-- ========================================
-- E-Commerce Data Platform v2.0
-- Analytical SQL Queries
-- ========================================
-- Database: ASM_Java5
-- Schema: ecommerce_warehouse
-- Tác giả: Data Engineering Team
-- Mục đích: Các câu query phân tích kinh doanh sử dụng Star Schema
-- ========================================

USE ASM_Java5;
GO

-- ========================================
-- QUERY 1: Monthly Revenue Trend (Last 12 Months)
-- ========================================
-- Mục đích: Phân tích xu hướng doanh thu trong 12 tháng gần nhất
-- Business Value: Giúp nhận diện mùa vụ, đánh giá hiệu quả kinh doanh
-- Logic: Tính tổng doanh thu theo tháng, sắp xếp theo thời gian
-- Sử dụng: Window function để so sánh với tháng trước

WITH MonthlyRevenue AS (
    SELECT 
        d.year,
        d.month,
        d.month_name,
        COUNT(DISTINCT fo.order_id) AS total_orders,
        COUNT(DISTINCT fo.customer_key) AS unique_customers,
        SUM(fo.quantity) AS total_quantity,
        SUM(fo.revenue) AS total_revenue,
        AVG(fo.revenue) AS avg_order_value,
        SUM(fo.discount) AS total_discount
    FROM ecommerce_warehouse.fact_orders fo
    JOIN ecommerce_warehouse.dim_date d ON fo.date_key = d.date_key
    WHERE fo.status IN ('DELIVERED', 'PLACED')
        AND d.full_date >= DATEADD(MONTH, -12, GETDATE())
    GROUP BY d.year, d.month, d.month_name
)
SELECT 
    year,
    month_name,
    total_revenue,
    total_orders,
    unique_customers,
    avg_order_value,
    LAG(total_revenue) OVER (ORDER BY year, month) AS prev_month_revenue,
    total_revenue - LAG(total_revenue) OVER (ORDER BY year, month) AS revenue_change,
    CASE 
        WHEN LAG(total_revenue) OVER (ORDER BY year, month) = 0 THEN NULL
        ELSE (total_revenue - LAG(total_revenue) OVER (ORDER BY year, month)) / 
             LAG(total_revenue) OVER (ORDER BY year, month) * 100 
    END AS revenue_growth_pct
FROM MonthlyRevenue
ORDER BY year, month;
GO

-- ========================================
-- QUERY 2: Top 10 Best-Selling Products with Revenue Contribution %
-- ========================================
-- Mục đích: Xác định sản phẩm bán chạy nhất và tỷ lệ đóng góp doanh thu
-- Business Value: Giúp quản lý kho hàng, chiến lược marketing, dự báo nhu cầu
-- Logic: Tính tổng doanh thu theo sản phẩm, tính % đóng góp vào tổng doanh thu
-- Sử dụng: Window function RANK() để xếp hạng

WITH ProductRevenue AS (
    SELECT 
        dp.name AS product_name,
        dp.category_name AS category,
        COUNT(DISTINCT fo.order_id) AS order_count,
        SUM(fo.quantity) AS total_quantity_sold,
        SUM(fo.revenue) AS total_revenue,
        AVG(fo.unit_price) AS avg_price
    FROM ecommerce_warehouse.fact_orders fo
    JOIN ecommerce_warehouse.dim_product dp ON fo.product_key = dp.product_key
    WHERE fo.status IN ('DELIVERED', 'PLACED')
    GROUP BY dp.name, dp.category_name
),
TotalRevenue AS (
    SELECT SUM(total_revenue) AS grand_total
    FROM ProductRevenue
)
SELECT 
    pr.product_name,
    pr.category,
    pr.order_count,
    pr.total_quantity_sold,
    pr.total_revenue,
    pr.avg_price,
    (pr.total_revenue * 100.0 / tr.grand_total) AS revenue_contribution_pct,
    RANK() OVER (ORDER BY pr.total_revenue DESC) AS revenue_rank
FROM ProductRevenue pr
CROSS JOIN TotalRevenue tr
ORDER BY pr.total_revenue DESC
OFFSET 0 ROWS FETCH NEXT 10 ROWS ONLY;
GO

-- ========================================
-- QUERY 3: Revenue by Category with Month-over-Month Growth %
-- ========================================
-- Mục đích: Phân tích doanh thu theo danh mục và tăng trưởng so với tháng trước
-- Business Value: Giúp tối ưu hóa danh mục, loại bỏ sản phẩm kém hiệu quả
-- Logic: Tính doanh thu theo danh mục và tháng, so sánh với tháng trước
-- Sử dụng: Window function PARTITION BY để tính growth theo category

WITH CategoryMonthlyRevenue AS (
    SELECT 
        d.year,
        d.month,
        d.month_name,
        dp.category_name AS category,
        SUM(fo.revenue) AS monthly_revenue
    FROM ecommerce_warehouse.fact_orders fo
    JOIN ecommerce_warehouse.dim_date d ON fo.date_key = d.date_key
    JOIN ecommerce_warehouse.dim_product dp ON fo.product_key = dp.product_key
    WHERE fo.status IN ('DELIVERED', 'PLACED')
    GROUP BY d.year, d.month, d.month_name, dp.category_name
),
CategoryGrowth AS (
    SELECT 
        category,
        year,
        month,
        month_name,
        monthly_revenue,
        LAG(monthly_revenue) OVER (PARTITION BY category ORDER BY year, month) AS prev_month_revenue,
        monthly_revenue - LAG(monthly_revenue) OVER (PARTITION BY category ORDER BY year, month) AS revenue_change,
        CASE 
            WHEN LAG(monthly_revenue) OVER (PARTITION BY category ORDER BY year, month) = 0 THEN NULL
            ELSE (monthly_revenue - LAG(monthly_revenue) OVER (PARTITION BY category ORDER BY year, month)) / 
                 LAG(monthly_revenue) OVER (PARTITION BY category ORDER BY year, month) * 100 
        END AS mom_growth_pct
    FROM CategoryMonthlyRevenue
)
SELECT 
    category,
    year,
    month_name,
    monthly_revenue,
    prev_month_revenue,
    revenue_change,
    mom_growth_pct
FROM CategoryGrowth
ORDER BY category, year DESC, month DESC;
GO

-- ========================================
-- QUERY 4: Customer Retention Rate
-- ========================================
-- Mục đích: Tính toán tỷ lệ khách hàng quay lại mua hàng
-- Business Value: Đánh giá lòng trung thành của khách hàng, hiệu quả dịch vụ
-- Logic: Tỷ lệ = (Số khách hàng mua > 1 lần) / (Tổng số khách hàng)
-- Sử dụng: Subquery để đếm số đơn hàng mỗi khách hàng

WITH CustomerOrderCounts AS (
    SELECT 
        dc.customer_key,
        dc.username,
        COUNT(DISTINCT fo.order_id) AS order_count,
        SUM(fo.revenue) AS total_spent
    FROM ecommerce_warehouse.dim_customer dc
    LEFT JOIN ecommerce_warehouse.fact_orders fo ON dc.customer_key = fo.customer_key
        AND fo.status IN ('DELIVERED', 'PLACED')
    GROUP BY dc.customer_key, dc.username
),
RetentionMetrics AS (
    SELECT 
        COUNT(*) AS total_customers,
        SUM(CASE WHEN order_count > 1 THEN 1 ELSE 0 END) AS returning_customers,
        SUM(CASE WHEN order_count = 1 THEN 1 ELSE 0 END) AS one_time_customers,
        SUM(CASE WHEN order_count = 0 THEN 1 ELSE 0 END) AS inactive_customers
    FROM CustomerOrderCounts
)
SELECT 
    total_customers,
    returning_customers,
    one_time_customers,
    inactive_customers,
    (returning_customers * 100.0 / total_customers) AS retention_rate_pct,
    (one_time_customers * 100.0 / total_customers) AS one_time_rate_pct,
    (inactive_customers * 100.0 / total_customers) AS inactive_rate_pct
FROM RetentionMetrics;
GO

-- ========================================
-- QUERY 5: Peak Ordering Hours (Group by Hour of Day)
-- ========================================
-- Mục đích: Xác định khung giờ có lượng đơn hàng cao nhất
-- Business Value: Giúp tối ưu hóa nguồn lực, marketing, khuyến mãi theo giờ
-- Logic: Nhóm đơn hàng theo giờ trong ngày, đếm số lượng đơn hàng
-- Sử dụng: DATEPART để extract hour từ datetime

SELECT 
    DATEPART(HOUR, fo.order_date) AS hour_of_day,
    COUNT(DISTINCT fo.order_id) AS order_count,
    SUM(fo.revenue) AS total_revenue,
    AVG(fo.revenue) AS avg_order_value,
    COUNT(DISTINCT fo.customer_key) AS unique_customers
FROM ecommerce_warehouse.fact_orders fo
WHERE fo.status IN ('DELIVERED', 'PLACED')
    AND fo.order_date IS NOT NULL
GROUP BY DATEPART(HOUR, fo.order_date)
ORDER BY hour_of_day;
GO

-- Phiên bản thay thế nếu không có order_time: Phân tích theo ngày trong tuần
SELECT 
    d.weekday_name,
    d.is_weekend,
    COUNT(DISTINCT fo.order_id) AS order_count,
    SUM(fo.revenue) AS total_revenue,
    AVG(fo.revenue) AS avg_order_value,
    COUNT(DISTINCT fo.customer_key) AS unique_customers
FROM ecommerce_warehouse.fact_orders fo
JOIN ecommerce_warehouse.dim_date d ON fo.date_key = d.date_key
WHERE fo.status IN ('DELIVERED', 'PLACED')
GROUP BY d.weekday_name, d.is_weekend
ORDER BY d.is_weekend, d.weekday_name;
GO

-- ========================================
-- QUERY 6: Order Completion Rate vs Cancellation Rate by Month
-- ========================================
-- Mục đích: So sánh tỷ lệ đơn hàng hoàn thành và hủy theo tháng
-- Business Value: Đánh giá chất lượng dịch vụ, xác định vấn đề trong quy trình
-- Logic: Tính tỷ lệ đơn hàng theo trạng thái (DELIVERED/PLACED vs CANCELLED)
-- Sử dụng: CASE statement để phân loại trạng thái

WITH MonthlyOrderStats AS (
    SELECT 
        d.year,
        d.month,
        d.month_name,
        fo.status,
        COUNT(DISTINCT fo.order_id) AS order_count
    FROM ecommerce_warehouse.fact_orders fo
    JOIN ecommerce_warehouse.dim_date d ON fo.date_key = d.date_key
    GROUP BY d.year, d.month, d.month_name, fo.status
),
MonthlyTotals AS (
    SELECT 
        year,
        month,
        month_name,
        SUM(order_count) AS total_orders
    FROM MonthlyOrderStats
    GROUP BY year, month, month_name
),
StatusBreakdown AS (
    SELECT 
        ms.year,
        ms.month,
        ms.month_name,
        ms.status,
        ms.order_count,
        mt.total_orders,
        (ms.order_count * 100.0 / mt.total_orders) AS status_rate_pct
    FROM MonthlyOrderStats ms
    JOIN MonthlyTotals mt ON ms.year = mt.year AND ms.month = mt.month
)
SELECT 
    year,
    month_name,
    SUM(CASE WHEN status IN ('DELIVERED', 'PLACED') THEN order_count ELSE 0 END) AS completed_orders,
    SUM(CASE WHEN status = 'CANCELLED' THEN order_count ELSE 0 END) AS cancelled_orders,
    SUM(CASE WHEN status = 'PENDING' THEN order_count ELSE 0 END) AS pending_orders,
    total_orders,
    (SUM(CASE WHEN status IN ('DELIVERED', 'PLACED') THEN order_count ELSE 0 END) * 100.0 / total_orders) AS completion_rate_pct,
    (SUM(CASE WHEN status = 'CANCELLED' THEN order_count ELSE 0 END) * 100.0 / total_orders) AS cancellation_rate_pct,
    (SUM(CASE WHEN status = 'PENDING' THEN order_count ELSE 0 END) * 100.0 / total_orders) AS pending_rate_pct
FROM StatusBreakdown
GROUP BY year, month_name, total_orders
ORDER BY year DESC, month DESC;
GO

-- ========================================
-- BONUS QUERIES
-- ========================================

-- BONUS 1: Average Order Value by Customer Segment
-- Phân tích giá trị đơn hàng trung bình theo phân khúc khách hàng
WITH CustomerAOV AS (
    SELECT 
        dc.customer_key,
        dc.username,
        COUNT(fo.order_id) AS order_count,
        SUM(fo.revenue) AS total_spent,
        AVG(fo.revenue) AS avg_order_value,
        CASE 
            WHEN COUNT(fo.order_id) >= 10 THEN 'VIP'
            WHEN COUNT(fo.order_id) >= 5 THEN 'Regular'
            WHEN COUNT(fo.order_id) >= 2 THEN 'Occasional'
            ELSE 'One-time'
        END AS customer_segment
    FROM ecommerce_warehouse.dim_customer dc
    LEFT JOIN ecommerce_warehouse.fact_orders fo ON dc.customer_key = fo.customer_key
        AND fo.status IN ('DELIVERED', 'PLACED')
    GROUP BY dc.customer_key, dc.username
)
SELECT 
    customer_segment,
    COUNT(*) AS customer_count,
    AVG(order_count) AS avg_orders_per_customer,
    AVG(total_spent) AS avg_total_spent,
    AVG(avg_order_value) AS avg_aov
FROM CustomerAOV
GROUP BY customer_segment
ORDER BY avg_total_spent DESC;
GO

-- BONUS 2: Product Category Performance Matrix
-- Ma trận hiệu suất danh mục sản phẩm
SELECT 
    dp.category_name,
    COUNT(DISTINCT dp.product_key) AS unique_products,
    COUNT(DISTINCT fo.order_id) AS total_orders,
    SUM(fo.quantity) AS total_quantity_sold,
    SUM(fo.revenue) AS total_revenue,
    AVG(fo.revenue) AS avg_order_value,
    AVG(fo.unit_price) AS avg_product_price,
    RANK() OVER (ORDER BY SUM(fo.revenue) DESC) AS revenue_rank
FROM ecommerce_warehouse.dim_product dp
LEFT JOIN ecommerce_warehouse.fact_orders fo ON dp.product_key = fo.product_key
    AND fo.status IN ('DELIVERED', 'PLACED')
GROUP BY dp.category_name
ORDER BY total_revenue DESC;
GO

PRINT '========================================';
PRINT '✅ Đã tải xong tất cả các câu query phân tích';
PRINT '========================================';
