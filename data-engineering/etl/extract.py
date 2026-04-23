#!/usr/bin/env python3
"""
E-Commerce Data Platform v2.0 - ETL Extract Module
==================================================
Extraction functions to pull data from SQL Server source database.

Author: Data Engineering Team
Date: 2024
"""

import pyodbc
import pandas as pd
import logging
import sys
import os

# Add parent directory to path for imports
sys.path.append(os.path.dirname(os.path.dirname(os.path.abspath(__file__))))

from config.constants import SOURCE_CONNECTION_STRING

logger = logging.getLogger(__name__)

# ========================================
# EXTRACTION FUNCTIONS
# ========================================

def extract_orders(conn):
    """
    Extract data from orders and order_details tables.
    ASM_Java5 structure: orders + order_details join.
    
    Args:
        conn: pyodbc connection object
        
    Returns:
        pd.DataFrame: DataFrame containing order data with details
    """
    logger.info("📥 Extracting orders + order_details...")
    
    try:
        query = """
        SELECT 
            o.id as order_id,
            o.username,
            o.status,
            o.create_date,
            o.shipping_phone as phone,
            o.address,
            od.id as order_detail_id,
            od.product_id,
            od.size_id,
            od.quantity,
            od.price as unit_price
        FROM orders o
        LEFT JOIN order_details od ON o.id = od.order_id
        WHERE o.create_date IS NOT NULL
        """
        
        df = pd.read_sql(query, conn)
        logger.info(f"✅ Extracted {len(df)} rows from orders + order_details")
        
        return df
        
    except Exception as e:
        logger.error(f"❌ Error extracting orders: {e}")
        raise


def extract_products(conn):
    """
    Extract data from products table.
    ASM_Java5 structure: products table.
    
    Args:
        conn: pyodbc connection object
        
    Returns:
        pd.DataFrame: DataFrame containing product data
    """
    logger.info("📥 Extracting products...")
    
    try:
        query = """
        SELECT 
            id as product_id,
            name,
            image,
            category_id,
            price,
            discount
        FROM products
        WHERE is_delete = 0
        """
        
        df = pd.read_sql(query, conn)
        logger.info(f"✅ Extracted {len(df)} rows from products")
        
        return df
        
    except Exception as e:
        logger.error(f"❌ Error extracting products: {e}")
        raise


def extract_customers(conn):
    """
    Extract data from accounts table (customers).
    ASM_Java5 structure: accounts table (không có customers).
    
    Args:
        conn: pyodbc connection object
        
    Returns:
        pd.DataFrame: DataFrame containing customer data
    """
    logger.info("📥 Extracting customers (accounts)...")
    
    try:
        query = """
        SELECT 
            username,
            fullname,
            email,
            phone,
            address
        FROM accounts
        WHERE is_delete = 0
        """
        
        df = pd.read_sql(query, conn)
        logger.info(f"✅ Extracted {len(df)} rows from accounts")
        
        return df
        
    except Exception as e:
        logger.error(f"❌ Error extracting customers: {e}")
        raise


def extract_categories(conn):
    """
    Extract data from categories table.
    ASM_Java5 structure: categories table.
    
    Args:
        conn: pyodbc connection object
        
    Returns:
        pd.DataFrame: DataFrame containing category data
    """
    logger.info("📥 Extracting categories...")
    
    try:
        query = """
        SELECT 
            id as category_id,
            name,
            parent_id
        FROM categories
        """
        
        df = pd.read_sql(query, conn)
        logger.info(f"✅ Extracted {len(df)} rows from categories")
        
        return df
        
    except Exception as e:
        logger.error(f"❌ Error extracting categories: {e}")
        raise


def extract_all():
    """
    Extract all data from source database.
    Creates connection and calls all extract functions.
    
    Returns:
        dict: Dictionary containing all extracted DataFrames
    """
    logger.info("📥 Starting full extraction...")
    
    try:
        # Connect to database
        conn = pyodbc.connect(SOURCE_CONNECTION_STRING)
        logger.info("✅ Connected to SQL Server")
        
        # Extract all tables
        df_orders = extract_orders(conn)
        df_products = extract_products(conn)
        df_customers = extract_customers(conn)
        df_categories = extract_categories(conn)
        
        # Close connection
        conn.close()
        logger.info("🔌 Closed database connection")
        
        # Return as dictionary
        return {
            'orders': df_orders,
            'products': df_products,
            'customers': df_customers,
            'categories': df_categories
        }
        
    except Exception as e:
        logger.error(f"❌ Error in extract_all: {e}")
        raise


# ========================================
# MAIN EXECUTION
# ========================================
if __name__ == "__main__":
    # Configure logging
    logging.basicConfig(
        level=logging.INFO,
        format='%(asctime)s - %(levelname)s - %(message)s'
    )
    
    # Extract all data
    data = extract_all()
    
    # Print summary
    print("\n" + "=" * 60)
    print("EXTRACTION SUMMARY")
    print("=" * 60)
    for table_name, df in data.items():
        print(f"{table_name}: {len(df)} rows")
    print("=" * 60)
