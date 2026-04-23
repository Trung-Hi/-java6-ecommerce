#!/usr/bin/env python3
"""
E-Commerce Data Platform v2.0 - Data Quality Module
==================================================
Data quality checks using custom validation functions.
Raises exceptions for critical failures.

Author: Data Engineering Team
Date: 2024
"""

import pandas as pd
import numpy as np
from datetime import datetime, timedelta
import logging

logger = logging.getLogger(__name__)

# ========================================
# QUALITY CONFIGURATION
# ========================================
QUALITY_THRESHOLDS = {
    'null_threshold': 0.05,           # 5% null threshold
    'min_rows': 10,                    # Minimum rows for extraction (lowered for test)
    'date_future_days': 1,            # Allow dates up to 1 day in future
}

# ========================================
# QUALITY CHECK FUNCTIONS
# ========================================

def check_nulls(df, columns, threshold=0.05):
    """
    Check if null percentage in specified columns exceeds threshold.
    
    Args:
        df (pd.DataFrame): Input DataFrame
        columns (list): List of column names to check
        threshold (float): Maximum allowed null percentage (0-1)
        
    Returns:
        dict: Check result with status and details
    """
    logger.info(f"🔍 Checking nulls for columns: {columns}")
    
    result = {
        'check_name': 'null_check',
        'status': True,
        'critical': True,
        'failed_rows': 0,
        'message': '',
        'timestamp': datetime.now().isoformat()
    }
    
    try:
        for column in columns:
            if column not in df.columns:
                logger.warning(f"   Column {column} not found in DataFrame")
                continue
            
            null_count = df[column].isnull().sum()
            null_pct = null_count / len(df)
            
            if null_pct > threshold:
                result['status'] = False
                result['failed_rows'] += null_count
                result['message'] += f"{column}: {null_pct:.1%} nulls (threshold: {threshold:.1%}); "
                logger.error(f"   ❌ {column}: {null_pct:.1%} nulls exceeds threshold")
            else:
                logger.info(f"   ✅ {column}: {null_pct:.1%} nulls within threshold")
        
        if result['status']:
            result['message'] = 'All null percentages within threshold'
        
    except Exception as e:
        result['status'] = False
        result['message'] = f'Error checking nulls: {str(e)}'
        logger.error(f"❌ Error in null check: {e}")
    
    return result


def check_duplicates(df, key_column):
    """
    Check if duplicates exist based on key column.
    
    Args:
        df (pd.DataFrame): Input DataFrame
        key_column (str): Column to check for duplicates
        
    Returns:
        dict: Check result with status and details
    """
    logger.info(f"🔍 Checking duplicates for column: {key_column}")
    
    result = {
        'check_name': 'duplicate_check',
        'status': True,
        'critical': True,
        'failed_rows': 0,
        'message': '',
        'timestamp': datetime.now().isoformat()
    }
    
    try:
        if key_column not in df.columns:
            result['status'] = False
            result['message'] = f'Column {key_column} not found in DataFrame'
            logger.error(f"❌ Column {key_column} not found")
            return result
        
        duplicate_count = df.duplicated(subset=[key_column]).sum()
        
        if duplicate_count > 0:
            result['status'] = False
            result['failed_rows'] = duplicate_count
            result['message'] = f'Found {duplicate_count} duplicate rows in {key_column}'
            logger.error(f"   ❌ Found {duplicate_count} duplicate rows")
        else:
            result['message'] = 'No duplicates found'
            logger.info(f"   ✅ No duplicates found")
        
    except Exception as e:
        result['status'] = False
        result['message'] = f'Error checking duplicates: {str(e)}'
        logger.error(f"❌ Error in duplicate check: {e}")
    
    return result


def check_revenue_positive(df):
    """
    Check if all revenue values are positive.
    
    Args:
        df (pd.DataFrame): Input DataFrame with revenue column
        
    Returns:
        dict: Check result with status and details
    """
    logger.info("🔍 Checking revenue values...")
    
    result = {
        'check_name': 'revenue_positive_check',
        'status': True,
        'critical': True,
        'failed_rows': 0,
        'message': '',
        'timestamp': datetime.now().isoformat()
    }
    
    try:
        if 'revenue' not in df.columns:
            result['status'] = False
            result['message'] = 'Revenue column not found in DataFrame'
            logger.error("❌ Revenue column not found")
            return result
        
        # Check for negative or zero revenue
        invalid_revenue = df[df['revenue'] <= 0]
        invalid_count = len(invalid_revenue)
        
        if invalid_count > 0:
            result['status'] = False
            result['failed_rows'] = invalid_count
            result['message'] = f'Found {invalid_count} rows with revenue <= 0'
            logger.error(f"   ❌ Found {invalid_count} rows with revenue <= 0")
        else:
            result['message'] = 'All revenue values are positive'
            logger.info(f"   ✅ All revenue values are positive")
        
    except Exception as e:
        result['status'] = False
        result['message'] = f'Error checking revenue: {str(e)}'
        logger.error(f"❌ Error in revenue check: {e}")
    
    return result


def check_row_count(df, min_rows):
    """
    Check if DataFrame has minimum required rows.
    
    Args:
        df (pd.DataFrame): Input DataFrame
        min_rows (int): Minimum required row count
        
    Returns:
        dict: Check result with status and details
    """
    logger.info(f"🔍 Checking row count (minimum: {min_rows})...")
    
    result = {
        'check_name': 'row_count_check',
        'status': True,
        'critical': True,
        'failed_rows': 0,
        'message': '',
        'timestamp': datetime.now().isoformat()
    }
    
    try:
        row_count = len(df)
        
        if row_count < min_rows:
            result['status'] = False
            result['failed_rows'] = row_count
            result['message'] = f'Only {row_count} rows (minimum: {min_rows})'
            logger.error(f"   ❌ Only {row_count} rows (minimum: {min_rows})")
        else:
            result['message'] = f'{row_count} rows (meets minimum: {min_rows})'
            logger.info(f"   ✅ {row_count} rows (meets minimum: {min_rows})")
        
    except Exception as e:
        result['status'] = False
        result['message'] = f'Error checking row count: {str(e)}'
        logger.error(f"❌ Error in row count check: {e}")
    
    return result


def check_date_range(df, date_col, future_days=1):
    """
    Check if dates are not too far in the future.
    
    Args:
        df (pd.DataFrame): Input DataFrame
        date_col (str): Date column to check
        future_days (int): Maximum allowed days in future
        
    Returns:
        dict: Check result with status and details
    """
    logger.info(f"🔍 Checking date range for {date_col} (max future: {future_days} days)...")
    
    result = {
        'check_name': 'date_range_check',
        'status': True,
        'critical': True,
        'failed_rows': 0,
        'message': '',
        'timestamp': datetime.now().isoformat()
    }
    
    try:
        if date_col not in df.columns:
            result['status'] = False
            result['message'] = f'Column {date_col} not found in DataFrame'
            logger.error(f"❌ Column {date_col} not found")
            return result
        
        # Convert to datetime if not already
        if not pd.api.types.is_datetime64_any_dtype(df[date_col]):
            df[date_col] = pd.to_datetime(df[date_col], errors='coerce')
        
        max_future_date = datetime.now() + timedelta(days=future_days)
        future_dates = df[df[date_col] > max_future_date]
        future_count = len(future_dates)
        
        if future_count > 0:
            result['status'] = False
            result['failed_rows'] = future_count
            result['message'] = f'Found {future_count} dates more than {future_days} days in future'
            logger.error(f"   ❌ Found {future_count} dates too far in future")
        else:
            result['message'] = 'All dates within valid range'
            logger.info(f"   ✅ All dates within valid range")
        
    except Exception as e:
        result['status'] = False
        result['message'] = f'Error checking date range: {str(e)}'
        logger.error(f"❌ Error in date range check: {e}")
    
    return result


def check_data_types(df, expected_types):
    """
    Check if DataFrame columns have expected data types.
    
    Args:
        df (pd.DataFrame): Input DataFrame
        expected_types (dict): Dictionary mapping column names to expected types
        
    Returns:
        dict: Check result with status and details
    """
    logger.info("🔍 Checking data types...")
    
    result = {
        'check_name': 'data_type_check',
        'status': True,
        'critical': False,  # Non-critical check
        'failed_rows': 0,
        'message': '',
        'timestamp': datetime.now().isoformat()
    }
    
    try:
        type_mismatches = []
        
        for column, expected_type in expected_types.items():
            if column not in df.columns:
                type_mismatches.append(f"{column}: not found")
                continue
            
            actual_type = str(df[column].dtype)
            
            # Simplified type matching
            type_match = False
            if 'int' in expected_type.lower() and 'int' in actual_type.lower():
                type_match = True
            elif 'float' in expected_type.lower() and ('float' in actual_type.lower() or 'int' in actual_type.lower()):
                type_match = True
            elif 'str' in expected_type.lower() and ('str' in actual_type.lower() or 'obj' in actual_type.lower()):
                type_match = True
            elif 'datetime' in expected_type.lower() and 'datetime' in actual_type.lower():
                type_match = True
            
            if not type_match:
                type_mismatches.append(f"{column}: expected {expected_type}, got {actual_type}")
        
        if type_mismatches:
            result['status'] = False
            result['message'] = f'Type mismatches: {"; ".join(type_mismatches)}'
            logger.warning(f"   ⚠️ Type mismatches: {type_mismatches}")
        else:
            result['message'] = 'All data types match expectations'
            logger.info(f"   ✅ All data types match")
        
    except Exception as e:
        result['status'] = False
        result['message'] = f'Error checking data types: {str(e)}'
        logger.error(f"❌ Error in data type check: {e}")
    
    return result


def check_referential_integrity(df_parent, df_child, parent_key, child_key):
    """
    Check referential integrity between parent and child DataFrames.
    
    Args:
        df_parent (pd.DataFrame): Parent DataFrame
        df_child (pd.DataFrame): Child DataFrame
        parent_key (str): Key column in parent
        child_key (str): Key column in child
        
    Returns:
        dict: Check result with status and details
    """
    logger.info(f"🔍 Checking referential integrity ({parent_key} -> {child_key})...")
    
    result = {
        'check_name': 'referential_integrity_check',
        'status': True,
        'critical': True,
        'failed_rows': 0,
        'message': '',
        'timestamp': datetime.now().isoformat()
    }
    
    try:
        if parent_key not in df_parent.columns or child_key not in df_child.columns:
            result['status'] = False
            result['message'] = 'Key columns not found'
            logger.error("❌ Key columns not found")
            return result
        
        # Get unique keys
        parent_keys = set(df_parent[parent_key].dropna().unique())
        child_keys = set(df_child[child_key].dropna().unique())
        
        # Find orphaned records
        orphaned_keys = child_keys - parent_keys
        orphaned_count = len(orphaned_keys)
        
        if orphaned_count > 0:
            result['status'] = False
            result['failed_rows'] = orphaned_count
            result['message'] = f'Found {orphaned_count} orphaned records'
            logger.error(f"   ❌ Found {orphaned_count} orphaned records")
        else:
            result['message'] = 'Referential integrity maintained'
            logger.info(f"   ✅ Referential integrity maintained")
        
    except Exception as e:
        result['status'] = False
        result['message'] = f'Error checking referential integrity: {str(e)}'
        logger.error(f"❌ Error in referential integrity check: {e}")
    
    return result


def generate_quality_report(results):
    """
    Generate a quality report from check results.
    
    Args:
        results (list): List of check result dictionaries
        
    Returns:
        dict: Quality report summary
    """
    logger.info("📊 Generating quality report...")
    
    report = {
        'timestamp': datetime.now().isoformat(),
        'total_checks': len(results),
        'passed_checks': sum(1 for r in results if r['status']),
        'failed_checks': sum(1 for r in results if not r['status']),
        'critical_failures': sum(1 for r in results if not r['status'] and r.get('critical', True)),
        'total_failed_rows': sum(r.get('failed_rows', 0) for r in results),
        'checks': results
    }
    
    logger.info(f"   Total checks: {report['total_checks']}")
    logger.info(f"   Passed: {report['passed_checks']}")
    logger.info(f"   Failed: {report['failed_checks']}")
    logger.info(f"   Critical failures: {report['critical_failures']}")
    
    return report


def run_quality_checks(df, config=QUALITY_THRESHOLDS):
    """
    Run all quality checks on a DataFrame.
    
    Args:
        df (pd.DataFrame): Input DataFrame
        config (dict): Quality configuration thresholds
        
    Returns:
        dict: Quality report
    """
    logger.info("🔍 Running all quality checks...")
    
    results = []
    
    # Run checks based on DataFrame structure
    if 'revenue' in df.columns:
        results.append(check_revenue_positive(df))
    
    # Check duplicates - use order_detail_id for joined orders, otherwise order_id
    if 'order_detail_id' in df.columns:
        results.append(check_duplicates(df, 'order_detail_id'))
    elif 'order_id' in df.columns or 'id' in df.columns:
        key_col = 'order_id' if 'order_id' in df.columns else 'id'
        results.append(check_duplicates(df, key_col))
    
    results.append(check_row_count(df, config['min_rows']))
    
    if 'create_date' in df.columns or 'created_at' in df.columns:
        date_col = 'create_date' if 'create_date' in df.columns else 'created_at'
        results.append(check_date_range(df, date_col, config['date_future_days']))
    
    # Check nulls for important columns
    important_cols = ['quantity', 'unit_price', 'product_id']
    existing_cols = [col for col in important_cols if col in df.columns]
    if existing_cols:
        results.append(check_nulls(df, existing_cols, config['null_threshold']))
    
    return generate_quality_report(results)


# ========================================
# MAIN EXECUTION
# ========================================
if __name__ == "__main__":
    # Configure logging
    logging.basicConfig(
        level=logging.INFO,
        format='%(asctime)s - %(levelname)s - %(message)s'
    )
    
    # Test quality functions
    print("Quality module loaded successfully")
