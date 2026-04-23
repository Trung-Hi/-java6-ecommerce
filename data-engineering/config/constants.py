#!/usr/bin/env python3
"""
E-Commerce Data Platform v2.0 - Configuration Constants
==========================================================
All configuration constants for the ETL pipeline.

Author: Data Engineering Team
Date: 2024
"""

import os

# ========================================
# DATABASE CONNECTION
# ========================================
SOURCE_CONNECTION_STRING = (
    "DRIVER={ODBC Driver 17 for SQL Server};"
    "SERVER=localhost,1433;"
    "DATABASE=ASM_Java5;"
    "Trusted_Connection=yes;"
    "TrustServerCertificate=yes;"
)

WAREHOUSE_SCHEMA = "ecommerce_warehouse"
MSSQL_CONN_ID = "mssql_default"

# ========================================
# AIRFLOW CONFIGURATION
# ========================================
DAG_ID = "ecommerce_etl_pipeline"
SCHEDULE_INTERVAL = "@daily"
CATCHUP = False
START_DATE = "2024-01-01"
RETRIES = 2
RETRY_DELAY_MINUTES = 5

# ========================================
# DATA QUALITY THRESHOLDS
# ========================================
QUALITY_CONFIG = {
    'null_threshold': 0.05,           # 5% null threshold
    'min_rows': 100,                   # Minimum rows for extraction
    'date_future_days': 1,            # Allow dates up to 1 day in future
}

# ========================================
# AZURE BLOB STORAGE
# ========================================
AZURE_CONNECTION_STRING = os.getenv('AZURE_STORAGE_CONNECTION_STRING', '')
AZURE_CONTAINER_NAME = 'ecommerce-datalake'

# ========================================
# EMAIL ALERTING
# ========================================
SMTP_SERVER = os.getenv('SMTP_SERVER', 'smtp.gmail.com')
SMTP_PORT = int(os.getenv('SMTP_PORT', '587'))
SMTP_USERNAME = os.getenv('SMTP_USERNAME', '')
SMTP_PASSWORD = os.getenv('SMTP_PASSWORD', '')
EMAIL_FROM = os.getenv('EMAIL_FROM', 'data-engineering@example.com')
EMAIL_TO = os.getenv('EMAIL_TO', 'data-team@example.com').split(',')

# ========================================
# LOGGING
# ========================================
LOG_LEVEL = "INFO"
LOG_FILE = "etl_pipeline.log"

# ========================================
# SLA CONFIGURATION
# ========================================
SLA_THRESHOLD_SECONDS = 3600  # 1 hour

# ========================================
# DATA TYPE MAPPINGS
# ========================================
DATA_TYPE_MAPPINGS = {
    # Integer columns
    'id': 'int64',
    'order_id': 'int64',
    'product_id': 'int64',
    'customer_id': 'int64',
    'category_id': 'int64',
    'quantity': 'int64',
    'size_id': 'int64',
    'year': 'int64',
    'month': 'int64',
    'day': 'int64',
    'quarter': 'int64',
    'week_of_year': 'int64',
    
    # Decimal columns
    'price': 'float64',
    'unit_price': 'float64',
    'discount': 'float64',
    'revenue': 'float64',
    'final_total': 'float64',
    'total_spent': 'float64',
    
    # String columns
    'username': 'string',
    'email': 'string',
    'name': 'string',
    'fullname': 'string',
    'status': 'string',
    'payment_method': 'string',
    'phone': 'string',
    'address': 'string',
    'category_name': 'string',
    'weekday_name': 'string',
    
    # Date columns
    'created_at': 'datetime64[ns]',
    'updated_at': 'datetime64[ns]',
    'join_date': 'datetime64[ns]',
    'create_date': 'datetime64[ns]',
    'order_date': 'datetime64[ns]',
    'full_date': 'datetime64[ns]',
    
    # Boolean columns
    'is_deleted': 'bool',
    'is_active': 'bool',
    'is_weekend': 'bool',
    'is_holiday': 'bool',
    'is_returned': 'bool',
}
