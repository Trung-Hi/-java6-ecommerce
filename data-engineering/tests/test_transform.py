#!/usr/bin/env python3
"""
E-Commerce Data Platform v2.0 - Unit Tests for Transform Module
================================================================
Unit tests for transformation functions using pytest.

Author: Data Engineering Team
Date: 2024
"""

import pytest
import pandas as pd
import numpy as np
from datetime import datetime, date, timedelta
import sys
import os

# Add parent directory to path for imports
sys.path.append(os.path.dirname(os.path.dirname(os.path.abspath(__file__))))

from etl.transform import (
    clean_nulls, fix_data_types, calculate_metrics, remove_duplicates,
    build_dim_date, validate_revenue, transform_orders, transform_dim_tables
)


# ========================================
# TEST FIXTURES
# ========================================

@pytest.fixture
def sample_orders_df():
    """Create sample orders DataFrame for testing."""
    return pd.DataFrame({
        'order_id': [1, 2, 3, 4, 5],
        'username': ['user1', 'user2', 'user1', 'user3', 'user2'],
        'status': ['DELIVERED', 'PLACED', 'DELIVERED', 'CANCELLED', 'DELIVERED'],
        'create_date': pd.to_datetime(['2024-04-01', '2024-04-02', '2024-04-03', '2024-04-04', '2024-04-05']),
        'payment_method': ['CREDIT', 'COD', 'CREDIT', 'CREDIT', 'COD'],
        'final_total': [100.0, 200.0, 150.0, 50.0, 300.0],
        'phone': ['1234567890', '9876543210', None, '5551234567', '1112223333'],
        'address': ['Addr1', 'Addr2', 'Addr3', None, 'Addr5'],
        'order_detail_id': [101, 102, 103, 104, 105],
        'product_id': [1, 2, 1, 3, 2],
        'size_id': [1, 2, 1, 1, 2],
        'quantity': [2, 1, 3, 1, 2],
        'unit_price': [50.0, 200.0, 50.0, 50.0, 150.0]
    })


@pytest.fixture
def sample_products_df():
    """Create sample products DataFrame for testing."""
    return pd.DataFrame({
        'product_id': [1, 2, 3],
        'name': ['Product A', 'Product B', 'Product C'],
        'image': ['img1.jpg', 'img2.jpg', None],
        'category_id': ['CAT1', 'CAT2', 'CAT1'],
        'price': [50.0, 200.0, 75.0],
        'discount': [0.0, 10.0, 5.0]
    })


@pytest.fixture
def sample_customers_df():
    """Create sample customers DataFrame for testing."""
    return pd.DataFrame({
        'username': ['user1', 'user2', 'user3'],
        'fullname': ['User One', 'User Two', 'User Three'],
        'email': ['user1@example.com', 'user2@example.com', None],
        'phone': ['1234567890', None, '5551234567'],
        'address': ['Addr1', 'Addr2', 'Addr3'],
        'join_date': pd.to_datetime(['2024-01-01', '2024-02-01', '2024-03-01'])
    })


@pytest.fixture
def sample_categories_df():
    """Create sample categories DataFrame for testing."""
    return pd.DataFrame({
        'category_id': ['CAT1', 'CAT2'],
        'name': ['Category 1', 'Category 2'],
        'parent_id': [None, 'CAT1']
    })


@pytest.fixture
def df_with_nulls():
    """Create DataFrame with null values for testing."""
    return pd.DataFrame({
        'col1': [1, 2, None, 4, 5],
        'col2': [1.1, None, 3.3, 4.4, 5.5],
        'col3': ['a', 'b', None, 'd', 'e'],
        'col4': pd.to_datetime(['2024-01-01', None, '2024-01-03', '2024-01-04', '2024-01-05'])
    })


@pytest.fixture
def df_with_duplicates():
    """Create DataFrame with duplicate rows for testing."""
    return pd.DataFrame({
        'id': [1, 2, 3, 1, 2, 4],
        'value': [10, 20, 30, 10, 20, 40]
    })


# ========================================
# TEST: clean_nulls
# ========================================

def test_clean_nulls_removes_nulls_numeric(df_with_nulls):
    """Test that clean_nulls removes nulls from numeric columns."""
    df_clean = clean_nulls(df_with_nulls)
    
    # Check that nulls are filled
    assert df_clean['col1'].isnull().sum() == 0
    assert df_clean['col2'].isnull().sum() == 0
    
    # Check that numeric columns have numeric type
    assert pd.api.types.is_numeric_dtype(df_clean['col1'])
    assert pd.api.types.is_numeric_dtype(df_clean['col2'])


def test_clean_nulls_removes_nulls_string(df_with_nulls):
    """Test that clean_nulls removes nulls from string columns."""
    df_clean = clean_nulls(df_with_nulls)
    
    # Check that nulls are filled with default values
    assert df_clean['col3'].isnull().sum() == 0
    # Implementation fills with empty string, not 'Unknown'
    assert df_clean.loc[2, 'col3'] == ''


def test_clean_nulls_handles_date_columns(df_with_nulls):
    """Test that clean_nulls handles date columns appropriately."""
    df_clean = clean_nulls(df_with_nulls)
    
    # Check that date column still has datetime type
    assert pd.api.types.is_datetime64_any_dtype(df_clean['col4'])
    
    # Check that nulls are either filled or rows dropped
    null_count = df_clean['col4'].isnull().sum()
    assert null_count == 0 or len(df_clean) < len(df_with_nulls)


# ========================================
# TEST: fix_data_types
# ========================================

def test_fix_data_types_converts_numeric(df_with_nulls):
    """Test that fix_data_types converts columns to correct numeric types."""
    df_typed = fix_data_types(df_with_nulls)
    
    # Check numeric columns
    assert pd.api.types.is_numeric_dtype(df_typed['col1'])
    assert pd.api.types.is_numeric_dtype(df_typed['col2'])


def test_fix_data_types_converts_datetime(df_with_nulls):
    """Test that fix_data_types converts date columns to datetime."""
    df_typed = fix_data_types(df_with_nulls)
    
    # Check datetime column
    assert pd.api.types.is_datetime64_any_dtype(df_typed['col4'])


def test_fix_data_types_converts_string(df_with_nulls):
    """Test that fix_data_types converts string columns."""
    df_typed = fix_data_types(df_with_nulls)
    
    # Check string column
    assert pd.api.types.is_string_dtype(df_typed['col3']) or pd.api.types.is_object_dtype(df_typed['col3'])


# ========================================
# TEST: calculate_metrics
# ========================================

def test_calculate_metrics_calculates_revenue(sample_orders_df):
    """Test that calculate_metrics correctly calculates revenue."""
    df_metrics = calculate_metrics(sample_orders_df)
    
    # Check revenue column exists
    assert 'revenue' in df_metrics.columns
    
    # Check revenue calculation (quantity * unit_price)
    assert df_metrics.loc[0, 'revenue'] == 100.0  # 2 * 50
    assert df_metrics.loc[1, 'revenue'] == 200.0  # 1 * 200
    assert df_metrics.loc[2, 'revenue'] == 150.0  # 3 * 50


def test_calculate_metrics_adds_date_components(sample_orders_df):
    """Test that calculate_metrics adds date component columns."""
    df_metrics = calculate_metrics(sample_orders_df)
    
    # Check that date components are added
    assert 'create_date_year' in df_metrics.columns
    assert 'create_date_month' in df_metrics.columns
    assert 'create_date_day' in df_metrics.columns
    assert 'create_date_hour' in df_metrics.columns
    
    # Check date component values
    assert df_metrics.loc[0, 'create_date_year'] == 2024
    assert df_metrics.loc[0, 'create_date_month'] == 4
    assert df_metrics.loc[0, 'create_date_day'] == 1


# ========================================
# TEST: remove_duplicates
# ========================================

def test_remove_duplicates_works(df_with_duplicates):
    """Test that remove_duplicates removes duplicate rows."""
    df_deduped = remove_duplicates(df_with_duplicates, 'id')
    
    # Check that duplicates are removed
    assert len(df_deduped) == 4  # Original had 6, 2 duplicates removed
    
    # Check that unique IDs remain
    assert df_deduped['id'].nunique() == len(df_deduped)


def test_remove_duplicates_keeps_first(df_with_duplicates):
    """Test that remove_duplicates keeps the first occurrence."""
    df_deduped = remove_duplicates(df_with_duplicates, 'id')
    
    # Check that first occurrence is kept
    assert df_deduped[df_deduped['id'] == 1].iloc[0]['value'] == 10
    assert df_deduped[df_deduped['id'] == 2].iloc[0]['value'] == 20


# ========================================
# TEST: build_dim_date
# ========================================

def test_date_dimension_complete():
    """Test that build_dim_date generates complete date dimension."""
    start_date = date(2024, 4, 1)
    end_date = date(2024, 4, 5)
    
    df_dim_date = build_dim_date(start_date, end_date)
    
    # Check that all dates are included
    assert len(df_dim_date) == 5  # 5 days
    
    # Check required columns exist
    required_cols = ['date_key', 'full_date', 'day', 'month', 'year', 'quarter', 'weekday_name', 'is_weekend']
    for col in required_cols:
        assert col in df_dim_date.columns
    
    # Check date_key format (YYYYMMDD)
    assert df_dim_date.loc[0, 'date_key'] == 20240401
    assert df_dim_date.loc[4, 'date_key'] == 20240405


def test_date_dimension_weekend_flag():
    """Test that build_dim_date correctly flags weekends."""
    # April 1, 2024 is a Monday
    start_date = date(2024, 4, 1)
    end_date = date(2024, 4, 7)  # Sunday
    
    df_dim_date = build_dim_date(start_date, end_date)
    
    # Check weekend flags
    # Monday (April 1) should not be weekend
    assert df_dim_date.loc[0, 'is_weekend'] == 0
    # Sunday (April 7) should be weekend
    assert df_dim_date.loc[6, 'is_weekend'] == 1


def test_date_dimension_quarter_calculation():
    """Test that build_dim_date correctly calculates quarters."""
    start_date = date(2024, 1, 1)
    end_date = date(2024, 12, 31)
    
    df_dim_date = build_dim_date(start_date, end_date)
    
    # Check Q1 (Jan-Mar)
    assert df_dim_date.loc[0, 'quarter'] == 1  # January
    assert df_dim_date.loc[2, 'quarter'] == 1  # March
    
    # Find April row by filtering
    april_row = df_dim_date[df_dim_date['full_date'] == date(2024, 4, 1)].iloc[0]
    assert april_row['quarter'] == 2  # April should be Q2


# ========================================
# TEST: validate_revenue
# ========================================

def test_negative_revenue_flagged():
    """Test that validate_revenue flags negative revenue values."""
    df = pd.DataFrame({
        'revenue': [100.0, -50.0, 200.0, 0.0, 150.0]
    })
    
    df_validated = validate_revenue(df)
    
    # Check that validation flags exist
    assert 'negative_revenue' in df_validated.columns
    assert 'zero_revenue' in df_validated.columns
    assert 'invalid_revenue' in df_validated.columns
    
    # Check that negative revenue is flagged
    assert df_validated.loc[1, 'negative_revenue'] == True
    
    # Check that zero revenue is flagged
    assert df_validated.loc[3, 'zero_revenue'] == True


def test_revenue_calculation_correct(sample_orders_df):
    """Test that revenue calculation is correct."""
    # Calculate revenue manually
    sample_orders_df['revenue'] = sample_orders_df['quantity'] * sample_orders_df['unit_price']
    
    # Validate
    df_validated = validate_revenue(sample_orders_df)
    
    # Check that all revenues are positive
    assert df_validated['negative_revenue'].sum() == 0


# ========================================
# TEST: transform_orders
# ========================================

def test_transform_orders_basic(sample_orders_df, sample_products_df, sample_customers_df):
    """Test basic order transformation."""
    df_transformed = transform_orders(sample_orders_df, sample_products_df, sample_customers_df)
    
    # Check that transformation succeeded
    assert not df_transformed.empty
    assert len(df_transformed) == len(sample_orders_df)
    
    # Check that required columns exist
    assert 'revenue' in df_transformed.columns
    assert 'date_key' in df_transformed.columns
    assert 'order_date' in df_transformed.columns
    assert 'order_time' in df_transformed.columns


def test_transform_orders_revenue_calculation(sample_orders_df, sample_products_df, sample_customers_df):
    """Test that transform_orders correctly calculates revenue."""
    df_transformed = transform_orders(sample_orders_df, sample_products_df, sample_customers_df)
    
    # Check revenue calculation
    assert df_transformed.loc[0, 'revenue'] == 100.0  # 2 * 50
    assert df_transformed.loc[1, 'revenue'] == 200.0  # 1 * 200


def test_transform_orders_date_key_format(sample_orders_df, sample_products_df, sample_customers_df):
    """Test that transform_orders correctly formats date_key."""
    df_transformed = transform_orders(sample_orders_df, sample_products_df, sample_customers_df)
    
    # Check date_key format (YYYYMMDD)
    assert df_transformed.loc[0, 'date_key'] == 20240401
    assert df_transformed.loc[1, 'date_key'] == 20240402


# ========================================
# TEST: transform_dim_tables
# ========================================

def test_transform_dim_tables_products(sample_products_df, sample_customers_df, sample_categories_df):
    """Test that transform_dim_tables correctly transforms products."""
    dim_data = transform_dim_tables(sample_products_df, sample_customers_df, sample_categories_df)
    
    # Check that products are transformed
    assert 'products' in dim_data
    assert not dim_data['products'].empty
    
    # Check that category_name is added
    assert 'category_name' in dim_data['products'].columns


def test_transform_dim_tables_customers(sample_products_df, sample_customers_df, sample_categories_df):
    """Test that transform_dim_tables correctly transforms customers."""
    dim_data = transform_dim_tables(sample_products_df, sample_customers_df, sample_categories_df)
    
    # Check that customers are transformed
    assert 'customers' in dim_data
    assert not dim_data['customers'].empty
    
    # Check that join_year and join_month are added
    assert 'join_year' in dim_data['customers'].columns
    assert 'join_month' in dim_data['customers'].columns


def test_transform_dim_tables_categories(sample_products_df, sample_customers_df, sample_categories_df):
    """Test that transform_dim_tables correctly transforms categories."""
    dim_data = transform_dim_tables(sample_products_df, sample_customers_df, sample_categories_df)
    
    # Check that categories are transformed
    assert 'categories' in dim_data
    assert not dim_data['categories'].empty


# ========================================
# TEST: Edge Cases
# ========================================

def test_clean_nulls_empty_dataframe():
    """Test clean_nulls with empty DataFrame."""
    df_empty = pd.DataFrame()
    df_clean = clean_nulls(df_empty)
    
    assert df_empty.equals(df_clean)


def test_remove_duplicates_empty_dataframe():
    """Test remove_duplicates with empty DataFrame."""
    df_empty = pd.DataFrame()
    df_deduped = remove_duplicates(df_empty, 'id')
    
    assert df_empty.equals(df_deduped)


def test_build_dim_date_single_day():
    """Test build_dim_date with single day range."""
    start_date = date(2024, 4, 1)
    end_date = date(2024, 4, 1)
    
    df_dim_date = build_dim_date(start_date, end_date)
    
    assert len(df_dim_date) == 1
    assert df_dim_date.loc[0, 'date_key'] == 20240401


def test_transform_orders_empty_dataframe():
    """Test transform_orders with empty DataFrame."""
    df_empty = pd.DataFrame()
    df_products = pd.DataFrame()
    df_customers = pd.DataFrame()
    
    df_transformed = transform_orders(df_empty, df_products, df_customers)
    
    assert df_transformed.empty


def test_validate_revenue_no_revenue_column():
    """Test validate_revenue when revenue column doesn't exist."""
    df = pd.DataFrame({'col1': [1, 2, 3]})
    
    df_validated = validate_revenue(df)
    
    # When revenue column doesn't exist, function returns DataFrame unchanged
    # without adding validation columns
    assert 'negative_revenue' not in df_validated.columns
    assert list(df_validated.columns) == ['col1']


# ========================================
# TEST RUNNER
# ========================================

if __name__ == '__main__':
    pytest.main([__file__, '-v'])
