#!/usr/bin/env python3
"""
E-Commerce Data Platform v2.0 - Apache Airflow DAG
===================================================
Orchestrates the ETL pipeline with data quality checks,
Azure backup, and email alerting.

Author: Data Engineering Team
Date: 2024
"""

from datetime import datetime, timedelta
from airflow import DAG
from airflow.decorators import task, dag
from airflow.providers.microsoft.mssql.hooks.mssql import MsSqlHook
from airflow.providers.sendgrid.operators.sendgrid import SendgridEmailOperator
import pandas as pd
import logging
import sys
import os

# Add parent directory to path for imports
sys.path.append(os.path.dirname(os.path.dirname(os.path.abspath(__file__))))

from etl.extract import extract_orders, extract_products, extract_customers, extract_categories
from etl.transform import (
    clean_nulls, fix_data_types, calculate_metrics, remove_duplicates,
    build_dim_date, validate_revenue, transform_orders, transform_dim_tables
)
from etl.quality import (
    check_nulls, check_duplicates, check_revenue_positive,
    check_row_count, check_date_range, generate_quality_report
)
from etl.azure_backup import connect_azure_blob, upload_to_blob
from monitoring.alerting import send_success_alert, send_failure_alert, generate_pipeline_report

# ========================================
# DAG CONFIGURATION
# ========================================
DEFAULT_ARGS = {
    'owner': 'data-engineering',
    'depends_on_past': False,
    'start_date': datetime(2024, 1, 1),
    'email_on_failure': True,
    'email_on_retry': False,
    'retries': 2,
    'retry_delay': timedelta(minutes=5),
}

# Constants - Cấu hình kết nối
MSSQL_CONN_ID = 'mssql_default'
WAREHOUSE_SCHEMA = 'ecommerce_warehouse'
QUALITY_CONFIG = {
    'null_threshold': 0.05,           # 5% null threshold
    'min_rows': 100,                   # Minimum rows for extraction
    'date_future_days': 1,            # Allow dates up to 1 day in future
}

# ========================================
# DAG DEFINITION
# ========================================
@dag(
    dag_id='ecommerce_etl_pipeline',
    default_args=DEFAULT_ARGS,
    description='E-Commerce ETL Pipeline with Data Quality, Azure Backup, and Alerting',
    schedule_interval='@daily',        # Chạy hàng ngày lúc midnight
    catchup=False,                     # Không backfill
    tags=['ecommerce', 'etl', 'warehouse'],
)
def ecommerce_etl_pipeline():
    """
    Main DAG orchestrating the ETL pipeline:
    1. Extract data from SQL Server
    2. Run data quality checks
    3. Transform data
    4. Load into warehouse
    5. Backup to Azure Blob Storage
    6. Send notification
    """
    
    # ========================================
    # TASK 1: EXTRACT DATA
    # ========================================
    @task
    def extract_task(**context):
        """
        Extract data from source SQL Server database
        Returns dictionary of DataFrames
        """
        logger = logging.getLogger(__name__)
        logger.info("📥 Bắt đầu EXTRACT task")
        
        try:
            mssql = MsSqlHook(mssql_conn_id=MSSQL_CONN_ID)
            conn = mssql.get_conn()
            
            # Extract từ các bảng nguồn
            df_orders = extract_orders(conn)
            df_products = extract_products(conn)
            df_customers = extract_customers(conn)
            df_categories = extract_categories(conn)
            
            conn.close()
            
            # Log row counts
            logger.info(f"✅ Extracted orders: {len(df_orders)} rows")
            logger.info(f"✅ Extracted products: {len(df_products)} rows")
            logger.info(f"✅ Extracted customers: {len(df_customers)} rows")
            logger.info(f"✅ Extracted categories: {len(df_categories)} rows")
            
            # Store in XCom for downstream tasks
            context['ti'].xcom_push(key='df_orders', value=df_orders.to_dict('records'))
            context['ti'].xcom_push(key='df_products', value=df_products.to_dict('records'))
            context['ti'].xcom_push(key='df_customers', value=df_customers.to_dict('records'))
            context['ti'].xcom_push(key='df_categories', value=df_categories.to_dict('records'))
            
            logger.info("✅ EXTRACT task hoàn thành")
            return {
                'orders': len(df_orders),
                'products': len(df_products),
                'customers': len(df_customers),
                'categories': len(df_categories)
            }
            
        except Exception as e:
            logger.error(f"❌ EXTRACT task thất bại: {e}")
            raise
    
    # ========================================
    # TASK 2: DATA QUALITY CHECK
    # ========================================
    @task
    def quality_check_task(**context):
        """
        Run data quality checks on extracted data
        Raises exception if critical checks fail
        """
        logger = logging.getLogger(__name__)
        logger.info("🔍 Bắt đầu QUALITY CHECK task")
        
        try:
            # Retrieve data from XCom
            orders_data = context['ti'].xcom_pull(key='df_orders', task_ids='extract_task')
            products_data = context['ti'].xcom_pull(key='df_products', task_ids='extract_task')
            customers_data = context['ti'].xcom_pull(key='df_customers', task_ids='extract_task')
            
            df_orders = pd.DataFrame(orders_data)
            df_products = pd.DataFrame(products_data)
            df_customers = pd.DataFrame(customers_data)
            
            quality_results = []
            
            # Check 1: Null percentage
            logger.info("🔍 Checking nulls...")
            null_check_orders = check_nulls(df_orders, ['product_id', 'quantity', 'unit_price'], 
                                            QUALITY_CONFIG['null_threshold'])
            null_check_products = check_nulls(df_products, ['name', 'price'], 
                                              QUALITY_CONFIG['null_threshold'])
            quality_results.append(null_check_orders)
            quality_results.append(null_check_products)
            
            # Check 2: Duplicates
            logger.info("🔍 Checking duplicates...")
            dup_check_orders = check_duplicates(df_orders, 'order_detail_id')
            quality_results.append(dup_check_orders)
            
            # Check 3: Row count
            logger.info("🔍 Checking row counts...")
            row_check_orders = check_row_count(df_orders, QUALITY_CONFIG['min_rows'])
            row_check_products = check_row_count(df_products, QUALITY_CONFIG['min_rows'])
            quality_results.append(row_check_orders)
            quality_results.append(row_check_products)
            
            # Check 4: Revenue positive
            logger.info("🔍 Checking revenue values...")
            revenue_check = check_revenue_positive(df_orders)
            quality_results.append(revenue_check)
            
            # Check 5: Date range
            logger.info("🔍 Checking date ranges...")
            date_check = check_date_range(df_orders, 'create_date', QUALITY_CONFIG['date_future_days'])
            quality_results.append(date_check)
            
            # Generate quality report
            quality_report = generate_quality_report(quality_results)
            
            # Log results
            logger.info(f"📊 Quality Report: {quality_report}")
            
            # Check if any critical checks failed
            critical_failures = [r for r in quality_results if not r['status'] and r.get('critical', True)]
            
            if critical_failures:
                error_msg = f"Critical quality checks failed: {[f['check_name'] for f in critical_failures]}"
                logger.error(f"❌ {error_msg}")
                context['ti'].xcom_push(key='quality_failed', value=True)
                context['ti'].xcom_push(key='quality_error', value=error_msg)
                raise Exception(error_msg)
            
            logger.info("✅ QUALITY CHECK task hoàn thành - Tất cả checks passed")
            context['ti'].xcom_push(key='quality_failed', value=False)
            context['ti'].xcom_push(key='quality_report', value=quality_report)
            
            return quality_report
            
        except Exception as e:
            logger.error(f"❌ QUALITY CHECK task thất bại: {e}")
            context['ti'].xcom_push(key='quality_failed', value=True)
            context['ti'].xcom_push(key='quality_error', value=str(e))
            raise
    
    # ========================================
    # TASK 3: TRANSFORM DATA
    # ========================================
    @task
    def transform_task(**context):
        """
        Transform data - clean, calculate metrics, build dimensions
        """
        logger = logging.getLogger(__name__)
        logger.info("🔄 Bắt đầu TRANSFORM task")
        
        try:
            # Retrieve data from XCom
            orders_data = context['ti'].xcom_pull(key='df_orders', task_ids='extract_task')
            products_data = context['ti'].xcom_pull(key='df_products', task_ids='extract_task')
            customers_data = context['ti'].xcom_pull(key='df_customers', task_ids='extract_task')
            categories_data = context['ti'].xcom_pull(key='df_categories', task_ids='extract_task')
            
            df_orders = pd.DataFrame(orders_data)
            df_products = pd.DataFrame(products_data)
            df_customers = pd.DataFrame(customers_data)
            df_categories = pd.DataFrame(categories_data)
            
            # Transform orders
            logger.info("🔄 Transforming orders...")
            df_orders_transformed = transform_orders(df_orders, df_products, df_customers)
            
            # Transform dimension tables
            logger.info("🔄 Transforming dimensions...")
            dim_data = transform_dim_tables(df_products, df_customers, df_categories)
            
            # Build date dimension
            logger.info("🔄 Building date dimension...")
            start_date = df_orders_transformed['create_date'].min().date()
            end_date = df_orders_transformed['create_date'].max().date()
            df_dim_date = build_dim_date(start_date, end_date)
            
            # Validate revenue
            logger.info("🔄 Validating revenue...")
            df_orders_transformed = validate_revenue(df_orders_transformed)
            
            # Store in XCom
            context['ti'].xcom_push(key='df_orders_transformed', value=df_orders_transformed.to_dict('records'))
            context['ti'].xcom_push(key='dim_data', value={
                'products': dim_data['products'].to_dict('records'),
                'customers': dim_data['customers'].to_dict('records'),
                'categories': dim_data['categories'].to_dict('records'),
                'date': df_dim_date.to_dict('records')
            })
            
            logger.info(f"✅ TRANSFORM task hoàn thành")
            logger.info(f"   - Orders: {len(df_orders_transformed)} rows")
            logger.info(f"   - Date dimension: {len(df_dim_date)} rows")
            
            return {
                'orders': len(df_orders_transformed),
                'date_dim': len(df_dim_date)
            }
            
        except Exception as e:
            logger.error(f"❌ TRANSFORM task thất bại: {e}")
            raise
    
    # ========================================
    # TASK 4: LOAD WAREHOUSE
    # ========================================
    @task
    def load_warehouse_task(**context):
        """
        Load transformed data into warehouse schema
        """
        logger = logging.getLogger(__name__)
        logger.info("📤 Bắt đầu LOAD WAREHOUSE task")
        
        try:
            # Retrieve transformed data from XCom
            orders_data = context['ti'].xcom_pull(key='df_orders_transformed', task_ids='transform_task')
            dim_data_dict = context['ti'].xcom_pull(key='dim_data', task_ids='transform_task')
            
            df_orders = pd.DataFrame(orders_data)
            df_products = pd.DataFrame(dim_data_dict['products'])
            df_customers = pd.DataFrame(dim_data_dict['customers'])
            df_categories = pd.DataFrame(dim_data_dict['categories'])
            df_date = pd.DataFrame(dim_data_dict['date'])
            
            mssql = MsSqlHook(mssql_conn_id=MSSQL_CONN_ID)
            conn = mssql.get_conn()
            cursor = conn.cursor()
            
            rows_loaded = 0
            
            # Load dimensions
            logger.info("📤 Loading dimensions...")
            
            # Load categories
            for _, row in df_categories.iterrows():
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
            for _, row in df_products.iterrows():
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
            for _, row in df_customers.iterrows():
                try:
                    join_date = row['join_date'].date() if pd.notna(row['join_date']) else None
                    cursor.execute(f"""
                        IF NOT EXISTS (SELECT 1 FROM {WAREHOUSE_SCHEMA}.dim_customer WHERE username = ?)
                        INSERT INTO {WAREHOUSE_SCHEMA}.dim_customer (username, fullname, email, phone, address, join_date, join_year, join_month)
                        VALUES (?, ?, ?, ?, ?, ?, ?, ?)
                    """, str(row['username']), str(row['username']), str(row['fullname']), str(row['email']),
                         str(row['phone']) if pd.notna(row['phone']) else None, str(row['address']) if pd.notna(row['address']) else None,
                         join_date, int(row['join_year']) if pd.notna(row['join_year']) else None,
                         int(row['join_month']) if pd.notna(row['join_month']) else None)
                    rows_loaded += 1
                except Exception as e:
                    logger.warning(f"   ⚠️ Lỗi insert customer: {e}")
            
            # Load date dimension
            for _, row in df_date.iterrows():
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
            logger.info("📤 Loading fact orders...")
            
            # Get key mappings
            cursor.execute(f"SELECT product_key, product_id FROM {WAREHOUSE_SCHEMA}.dim_product")
            product_map = {row[1]: row[0] for row in cursor.fetchall()}
            
            cursor.execute(f"SELECT customer_key, username FROM {WAREHOUSE_SCHEMA}.dim_customer")
            customer_map = {row[1]: row[0] for row in cursor.fetchall()}
            
            for _, row in df_orders.iterrows():
                try:
                    product_key = product_map.get(int(row['product_id']))
                    customer_key = customer_map.get(str(row['username']))
                    date_key = row['date_key']
                    
                    if product_key is None or customer_key is None or date_key is None:
                        continue
                    
                    cursor.execute(f"""
                        INSERT INTO {WAREHOUSE_SCHEMA}.fact_orders 
                        (order_id, product_key, customer_key, date_key, size_id, quantity, unit_price, revenue, 
                         final_total, status, payment_method, phone, address, order_date, order_time)
                        VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)
                    """, int(row['order_id']), product_key, customer_key, date_key,
                         int(row['size_id']) if pd.notna(row['size_id']) else None,
                         int(row['quantity']), float(row['unit_price']), float(row['revenue']),
                         float(row['final_total']) if pd.notna(row['final_total']) else None,
                         str(row['status']) if pd.notna(row['status']) else None,
                         str(row['payment_method']) if pd.notna(row['payment_method']) else None,
                         str(row['phone']) if pd.notna(row['phone']) else None,
                         str(row['address']) if pd.notna(row['address']) else None,
                         row['order_date'], row['order_time'])
                    rows_loaded += 1
                except Exception as e:
                    logger.warning(f"   ⚠️ Lỗi insert order: {e}")
            
            conn.commit()
            conn.close()
            
            logger.info(f"✅ LOAD WAREHOUSE task hoàn thành - {rows_loaded} rows loaded")
            context['ti'].xcom_push(key='rows_loaded', value=rows_loaded)
            
            return rows_loaded
            
        except Exception as e:
            logger.error(f"❌ LOAD WAREHOUSE task thất bại: {e}")
            raise
    
    # ========================================
    # TASK 5: BACKUP TO AZURE
    # ========================================
    @task
    def backup_azure_task(**context):
        """
        Upload data to Azure Blob Storage for backup and data lake
        """
        logger = logging.getLogger(__name__)
        logger.info("☁️ Bắt đầu AZURE BACKUP task")
        
        try:
            # Retrieve data from XCom
            orders_data = context['ti'].xcom_pull(key='df_orders_transformed', task_ids='transform_task')
            dim_data_dict = context['ti'].xcom_pull(key='dim_data', task_ids='transform_task')
            
            df_orders = pd.DataFrame(orders_data)
            df_products = pd.DataFrame(dim_data_dict['products'])
            df_customers = pd.DataFrame(dim_data_dict['customers'])
            df_categories = pd.DataFrame(dim_data_dict['categories'])
            
            # Connect to Azure Blob Storage
            blob_service_client = connect_azure_blob()
            
            # Get current date for partitioning
            execution_date = context['ds']
            year = execution_date[:4]
            month = execution_date[5:7]
            day = execution_date[8:10]
            
            container_name = 'ecommerce-datalake'
            
            # Upload orders with partitioning
            orders_blob_path = f"raw/orders/year={year}/month={month}/orders_{execution_date}.csv"
            upload_to_blob(df_orders, container_name, orders_blob_path, blob_service_client)
            
            # Upload products
            products_blob_path = f"raw/products/products_{execution_date}.csv"
            upload_to_blob(df_products, container_name, products_blob_path, blob_service_client)
            
            # Upload customers
            customers_blob_path = f"raw/customers/customers_{execution_date}.csv"
            upload_to_blob(df_customers, container_name, customers_blob_path, blob_service_client)
            
            # Upload warehouse data
            warehouse_blob_path = f"warehouse/fact_orders/fact_orders_{execution_date}.csv"
            upload_to_blob(df_orders, container_name, warehouse_blob_path, blob_service_client)
            
            logger.info(f"✅ AZURE BACKUP task hoàn thành")
            logger.info(f"   - Container: {container_name}")
            logger.info(f"   - Partition: year={year}/month={month}")
            
            return {
                'container': container_name,
                'partition': f"year={year}/month={month}"
            }
            
        except Exception as e:
            logger.error(f"❌ AZURE BACKUP task thất bại: {e}")
            raise
    
    # ========================================
    # TASK 6: SEND NOTIFICATION
    # ========================================
    @task
    def notify_task(**context):
        """
        Send email notification on pipeline completion
        """
        logger = logging.getLogger(__name__)
        logger.info("📧 Bắt đầu NOTIFY task")
        
        try:
            # Check if quality check failed
            quality_failed = context['ti'].xcom_pull(key='quality_failed', task_ids='quality_check_task')
            
            if quality_failed:
                # Send failure alert
                error_message = context['ti'].xcom_pull(key='quality_error', task_ids='quality_check_task')
                failed_task = context['ti'].xcom_pull(key='failed_task', default='quality_check_task')
                
                send_failure_alert(
                    pipeline_name='ecommerce_etl_pipeline',
                    error_message=error_message,
                    failed_task=failed_task
                )
                
                logger.error(f"❌ Sent failure alert: {error_message}")
            else:
                # Send success alert
                rows_loaded = context['ti'].xcom_pull(key='rows_loaded', task_ids='load_warehouse_task')
                duration = (context['ti'].logical_date - context['ti'].execution_date).total_seconds()
                
                send_success_alert(
                    pipeline_name='ecommerce_etl_pipeline',
                    rows_loaded=rows_loaded,
                    duration=duration
                )
                
                logger.info(f"✅ Sent success alert: {rows_loaded} rows loaded in {duration:.2f}s")
            
            logger.info("✅ NOTIFY task hoàn thành")
            
        except Exception as e:
            logger.error(f"❌ NOTIFY task thất bại: {e}")
            # Don't raise - notification failure shouldn't fail the pipeline
            logger.warning("⚠️ Notification failed but pipeline completed")
    
    # ========================================
    # TASK DEPENDENCIES
    # ========================================
    extract = extract_task()
    quality_check = quality_check_task()
    transform = transform_task()
    load_warehouse = load_warehouse_task()
    backup_azure = backup_azure_task()
    notify = notify_task()
    
    # Define task dependencies
    extract >> quality_check >> transform >> load_warehouse >> backup_azure >> notify

# ========================================
# DAG INSTANTIATION
# ========================================
ecommerce_etl_dag = ecommerce_etl_pipeline()
