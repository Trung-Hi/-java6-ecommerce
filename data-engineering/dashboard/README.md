# 📊 Metabase Dashboard Setup

This folder contains Metabase dashboard exports and setup instructions for the E-Commerce Data Platform.

## Setup Instructions

1. **Install Metabase**
   ```bash
   # Using Docker
   docker run -d -p 3000:3000 \
     -e "MB_DB_TYPE=postgres" \
     -e "MB_DB_DBNAME=metabase" \
     -e "MB_DB_PORT=5432" \
     -e "MB_DB_USER=metabase" \
     -e "MB_DB_PASS=password" \
     -e "MB_DB_HOST=postgres" \
     metabase/metabase
   ```

2. **Connect to SQL Server**
   - Open Metabase at http://localhost:3000
   - Navigate to Admin → Databases → Add database
   - Select Microsoft SQL Server
   - Configure connection:
     - Host: localhost
     - Port: 1433
     - Database: ASM_Java5
     - Username: sa
     - Password: YourPassword123

3. **Import Dashboard**
   - Navigate to Admin → Data model
   - Import dashboard JSON files from this folder

## Available Dashboards

- `monthly_revenue.json` - Monthly revenue trend analysis
- `product_performance.json` - Product performance metrics
- `customer_analysis.json` - Customer retention and behavior

## Dashboard Refresh

Dashboards are automatically refreshed when the ETL pipeline runs daily at midnight.

## Questions?

Contact Data Engineering Team
