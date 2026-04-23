#!/usr/bin/env python3
import pyodbc

conn = pyodbc.connect(
    "DRIVER={ODBC Driver 17 for SQL Server};"
    "SERVER=localhost,1433;"
    "DATABASE=ASM_Java5;"
    "Trusted_Connection=yes;"
    "TrustServerCertificate=yes;"
)

cursor = conn.cursor()

# Query 1: Monthly Revenue Trend
print("\n" + "="*60)
print("QUERY 1: Monthly Revenue Trend")
print("="*60)
cursor.execute("""
    SELECT 
        d.year,
        d.month,
        COUNT(DISTINCT fo.order_id) AS total_orders,
        SUM(fo.revenue) AS total_revenue
    FROM ecommerce_warehouse.fact_orders fo
    JOIN ecommerce_warehouse.dim_date d ON fo.date_key = d.date_key
    GROUP BY d.year, d.month
    ORDER BY year, month
""")
for row in cursor.fetchall():
    print(f"Year {row[0]}, Month {row[1]}: {row[2]} orders, Revenue: ${row[3]:.2f}")

# Query 2: Top Products
print("\n" + "="*60)
print("QUERY 2: Top 10 Products")
print("="*60)
cursor.execute("""
    SELECT TOP 10
        dp.name AS product_name,
        dp.category_name,
        COUNT(DISTINCT fo.order_id) AS order_count,
        SUM(fo.revenue) AS total_revenue
    FROM ecommerce_warehouse.fact_orders fo
    JOIN ecommerce_warehouse.dim_product dp ON fo.product_key = dp.product_key
    GROUP BY dp.name, dp.category_name
    ORDER BY total_revenue DESC
""")
for row in cursor.fetchall():
    print(f"{row[0]:30} | {row[1]:20} | Orders: {row[2]:3} | Revenue: ${row[3]:.2f}")

# Query 4: Customer Retention
print("\n" + "="*60)
print("QUERY 4: Customer Retention")
print("="*60)
cursor.execute("""
    WITH CustomerOrderCounts AS (
        SELECT 
            dc.customer_key,
            COUNT(DISTINCT fo.order_id) AS order_count
        FROM ecommerce_warehouse.dim_customer dc
        LEFT JOIN ecommerce_warehouse.fact_orders fo ON dc.customer_key = fo.customer_key
        GROUP BY dc.customer_key
    )
    SELECT 
        COUNT(*) AS total_customers,
        SUM(CASE WHEN order_count > 1 THEN 1 ELSE 0 END) AS returning_customers,
        SUM(CASE WHEN order_count = 1 THEN 1 ELSE 0 END) AS one_time_customers
    FROM CustomerOrderCounts
""")
for row in cursor.fetchall():
    print(f"Total Customers: {row[0]}")
    print(f"Returning Customers: {row[1]}")
    print(f"One-time Customers: {row[2]}")

conn.close()
