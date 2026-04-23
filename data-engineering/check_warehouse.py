#!/usr/bin/env python3
"""Check warehouse data table row counts"""

import pyodbc
from config.constants import SOURCE_CONNECTION_STRING, WAREHOUSE_SCHEMA

conn = pyodbc.connect(SOURCE_CONNECTION_STRING)
cursor = conn.cursor()

query = f"""
SELECT 'dim_date' as tbl, COUNT(*) as rows FROM {WAREHOUSE_SCHEMA}.dim_date
UNION ALL SELECT 'dim_product', COUNT(*) FROM {WAREHOUSE_SCHEMA}.dim_product
UNION ALL SELECT 'dim_customer', COUNT(*) FROM {WAREHOUSE_SCHEMA}.dim_customer
UNION ALL SELECT 'dim_category', COUNT(*) FROM {WAREHOUSE_SCHEMA}.dim_category
UNION ALL SELECT 'fact_orders', COUNT(*) FROM {WAREHOUSE_SCHEMA}.fact_orders
"""

cursor.execute(query)
results = cursor.fetchall()

print("Warehouse Data Table Row Counts:")
print("=" * 50)
for row in results:
    print(f"{row[0]:20} {row[1]:10} rows")
print("=" * 50)

conn.close()
