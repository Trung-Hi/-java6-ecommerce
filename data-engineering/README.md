# 🛒 E-Commerce Data Platform v2.0

![Python](https://img.shields.io/badge/Python-3.8%2B-blue)
![SQL Server](https://img.shields.io/badge/SQL%20Server-2019%2B-red)
![Apache Airflow](https://img.shields.io/badge/Apache%20Airflow-2.7%2B-yellow)
![Azure](https://img.shields.io/badge/Azure-Blob%20Storage-blue)
![Metabase](https://img.shields.io/badge/Metabase-BI-green)
![License](https://img.shields.io/badge/License-MIT-yellow)

**Production-grade data platform for e-commerce analytics — transforming transactional data into actionable business insights through automated ETL pipelines, data quality checks, Azure backup, and real-time monitoring.**

---

## 📋 Overview

This project builds a complete Data Engineering platform on top of an existing E-Commerce application, enabling data-driven decision making through:

- **Automated ETL Pipeline** — Apache Airflow-orchestrated data extraction, transformation, and loading
- **Data Quality Checks** — Custom validation with threshold-based alerts
- **Star Schema Data Warehouse** — Optimized for analytical queries with proper dimension and fact tables
- **Azure Blob Storage** — Partitioned backup and data lake with year/month/day structure
- **Email Alerting** — Real-time notifications for success, failure, and SLA warnings
- **Business Intelligence** — Pre-built analytical queries for revenue, retention, and product performance
- **Scalable Architecture** — Production-ready code with comprehensive error handling and logging

---

## 🏗️ Architecture

```mermaid
graph TD
    A[E-Commerce SQL Server<br/>ASM_Java5] -->|Extract| B[Apache Airflow<br/>DAG Orchestrator]
    B --> C[Quality Checks<br/>Custom Validation]
    C -->|Pass| D[Transform<br/>Clean & Calculate Metrics]
    C -->|Fail| E[Alerting<br/>Email Notification]
    D --> F[Data Warehouse<br/>Star Schema]
    D --> G[Azure Blob Storage<br/>Partitioned Backup]
    F --> H[Analytical Queries<br/>SQL Server]
    F --> I[Metabase Dashboard<br/>Visualization]
    B --> J[Monitoring<br/>Email Alerts]
    
    style A fill:#e1f5ff
    style B fill:#fff4e1
    style C fill:#ffe1e1
    style D fill:#e8f5e9
    style E fill:#ffe1e1
    style F fill:#f3e5f5
    style G fill:#e1f5ff
    style H fill:#fce4ec
    style I fill:#fce4ec
    style J fill:#fff9c4
```

### Data Flow

1. **Source Database** — Transactional tables (orders, order_details, products, accounts, categories)
2. **Apache Airflow** — Orchestrates pipeline with 6 tasks (extract, quality, transform, load, backup, notify)
3. **Data Quality** — Custom validation checks (nulls, duplicates, revenue, row count, date range)
4. **Transformation** — Clean nulls, fix data types, calculate metrics, build date dimension
5. **Data Warehouse** — Star Schema with dimension and fact tables in ecommerce_warehouse schema
6. **Azure Backup** — Partitioned storage with year/month/day structure for efficient querying
7. **Analytics** — SQL queries for business insights
8. **Monitoring** — Email alerts for success, failure, quality issues, SLA warnings
9. **Dashboard** — Metabase visualization (optional)

---

## 🛠️ Tech Stack

| Component | Technology | Purpose |
|-----------|------------|---------|
| **Orchestration** | Apache Airflow 2.7+ | Pipeline scheduling and task management |
| **Database** | Microsoft SQL Server 2019+ | Source & Warehouse |
| **Python** | Python 3.8+ | Data processing and ETL |
| **Libraries** | pyodbc, pandas, numpy | ETL operations |
| **Cloud Storage** | Azure Blob Storage | Backup and data lake |
| **Monitoring** | smtplib | Email alerting |
| **BI Tool** | Metabase (optional) | Data visualization |
| **Quality** | Custom validation | Data quality checks |

---

## 📊 Star Schema Design

```
┌─────────────────────────────────────────────────────────────────┐
│                    FACT TABLES                                   │
│  ┌─────────────────────────────────────────────────────────┐   │
│  │                    fact_orders                            │   │
│  │  ┌──────────────┐  ┌──────────────┐  ┌──────────────┐  │   │
│  │  │ order_id PK │  │  quantity    │  │  revenue      │  │   │
│  │  └──────────────┘  └──────────────┘  └──────────────┘  │   │
│  │  ┌──────────────┐  ┌──────────────┐  ┌──────────────┐  │   │
│  │  │ product_key  │  │ customer_key │  │  date_key    │  │   │
│  │  └──────┬───────┘  └──────┬───────┘  └──────┬───────┘  │   │
│  └─────────┼──────────────────┼──────────────────┼──────────┘   │
└────────────┼──────────────────┼──────────────────┼──────────────┘
             │                  │                  │
             ▼                  ▼                  ▼
┌─────────────────────────────────────────────────────────────────┐
│                  DIMENSION TABLES                                │
│  ┌──────────────┐  ┌──────────────┐  ┌──────────────┐          │
│  │ dim_product  │  │ dim_customer │  │  dim_date    │          │
│  │              │  │              │  │              │          │
│  │ product_key  │  │ customer_key │  │  date_key    │          │
│  │ product_id   │  │ username     │  │  full_date   │          │
│  │ name         │  │ fullname     │  │  day         │          │
│  │ category_id  │  │ email        │  │  month       │          │
│  │ category_name│  │ phone        │  │  year        │          │
│  │ price        │  │ address      │  │  quarter     │          │
│  └──────────────┘  └──────────────┘  │  weekday_name│          │
│                                    │  is_weekend  │          │
│  ┌──────────────┐                  └──────────────┘          │
│  │ dim_category │                                            │
│  │              │                                            │
│  │ category_key │                                            │
│  │ category_id  │                                            │
│  │ name         │                                            │
│  │ parent_id    │                                            │
│  └──────────────┘                                            │
└─────────────────────────────────────────────────────────────────┘
```

### Schema Tables

**Fact Tables:**
- `fact_orders` — Transactional data (order_id, product_key, customer_key, date_key, size_id, quantity, unit_price, revenue, final_total, status, payment_method, phone, address)

**Dimension Tables:**
- `dim_date` — Time dimension (date_key, full_date, day, month, year, quarter, weekday_name, is_weekend, is_holiday, week_of_year)
- `dim_product` — Product dimension (product_key, product_id, name, image, category_id, category_name, price, discount, stock_quantity)
- `dim_customer` — Customer dimension (customer_key, username, fullname, email, phone, address, join_date, join_year, join_month)
- `dim_category` — Category dimension (category_key, category_id, name, parent_id)

---

## 🚀 Setup & Installation

### Prerequisites

- Python 3.8 or higher
- SQL Server 2019 or higher
- ODBC Driver 17 for SQL Server
- Docker & Docker Compose (for Airflow)
- Azure Storage Account (for backup)
- SMTP server (for email alerts)

### Step 1: Install Python Dependencies

```bash
# Create virtual environment (recommended)
python -m venv venv

# Activate virtual environment
# Windows:
venv\Scripts\activate
# Linux/Mac:
source venv/bin/activate

# Install dependencies
pip install apache-airflow pyodbc pandas numpy azure-storage-blob
```

### Step 2: Install ODBC Driver

**Windows:**
Download and install [ODBC Driver 17 for SQL Server](https://learn.microsoft.com/en-us/sql/connect/odbc/download-odbc-driver-for-sql-server)

**Linux (Ubuntu/Debian):**
```bash
curl https://packages.microsoft.com/keys/microsoft.asc | apt-key add -
curl https://packages.microsoft.com/config/ubuntu/20.04/prod.list > /etc/apt/sources.list.d/mssql-release.list
apt-get update
ACCEPT_EULA=Y apt-get install -y msodbcsql17
```

### Step 3: Setup Apache Airflow (Docker)

Create `docker-compose.yml`:

```yaml
version: '3.8'
services:
  postgres:
    image: postgres:13
    environment:
      POSTGRES_USER: airflow
      POSTGRES_PASSWORD: airflow
      POSTGRES_DB: airflow
    volumes:
      - postgres_data:/var/lib/postgresql/data

  redis:
    image: redis:latest

  airflow-webserver:
    image: apache/airflow:2.7.0-python3.9
    depends_on:
      - postgres
      - redis
    environment:
      AIRFLOW__CORE__EXECUTOR: CeleryExecutor
      AIRFLOW__CORE__SQL_ALCHEMY_CONN: postgresql+psycopg2://airflow:airflow@postgres/airflow
      AIRFLOW__CELERY__BROKER_URL: redis://redis:6379/0
      AIRFLOW__CELERY__RESULT_BACKEND: redis://redis:6379/0
    volumes:
      - ./dags:/opt/airflow/dags
      - ./etl:/opt/airflow/etl
      - ./monitoring:/opt/airflow/monitoring
      - ./sql:/opt/airflow/sql
    ports:
      - "8080:8080"
    command: webserver

  airflow-scheduler:
    image: apache/airflow:2.7.0-python3.9
    depends_on:
      - postgres
      - redis
    environment:
      AIRFLOW__CORE__EXECUTOR: CeleryExecutor
      AIRFLOW__CORE__SQL_ALCHEMY_CONN: postgresql+psycopg2://airflow:airflow@postgres/airflow
      AIRFLOW__CELERY__BROKER_URL: redis://redis:6379/0
      AIRFLOW__CELERY__RESULT_BACKEND: redis://redis:6379/0
    volumes:
      - ./dags:/opt/airflow/dags
      - ./etl:/opt/airflow/etl
      - ./monitoring:/opt/airflow/monitoring
      - ./sql:/opt/airflow/sql
    command: scheduler

  airflow-worker:
    image: apache/airflow:2.7.0-python3.9
    depends_on:
      - postgres
      - redis
    environment:
      AIRFLOW__CORE__EXECUTOR: CeleryExecutor
      AIRFLOW__CORE__SQL_ALCHEMY_CONN: postgresql+psycopg2://airflow:airflow@postgres/airflow
      AIRFLOW__CELERY__BROKER_URL: redis://redis:6379/0
      AIRFLOW__CELERY__RESULT_BACKEND: redis://redis:6379/0
    volumes:
      - ./dags:/opt/airflow/dags
      - ./etl:/opt/airflow/etl
      - ./monitoring:/opt/airflow/monitoring
      - ./sql:/opt/airflow/sql
    command: celery worker

volumes:
  postgres_data:
```

Start Airflow:
```bash
docker-compose up -d
```

Access Airflow UI: http://localhost:8080

### Step 4: Configure Database Connection

Edit `etl/extract.py` and update the connection string:

```python
SOURCE_CONNECTION_STRING = (
    "DRIVER={ODBC Driver 17 for SQL Server};"
    "SERVER=localhost;"
    "DATABASE=ASM_Java5;"
    "UID=sa;"
    "PWD=YourPassword123;"
    "TrustServerCertificate=yes;"
)
```

Or use environment variables:

```bash
# Windows
set SQL_SERVER_CONNECTION_STRING="DRIVER={ODBC Driver 17 for SQL Server};SERVER=localhost;DATABASE=ASM_Java5;UID=sa;PWD=YourPassword123;TrustServerCertificate=yes;"

# Linux/Mac
export SQL_SERVER_CONNECTION_STRING="DRIVER={ODBC Driver 17 for SQL Server};SERVER=localhost;DATABASE=ASM_Java5;UID=sa;PWD=YourPassword123;TrustServerCertificate=yes;"
```

### Step 5: Configure Azure Blob Storage

Set environment variables:

```bash
export AZURE_STORAGE_CONNECTION_STRING="DefaultEndpointsProtocol=https;AccountName=youraccount;AccountKey=yourkey;EndpointSuffix=core.windows.net"
```

### Step 6: Configure Email Alerts

Set environment variables:

```bash
export SMTP_SERVER="smtp.gmail.com"
export SMTP_PORT="587"
export SMTP_USERNAME="your-email@gmail.com"
export SMTP_PASSWORD="your-app-password"
export EMAIL_FROM="data-engineering@example.com"
export EMAIL_TO="data-team@example.com"
```

### Step 7: Create Warehouse Schema

Run the schema creation script in SQL Server Management Studio (SSMS):

```sql
-- Open sql/warehouse_schema.sql in SSMS
-- Select ASM_Java5 database
-- Execute (F5)
```

Or via command line:

```bash
sqlcmd -S localhost -d ASM_Java5 -U sa -P YourPassword123 -i sql/warehouse_schema.sql
```

---

## 🏃 Running the Pipeline

### Run via Airflow UI

1. Open Airflow UI: http://localhost:8080
2. Navigate to DAGs → ecommerce_etl_pipeline
3. Click "Trigger DAG" to run manually
4. Monitor task execution in Graph View

### Run via Command Line (Manual)

```bash
# Extract data
python etl/extract.py

# Transform data
python etl/transform.py

# Run quality checks
python etl/quality.py

# Backup to Azure
python etl/azure_backup.py
```

---

## ⏰ Pipeline Schedule

The ETL pipeline runs automatically every day at midnight via Apache Airflow:

- **Schedule:** `@daily` (runs at 00:00 UTC)
- **DAG ID:** `ecommerce_etl_pipeline`
- **Catchup:** `False` (no backfill)
- **Retry Policy:** 2 retries with 5-minute delay

### Task Dependencies

```
extract_task
    ↓
quality_check_task
    ↓
transform_task
    ↓
load_warehouse_task
    ↓
backup_azure_task
    ↓
notify_task
```

---

## 🔍 Data Quality Checks

### Quality Check Functions

| Check | Description | Threshold | Critical |
|-------|-------------|-----------|----------|
| `check_nulls` | Null percentage in specified columns | 5% | Yes |
| `check_duplicates` | Duplicate rows based on key column | 0 | Yes |
| `check_revenue_positive` | Revenue values must be positive | > 0 | Yes |
| `check_row_count` | Minimum rows extracted | 100 | Yes |
| `check_date_range` | Dates not too far in future | 1 day | Yes |
| `check_data_types` | Column data types match expectations | - | No |
| `check_referential_integrity` | Foreign key relationships maintained | - | Yes |

### Quality Report

```json
{
  "timestamp": "2024-04-23T10:00:00",
  "total_checks": 6,
  "passed_checks": 6,
  "failed_checks": 0,
  "critical_failures": 0,
  "total_failed_rows": 0,
  "checks": [...]
}
```

If any critical check fails, the pipeline stops and sends a failure alert.

---

## ☁️ Azure Blob Storage Structure

Data is backed up to Azure Blob Storage with partitioning:

```
ecommerce-datalake/
├── raw/
│   ├── orders/
│   │   └── year=2026/
│   │       └── month=04/
│   │           └── day=23/
│   │               └── orders_20260423.csv
│   ├── products/
│   │   └── year=2026/
│   │       └── month=04/
│   │           └── products_20260423.csv
│   ├── customers/
│   │   └── year=2026/
│   │       └── month=04/
│   │           └── customers_20260423.csv
│   └── categories/
│       └── categories.csv
└── warehouse/
    └── fact_orders/
        └── year=2026/
            └── month=04/
                └── day=23/
                    └── fact_orders_20260423.csv
```

### Azure Functions

- `connect_azure_blob()` — Connect to Azure Blob Storage
- `upload_to_blob()` — Upload DataFrame as CSV
- `upload_with_partitioning()` — Upload with year/month/day partitioning
- `backup_all_data()` — Backup all data with proper structure
- `list_blobs()` — List blobs in container
- `download_blob()` — Download blob to local file
- `delete_blob()` — Delete blob from storage

---

## 📧 Monitoring & Alerting

### Alert Types

| Alert Type | Trigger | Recipients |
|------------|---------|------------|
| Success Alert | Pipeline completes successfully | Data Team |
| Failure Alert | Any task fails | Data Team |
| Quality Alert | Critical quality check fails | Data Team |
| SLA Warning | Pipeline exceeds duration threshold | Data Team |
| Daily Report | End of day summary | Data Team |

### Email Configuration

Configure SMTP settings in environment variables or `monitoring/alerting.py`:

```python
SMTP_SERVER = os.getenv('SMTP_SERVER', 'smtp.gmail.com')
SMTP_PORT = int(os.getenv('SMTP_PORT', '587'))
SMTP_USERNAME = os.getenv('SMTP_USERNAME', '')
SMTP_PASSWORD = os.getenv('SMTP_PASSWORD', '')
EMAIL_FROM = os.getenv('EMAIL_FROM', 'data-engineering@example.com')
EMAIL_TO = os.getenv('EMAIL_TO', 'data-team@example.com').split(',')
```

---

## 📈 Analytical Queries

Run the analytical queries in `sql/analytics_queries.sql` using SSMS or any SQL client.

### Available Queries

1. **Monthly Revenue Trend (Last 12 Months)**
   - Revenue, orders, customers by month
   - Month-over-month growth rate
   - Average order value

2. **Top 10 Best-Selling Products**
   - Product performance metrics
   - Revenue contribution percentage
   - Ranking by revenue

3. **Revenue by Category with MoM Growth**
   - Category performance over time
   - Month-over-month growth by category

4. **Customer Retention Rate**
   - Percentage of returning customers
   - One-time vs repeat customer analysis
   - Inactive customer tracking

5. **Peak Ordering Hours**
   - Order distribution by hour of day
   - Revenue by time period
   - Alternative: Day of week analysis

6. **Order Completion vs Cancellation Rate**
   - Order status breakdown by month
   - Completion and cancellation rates
   - Pending order tracking

### Example Query Execution

```sql
-- Open sql/analytics_queries.sql in SSMS
-- Select ASM_Java5 database
-- Execute desired query (F5)
```

---

## 🧪 Unit Tests

Run unit tests using pytest:

```bash
# Install pytest
pip install pytest

# Run all tests
pytest tests/

# Run specific test file
pytest tests/test_transform.py

# Run with coverage
pytest tests/ --cov=etl --cov-report=html
```

### Test Coverage

- `test_clean_nulls_removes_nulls()` — Test null cleaning logic
- `test_remove_duplicates_works()` — Test duplicate removal
- `test_revenue_calculation_correct()` — Test revenue calculation
- `test_negative_revenue_flagged()` — Test negative revenue detection
- `test_date_dimension_complete()` — Test date dimension generation

---

## 📁 Folder Structure

```
data-engineering/
├── dags/                          → Airflow DAGs
│   └── etl_dag.py                # Main ETL DAG
├── etl/                           → ETL modules
│   ├── extract.py                 # Data extraction
│   ├── transform.py               # Data transformation
│   ├── quality.py                 # Data quality checks
│   └── azure_backup.py            # Azure Blob Storage
├── sql/                           → SQL scripts
│   ├── warehouse_schema.sql       # Warehouse schema
│   └── analytics_queries.sql     # Analytical queries
├── monitoring/                    → Monitoring & alerting
│   └── alerting.py                # Email alerting
├── tests/                         → Unit tests
│   └── test_transform.py          # Transform tests
├── dashboard/                     → Metabase exports
│   ├── monthly_revenue.json
│   ├── product_performance.json
│   └── customer_analysis.json
├── docs/                          → Documentation
│   ├── architecture.md
│   ├── data_dictionary.md
│   └── deployment_guide.md
├── docker-compose.yml             # Airflow Docker setup
├── requirements.txt                # Python dependencies
└── README_v2.md                   # This file
```

---

## 🐛 Troubleshooting

### Common Issues

**1. Airflow DAG not appearing**
- Ensure DAG file is in `dags/` folder
- Check Airflow scheduler is running
- Restart Airflow webserver: `docker-compose restart airflow-webserver`

**2. SQL Server connection error**
- Verify ODBC Driver 17 is installed
- Check connection string credentials
- Ensure SQL Server allows remote connections

**3. Azure upload fails**
- Verify AZURE_STORAGE_CONNECTION_STRING is set
- Check container exists or can be created
- Ensure sufficient storage quota

**4. Email not sending**
- Verify SMTP credentials are correct
- Check if 2FA is enabled (use app password)
- Ensure SMTP server allows relaying

**5. Quality checks failing**
- Review quality report in logs
- Check data source for data quality issues
- Adjust thresholds in config if needed

---

## 📚 References

- [Apache Airflow Documentation](https://airflow.apache.org/docs/)
- [Azure Blob Storage Documentation](https://docs.microsoft.com/en-us/azure/storage/blobs/)
- [pyodbc Documentation](https://github.com/mkleehammer/pyodbc)
- [Pandas Documentation](https://pandas.pydata.org/docs/)
- [SQL Server Documentation](https://docs.microsoft.com/en-us/sql/)
- [Star Schema Design](https://www.kimballgroup.com/data-warehouse-business-intelligence-resources/kimball-techniques/dimensional-modeling-techniques/)

---

## 👥 Authors

Data Engineering Team - 2024

---

## 📄 License

MIT License - feel free to use this project for learning and development.

---

## 🤝 Contributing

Contributions are welcome! Please feel free to submit a Pull Request.

---

## 📞 Support

For questions or issues:
- Open an issue on GitHub
- Email: data-engineering@example.com

---

**Built with ❤️ for data-driven decision making at top tech companies**
