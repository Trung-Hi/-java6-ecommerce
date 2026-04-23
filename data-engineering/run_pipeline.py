#!/usr/bin/env python3
"""
E-Commerce Data Platform v2.0 - Full ETL Pipeline Runner
==========================================================
Runs the complete ETL pipeline: Extract → Transform → Quality → Load

Author: Data Engineering Team
Date: 2024
"""

import logging
import sys
import os
import pandas as pd

# Add parent directory to path for imports
sys.path.append(os.path.dirname(os.path.abspath(__file__)))

from etl.extract import extract_all
from etl.transform import (
    clean_nulls, fix_data_types, calculate_metrics, remove_duplicates,
    build_dim_date, validate_revenue, transform_orders, transform_dim_tables
)
from etl.quality import run_quality_checks
import pyodbc
from config.constants import SOURCE_CONNECTION_STRING, WAREHOUSE_SCHEMA

logging.basicConfig(
    level=logging.INFO,
    format='%(asctime)s - %(levelname)s - %(message)s'
)

logger = logging.getLogger(__name__)


def main():
    """Run the full ETL pipeline."""
    logger.info("🚀 Starting Full ETL Pipeline...")
    
    try:
        # ========================================
        # STEP 1: EXTRACT
        # ========================================
        logger.info("=" * 60)
        logger.info("STEP 1: EXTRACT DATA")
        logger.info("=" * 60)
        
        data = extract_all()
        
        df_orders = data['orders']
        df_products = data['products']
        df_customers = data['customers']
        df_categories = data['categories']
        
        logger.info(f"✅ Extracted data:")
        logger.info(f"   - Orders: {len(df_orders)} rows")
        logger.info(f"   - Products: {len(df_products)} rows")
        logger.info(f"   - Customers: {len(df_customers)} rows")
        logger.info(f"   - Categories: {len(df_categories)} rows")
        
        # ========================================
        # STEP 2: TRANSFORM
        # ========================================
        logger.info("=" * 60)
        logger.info("STEP 2: TRANSFORM DATA")
        logger.info("=" * 60)
        
        # Transform orders
        df_orders_transformed = transform_orders(df_orders, df_products, df_customers)
        logger.info(f"✅ Transformed orders: {len(df_orders_transformed)} rows")
        
        # Transform dimensions
        dim_data = transform_dim_tables(df_products, df_customers, df_categories)
        logger.info(f"✅ Transformed dimensions:")
        logger.info(f"   - Products: {len(dim_data['products'])} rows")
        logger.info(f"   - Customers: {len(dim_data['customers'])} rows")
        logger.info(f"   - Categories: {len(dim_data['categories'])} rows")
        
        # Build date dimension
        if not df_orders_transformed.empty and 'create_date' in df_orders_transformed.columns:
            start_date = df_orders_transformed['create_date'].min().date()
            end_date = df_orders_transformed['create_date'].max().date()
            df_dim_date = build_dim_date(start_date, end_date)
            logger.info(f"✅ Built date dimension: {len(df_dim_date)} rows")
            dim_data['date'] = df_dim_date
        else:
            logger.warning("⚠️ Could not build date dimension - no orders with valid dates")
        
        # ========================================
        # STEP 3: QUALITY CHECKS
        # ========================================
        logger.info("=" * 60)
        logger.info("STEP 3: DATA QUALITY CHECKS")
        logger.info("=" * 60)
        
        quality_report = run_quality_checks(df_orders_transformed)
        
        if quality_report['critical_failures'] > 0:
            logger.error(f"❌ Critical quality checks failed: {quality_report['critical_failures']}")
            logger.error(f"   Total failed rows: {quality_report['total_failed_rows']}")
            return False
        else:
            logger.info(f"✅ All quality checks passed")
            logger.info(f"   Total checks: {quality_report['total_checks']}")
            logger.info(f"   Passed: {quality_report['passed_checks']}")
        
        # ========================================
        # STEP 4: LOAD TO WAREHOUSE
        # ========================================
        logger.info("=" * 60)
        logger.info("STEP 4: LOAD TO WAREHOUSE")
        logger.info("=" * 60)
        
        # Connect to SQL Server
        conn = pyodbc.connect(SOURCE_CONNECTION_STRING)
        cursor = conn.cursor()
        
        rows_loaded = 0
        
        # Load categories
        for _, row in dim_data['categories'].iterrows():
            try:
                cursor.execute(f"""
                    IF NOT EXISTS (SELECT 1 FROM {WAREHOUSE_SCHEMA}.dim_category WHERE category_id = ?)
                    INSERT INTO {WAREHOUSE_SCHEMA}.dim_category (category_id, name, parent_id)
                    VALUES (?, ?, ?)
                """, str(row['category_id']), str(row['category_id']), row['name'],
                     str(row['parent_id']) if pd.notna(row['parent_id']) else None)
                rows_loaded += 1
            except Exception as e:
                logger.warning(f"   ⚠️ Lỗi insert category: {e}")
        
        # Load products
        for _, row in dim_data['products'].iterrows():
            try:
                cursor.execute(f"""
                    IF NOT EXISTS (SELECT 1 FROM {WAREHOUSE_SCHEMA}.dim_product WHERE product_id = ?)
                    INSERT INTO {WAREHOUSE_SCHEMA}.dim_product (product_id, name, image, category_id, category_name, price, discount)
                    VALUES (?, ?, ?, ?, ?, ?, ?)
                """, int(row['product_id']), int(row['product_id']), str(row['name']), str(row['image']) if pd.notna(row['image']) else None,
                     str(row['category_id']) if pd.notna(row['category_id']) else None, str(row['category_name']),
                     float(row['price']) if pd.notna(row['price']) else 0, float(row['discount']) if pd.notna(row['discount']) else 0)
                rows_loaded += 1
            except Exception as e:
                logger.warning(f"   ⚠️ Lỗi insert product: {e}")
        
        # Load customers
        for _, row in dim_data['customers'].iterrows():
            try:
                cursor.execute(f"""
                    IF NOT EXISTS (SELECT 1 FROM {WAREHOUSE_SCHEMA}.dim_customer WHERE username = ?)
                    INSERT INTO {WAREHOUSE_SCHEMA}.dim_customer (username, fullname, email, phone, address)
                    VALUES (?, ?, ?, ?, ?)
                """, str(row['username']), str(row['username']), str(row['fullname']), str(row['email']),
                     str(row['phone']) if pd.notna(row['phone']) else None, str(row['address']) if pd.notna(row['address']) else None)
                rows_loaded += 1
            except Exception as e:
                logger.warning(f"   ⚠️ Lỗi insert customer: {e}")
        
        # Load date dimension
        if 'date' in dim_data:
            for _, row in dim_data['date'].iterrows():
                try:
                    cursor.execute(f"""
                        IF NOT EXISTS (SELECT 1 FROM {WAREHOUSE_SCHEMA}.dim_date WHERE date_key = ?)
                        INSERT INTO {WAREHOUSE_SCHEMA}.dim_date (date_key, full_date, day, month, year, quarter, weekday_name, is_weekend)
                        VALUES (?, ?, ?, ?, ?, ?, ?, ?)
                    """, row['date_key'], row['date_key'], row['full_date'], row['day'], row['month'],
                         row['year'], row['quarter'], row['weekday_name'], row['is_weekend'])
                    rows_loaded += 1
                except Exception as e:
                    logger.warning(f"   ⚠️ Lỗi insert date: {e}")
        
        # Load fact orders
        cursor.execute(f"SELECT product_key, product_id FROM {WAREHOUSE_SCHEMA}.dim_product")
        product_map = {row[1]: row[0] for row in cursor.fetchall()}
        
        cursor.execute(f"SELECT customer_key, username FROM {WAREHOUSE_SCHEMA}.dim_customer")
        customer_map = {row[1]: row[0] for row in cursor.fetchall()}
        
        # Insert missing products from orders into dim_product
        missing_products = set(df_orders_transformed['product_id'].unique()) - set(product_map.keys())
        if missing_products:
            logger.info(f"📦 Inserting {len(missing_products)} missing products into dim_product...")
            for product_id in missing_products:
                try:
                    cursor.execute(f"""
                        INSERT INTO {WAREHOUSE_SCHEMA}.dim_product 
                        (product_id, name, image, category_id, category_name, price, discount)
                        VALUES (?, ?, ?, ?, ?, ?, ?)
                    """, int(product_id), f'Unknown Product {product_id}', None, 
                         'UNKNOWN', 'Unknown Category', 0.0, 0.0)
                    # Get the auto-generated product_key
                    cursor.execute(f"SELECT product_key FROM {WAREHOUSE_SCHEMA}.dim_product WHERE product_id = ?", int(product_id))
                    new_key = cursor.fetchone()[0]
                    product_map[int(product_id)] = new_key
                    rows_loaded += 1
                except Exception as e:
                    logger.warning(f"   ⚠️ Error inserting missing product {product_id}: {e}")
        
        # Insert missing customers from orders into dim_customer
        missing_customers = set(df_orders_transformed['username'].unique()) - set(customer_map.keys())
        if missing_customers:
            logger.info(f"👤 Inserting {len(missing_customers)} missing customers into dim_customer...")
            for username in missing_customers:
                try:
                    cursor.execute(f"""
                        INSERT INTO {WAREHOUSE_SCHEMA}.dim_customer 
                        (username, fullname, email, phone, address)
                        VALUES (?, ?, ?, ?, ?)
                    """, str(username), str(username), f'{username}@example.com', 
                         None, None)
                    # Get the auto-generated customer_key
                    cursor.execute(f"SELECT customer_key FROM {WAREHOUSE_SCHEMA}.dim_customer WHERE username = ?", str(username))
                    new_key = cursor.fetchone()[0]
                    customer_map[str(username)] = new_key
                    rows_loaded += 1
                except Exception as e:
                    logger.warning(f"   ⚠️ Error inserting missing customer {username}: {e}")
        
        for _, row in df_orders_transformed.iterrows():
            try:
                product_key = product_map.get(int(row['product_id']))
                customer_key = customer_map.get(str(row['username']))
                date_key = row.get('date_key')
                
                if product_key is None or customer_key is None or date_key is None:
                    continue
                
                cursor.execute(f"""
                    INSERT INTO {WAREHOUSE_SCHEMA}.fact_orders 
                    (order_id, order_detail_id, product_key, customer_key, date_key, size_id, quantity, unit_price, revenue, 
                     status, phone, address, order_date, order_time)
                    VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)
                """, int(row['order_id']), int(row['order_detail_id']), product_key, customer_key, date_key,
                     int(row['size_id']) if pd.notna(row['size_id']) else None,
                     int(row['quantity']), float(row['unit_price']), float(row['revenue']),
                     str(row['status']) if pd.notna(row['status']) else None,
                     str(row['phone']) if pd.notna(row['phone']) else None,
                     str(row['address']) if pd.notna(row['address']) else None,
                     row['order_date'], row['order_time'])
                rows_loaded += 1
            except Exception as e:
                logger.warning(f"   ⚠️ Lỗi insert order: {e}")
        
        conn.commit()
        conn.close()
        
        logger.info(f"✅ Loaded {rows_loaded} rows to warehouse")
        
        # ========================================
        # SUMMARY
        # ========================================
        logger.info("=" * 60)
        logger.info("✅ PIPELINE COMPLETED SUCCESSFULLY")
        logger.info("=" * 60)
        logger.info(f"   Extracted: {len(df_orders)} orders")
        logger.info(f"   Transformed: {len(df_orders_transformed)} orders")
        logger.info(f"   Quality: {quality_report['passed_checks']}/{quality_report['total_checks']} checks passed")
        logger.info(f"   Loaded: {rows_loaded} rows to warehouse")
        
        return True
        
    except Exception as e:
        logger.error(f"❌ Pipeline failed: {e}")
        import traceback
        traceback.print_exc()
        return False


if __name__ == "__main__":
    success = main()
    sys.exit(0 if success else 1)
