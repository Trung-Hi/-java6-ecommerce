#!/usr/bin/env python3
"""
E-Commerce Data Platform v2.0 - ETL Transform Module
=====================================================
Transformation functions to clean, validate, and prepare data
for loading into the data warehouse.

Author: Data Engineering Team
Date: 2024
"""

import pandas as pd
import numpy as np
from datetime import datetime, timedelta
import logging

logger = logging.getLogger(__name__)

# ========================================
# TRANSFORMATION FUNCTIONS
# ========================================

def clean_nulls(df):
    """
    Clean null values in DataFrame based on column-specific strategies.
    
    Strategy:
    - Numeric columns: fill with 0 or median
    - String columns: fill with 'Unknown' or empty string
    - Date columns: fill with current date or drop
    
    Args:
        df (pd.DataFrame): Input DataFrame
        
    Returns:
        pd.DataFrame: DataFrame with nulls cleaned
    """
    logger.info("🧹 Cleaning null values...")
    
    df_clean = df.copy()
    
    for column in df_clean.columns:
        null_count = df_clean[column].isnull().sum()
        if null_count == 0:
            continue
            
        null_pct = null_count / len(df_clean) * 100
        
        # Numeric columns
        if pd.api.types.is_numeric_dtype(df_clean[column]):
            if null_pct > 50:
                # Too many nulls, fill with 0
                df_clean[column] = df_clean[column].fillna(0)
                logger.info(f"   Filled {column} ({null_pct:.1f}% nulls) with 0")
            else:
                # Fill with median
                median_val = df_clean[column].median()
                df_clean[column] = df_clean[column].fillna(median_val)
                logger.info(f"   Filled {column} ({null_pct:.1f}% nulls) with median: {median_val}")
        
        # String columns
        elif pd.api.types.is_string_dtype(df_clean[column]):
            if 'email' in column.lower():
                df_clean[column] = df_clean[column].fillna('unknown@example.com')
            elif 'name' in column.lower():
                df_clean[column] = df_clean[column].fillna('Unknown')
            else:
                df_clean[column] = df_clean[column].fillna('')
            logger.info(f"   Filled {column} ({null_pct:.1f}% nulls) with default value")
        
        # Date columns
        elif pd.api.types.is_datetime64_any_dtype(df_clean[column]):
            if null_pct > 10:
                # Drop rows with null dates
                df_clean = df_clean.dropna(subset=[column])
                logger.info(f"   Dropped {null_count} rows with null {column}")
            else:
                # Fill with current date
                df_clean[column] = df_clean[column].fillna(datetime.now())
                logger.info(f"   Filled {column} ({null_pct:.1f}% nulls) with current date")
    
    return df_clean


def fix_data_types(df):
    """
    Ensure correct data types for all columns.
    
    Args:
        df (pd.DataFrame): Input DataFrame
        
    Returns:
        pd.DataFrame: DataFrame with correct data types
    """
    logger.info("🔧 Fixing data types...")
    
    df_typed = df.copy()
    
    # Define expected types based on column names
    type_mapping = {
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
    
    for column in df_typed.columns:
        # Find matching type from mapping
        for pattern, dtype in type_mapping.items():
            if pattern in column.lower():
                try:
                    if dtype == 'int64':
                        df_typed[column] = pd.to_numeric(df_typed[column], errors='coerce').astype('Int64')
                    elif dtype == 'float64':
                        df_typed[column] = pd.to_numeric(df_typed[column], errors='coerce')
                    elif dtype == 'string':
                        df_typed[column] = df_typed[column].astype('string')
                    elif dtype == 'datetime64[ns]':
                        df_typed[column] = pd.to_datetime(df_typed[column], errors='coerce')
                    elif dtype == 'bool':
                        df_typed[column] = df_typed[column].astype('bool')
                    
                    logger.info(f"   Converted {column} to {dtype}")
                    break
                except Exception as e:
                    logger.warning(f"   Could not convert {column} to {dtype}: {e}")
    
    return df_typed


def calculate_metrics(df):
    """
    Calculate derived metrics and add new columns.
    
    Metrics calculated:
    - revenue = quantity * unit_price
    - order_month = month from date
    - order_year = year from date
    - order_hour = hour from datetime
    
    Args:
        df (pd.DataFrame): Input DataFrame
        
    Returns:
        pd.DataFrame: DataFrame with calculated metrics
    """
    logger.info("📊 Calculating metrics...")
    
    df_metrics = df.copy()
    
    # Calculate revenue if columns exist
    if 'quantity' in df_metrics.columns and 'unit_price' in df_metrics.columns:
        df_metrics['revenue'] = df_metrics['quantity'] * df_metrics['unit_price']
        logger.info("   Calculated revenue = quantity * unit_price")
    
    # Extract date components
    date_cols = [col for col in df_metrics.columns if 'date' in col.lower() or 'created_at' in col.lower()]
    for date_col in date_cols:
        if pd.api.types.is_datetime64_any_dtype(df_metrics[date_col]):
            df_metrics[f'{date_col}_year'] = df_metrics[date_col].dt.year
            df_metrics[f'{date_col}_month'] = df_metrics[date_col].dt.month
            df_metrics[f'{date_col}_day'] = df_metrics[date_col].dt.day
            df_metrics[f'{date_col}_hour'] = df_metrics[date_col].dt.hour
            logger.info(f"   Extracted date components from {date_col}")
    
    return df_metrics


def remove_duplicates(df, key_column='order_id'):
    """
    Remove duplicate rows based on key column.
    
    Args:
        df (pd.DataFrame): Input DataFrame
        key_column (str): Column to use for duplicate detection
        
    Returns:
        pd.DataFrame: DataFrame with duplicates removed
    """
    logger.info(f"🗑️ Removing duplicates based on {key_column}...")
    
    initial_count = len(df)
    df_deduped = df.drop_duplicates(subset=[key_column], keep='first')
    duplicates_removed = initial_count - len(df_deduped)
    
    if duplicates_removed > 0:
        logger.info(f"   Removed {duplicates_removed} duplicate rows")
    else:
        logger.info("   No duplicates found")
    
    return df_deduped


def build_dim_date(start_date, end_date):
    """
    Build complete date dimension table for a date range.
    
    Args:
        start_date (datetime.date): Start date
        end_date (datetime.date): End date
        
    Returns:
        pd.DataFrame: Date dimension DataFrame
    """
    logger.info(f"📅 Building date dimension from {start_date} to {end_date}...")
    
    date_range = pd.date_range(start=start_date, end=end_date, freq='D')
    
    dim_date_data = []
    for date in date_range:
        date_key = int(date.strftime('%Y%m%d'))
        weekday_name = date.strftime('%A')
        is_weekend = 1 if date.weekday() >= 5 else 0
        quarter = (date.month - 1) // 3 + 1
        week_of_year = date.isocalendar()[1]
        
        dim_date_data.append({
            'date_key': date_key,
            'full_date': date.date(),
            'day': date.day,
            'month': date.month,
            'year': date.year,
            'quarter': quarter,
            'weekday_name': weekday_name,
            'is_weekend': is_weekend,
            'is_holiday': 0,
            'week_of_year': week_of_year
        })
    
    df_dim_date = pd.DataFrame(dim_date_data)
    logger.info(f"   Generated {len(df_dim_date)} date dimension rows")
    
    return df_dim_date


def validate_revenue(df):
    """
    Validate revenue values and flag invalid rows.
    
    Flags:
    - negative_revenue: revenue < 0
    - zero_revenue: revenue == 0
    - invalid_revenue: NaN or null
    
    Args:
        df (pd.DataFrame): Input DataFrame with revenue column
        
    Returns:
        pd.DataFrame: DataFrame with validation flags
    """
    logger.info("✅ Validating revenue values...")
    
    df_validated = df.copy()
    
    if 'revenue' in df_validated.columns:
        # Flag negative revenue
        df_validated['negative_revenue'] = df_validated['revenue'] < 0
        negative_count = df_validated['negative_revenue'].sum()
        
        # Flag zero revenue
        df_validated['zero_revenue'] = df_validated['revenue'] == 0
        zero_count = df_validated['zero_revenue'].sum()
        
        # Flag invalid revenue
        df_validated['invalid_revenue'] = df_validated['revenue'].isnull()
        invalid_count = df_validated['invalid_revenue'].sum()
        
        logger.info(f"   Negative revenue: {negative_count} rows")
        logger.info(f"   Zero revenue: {zero_count} rows")
        logger.info(f"   Invalid revenue: {invalid_count} rows")
    
    return df_validated


def transform_orders(df_orders, df_products, df_customers):
    """
    Transform orders data for loading into fact table.
    ASM_Java5 structure: orders + order_details join.
    
    Args:
        df_orders (pd.DataFrame): Orders DataFrame
        df_products (pd.DataFrame): Products DataFrame
        df_customers (pd.DataFrame): Customers DataFrame
        
    Returns:
        pd.DataFrame: Transformed orders DataFrame
    """
    logger.info("🔄 Transforming orders data...")
    
    try:
        if df_orders.empty:
            logger.warning("⚠️ Orders DataFrame is empty")
            return pd.DataFrame()
        
        # Clean nulls
        df_orders = clean_nulls(df_orders)
        
        # Fix data types
        df_orders = fix_data_types(df_orders)
        
        # Handle null values for critical columns
        df_orders['quantity'] = df_orders['quantity'].fillna(0)
        df_orders['unit_price'] = df_orders['unit_price'].fillna(0)
        
        # Convert to numeric
        df_orders['quantity'] = pd.to_numeric(df_orders['quantity'], errors='coerce')
        df_orders['unit_price'] = pd.to_numeric(df_orders['unit_price'], errors='coerce')
        df_orders['create_date'] = pd.to_datetime(df_orders['create_date'], errors='coerce')
        
        # Drop rows with null critical values
        df_orders = df_orders.dropna(subset=['create_date', 'product_id'])
        
        # Calculate revenue
        df_orders['revenue'] = df_orders['quantity'] * df_orders['unit_price']
        
        # Remove duplicates based on order_detail_id
        df_orders = remove_duplicates(df_orders, 'order_detail_id')
        
        # Extract date components
        df_orders['order_date'] = df_orders['create_date'].dt.date
        df_orders['order_time'] = df_orders['create_date'].dt.time
        df_orders['date_key'] = df_orders['order_date'].apply(
            lambda x: int(x.strftime('%Y%m%d')) if pd.notna(x) else None
        )
        
        logger.info(f"✅ Transformed {len(df_orders)} order rows")
        return df_orders
        
    except Exception as e:
        logger.error(f"❌ Error transforming orders: {e}")
        raise


def transform_dim_tables(df_products, df_customers, df_categories):
    """
    Transform dimension tables for loading into warehouse.
    ASM_Java5 structure: accounts, products with varchar category_id.
    
    Args:
        df_products (pd.DataFrame): Products DataFrame
        df_customers (pd.DataFrame): Customers DataFrame
        df_categories (pd.DataFrame): Categories DataFrame
        
    Returns:
        dict: Dictionary containing transformed dimension DataFrames
    """
    logger.info("🔄 Transforming dimension tables...")
    
    try:
        # Transform products
        if not df_products.empty:
            df_products = clean_nulls(df_products)
            df_products['price'] = pd.to_numeric(df_products['price'], errors='coerce')
            df_products['discount'] = pd.to_numeric(df_products['discount'], errors='coerce')
            
            # Merge with categories
            df_products = df_products.merge(
                df_categories[['category_id', 'name']],
                on='category_id',
                how='left'
            )
            df_products = df_products.rename(columns={'name_y': 'category_name', 'name_x': 'name'})
            df_products['category_name'] = df_products['category_name'].fillna('Uncategorized')
            
            logger.info(f"✅ Transformed {len(df_products)} product rows")
        
        # Transform customers (accounts)
        if not df_customers.empty:
            df_customers = clean_nulls(df_customers)
            
            # Handle join_date if it exists (accounts table doesn't have created_at)
            if 'join_date' in df_customers.columns:
                df_customers['join_date'] = pd.to_datetime(df_customers['join_date'], errors='coerce')
                df_customers['join_year'] = df_customers['join_date'].dt.year
                df_customers['join_month'] = df_customers['join_date'].dt.month
            else:
                # Set default values if join_date doesn't exist
                from datetime import datetime
                df_customers['join_date'] = datetime.now()
                df_customers['join_year'] = datetime.now().year
                df_customers['join_month'] = datetime.now().month
            
            df_customers['email'] = df_customers['email'].fillna('unknown@example.com')
            df_customers['phone'] = df_customers['phone'].fillna('')
            df_customers['address'] = df_customers['address'].fillna('')
            
            logger.info(f"✅ Transformed {len(df_customers)} customer rows")
        
        # Transform categories
        if not df_categories.empty:
            df_categories = clean_nulls(df_categories)
            logger.info(f"✅ Transformed {len(df_categories)} category rows")
        
        return {
            'products': df_products,
            'customers': df_customers,
            'categories': df_categories
        }
        
    except Exception as e:
        logger.error(f"❌ Error transforming dimensions: {e}")
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
    
    # Test transformation functions
    print("Transform module loaded successfully")
