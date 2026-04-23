#!/usr/bin/env python3
"""
E-Commerce Data Platform v2.0 - Monitoring & Alerting Module
============================================================
Email alerting system for pipeline notifications.
Uses smtplib for email sending.

Author: Data Engineering Team
Date: 2024
"""

import smtplib
from email.mime.text import MIMEText
from email.mime.multipart import MIMEMultipart
from datetime import datetime
import logging
import os

logger = logging.getLogger(__name__)

# ========================================
# EMAIL CONFIGURATION
# ========================================
SMTP_SERVER = os.getenv('SMTP_SERVER', 'smtp.gmail.com')
SMTP_PORT = int(os.getenv('SMTP_PORT', '587'))
SMTP_USERNAME = os.getenv('SMTP_USERNAME', '')
SMTP_PASSWORD = os.getenv('SMTP_PASSWORD', '')
EMAIL_FROM = os.getenv('EMAIL_FROM', 'data-engineering@example.com')
EMAIL_TO = os.getenv('EMAIL_TO', 'data-team@example.com').split(',')


def send_email(subject, body, is_html=False):
    """
    Send email using SMTP.
    
    Args:
        subject (str): Email subject
        body (str): Email body
        is_html (bool): Whether body is HTML format
    """
    logger.info(f"📧 Sending email: {subject}")
    
    try:
        # Create message
        msg = MIMEMultipart('alternative')
        msg['Subject'] = subject
        msg['From'] = EMAIL_FROM
        msg['To'] = ', '.join(EMAIL_TO)
        
        # Attach body
        if is_html:
            msg.attach(MIMEText(body, 'html'))
        else:
            msg.attach(MIMEText(body, 'plain'))
        
        # Connect to SMTP server and send
        with smtplib.SMTP(SMTP_SERVER, SMTP_PORT) as server:
            server.starttls()
            server.login(SMTP_USERNAME, SMTP_PASSWORD)
            server.send_message(msg)
        
        logger.info(f"   ✅ Email sent successfully to {EMAIL_TO}")
        
    except Exception as e:
        logger.error(f"❌ Error sending email: {e}")
        raise


def send_success_alert(pipeline_name, rows_loaded, duration):
    """
    Send success alert when pipeline completes successfully.
    
    Args:
        pipeline_name (str): Name of the pipeline
        rows_loaded (int): Number of rows loaded
        duration (float): Pipeline duration in seconds
    """
    logger.info(f"✅ Preparing success alert for {pipeline_name}")
    
    subject = f"✅ SUCCESS: {pipeline_name} Completed"
    
    body = f"""
    <html>
    <body>
        <h2>✅ Pipeline Success</h2>
        <p><strong>Pipeline:</strong> {pipeline_name}</p>
        <p><strong>Status:</strong> Completed Successfully</p>
        <p><strong>Timestamp:</strong> {datetime.now().strftime('%Y-%m-%d %H:%M:%S')}</p>
        
        <h3>Summary:</h3>
        <ul>
            <li>Rows Loaded: <strong>{rows_loaded:,}</strong></li>
            <li>Duration: <strong>{duration:.2f} seconds</strong></li>
            <li>Throughput: <strong>{rows_loaded/duration:.2f} rows/sec</strong></li>
        </ul>
        
        <p>The pipeline has completed successfully. Data is available in the warehouse.</p>
        
        <hr>
        <p><small>This is an automated message from E-Commerce Data Platform v2.0</small></p>
    </body>
    </html>
    """
    
    send_email(subject, body, is_html=True)


def send_failure_alert(pipeline_name, error_message, failed_task):
    """
    Send failure alert when pipeline task fails.
    
    Args:
        pipeline_name (str): Name of the pipeline
        error_message (str): Error message
        failed_task (str): Name of the failed task
    """
    logger.info(f"❌ Preparing failure alert for {pipeline_name}")
    
    subject = f"❌ FAILURE: {pipeline_name} Failed"
    
    body = f"""
    <html>
    <body>
        <h2>❌ Pipeline Failure</h2>
        <p><strong>Pipeline:</strong> {pipeline_name}</p>
        <p><strong>Status:</strong> FAILED</p>
        <p><strong>Timestamp:</strong> {datetime.now().strftime('%Y-%m-%d %H:%M:%S')}</p>
        
        <h3>Failure Details:</h3>
        <ul>
            <li>Failed Task: <strong>{failed_task}</strong></li>
            <li>Error Message: <strong>{error_message}</strong></li>
        </ul>
        
        <p>Please investigate the failure immediately. Check Airflow logs for more details.</p>
        
        <hr>
        <p><small>This is an automated message from E-Commerce Data Platform v2.0</small></p>
    </body>
    </html>
    """
    
    send_email(subject, body, is_html=True)


def send_quality_alert(pipeline_name, quality_report):
    """
    Send alert when data quality checks fail.
    
    Args:
        pipeline_name (str): Name of the pipeline
        quality_report (dict): Quality report from checks
    """
    logger.info(f"⚠️ Preparing quality alert for {pipeline_name}")
    
    subject = f"⚠️ QUALITY ALERT: {pipeline_name} Quality Issues"
    
    failed_checks = [c for c in quality_report['checks'] if not c['status']]
    
    checks_html = ""
    for check in failed_checks:
        checks_html += f"<li><strong>{check['check_name']}</strong>: {check['message']}</li>"
    
    body = f"""
    <html>
    <body>
        <h2>⚠️ Data Quality Alert</h2>
        <p><strong>Pipeline:</strong> {pipeline_name}</p>
        <p><strong>Status:</strong> Quality Checks Failed</p>
        <p><strong>Timestamp:</strong> {datetime.now().strftime('%Y-%m-%d %H:%M:%S')}</p>
        
        <h3>Quality Summary:</h3>
        <ul>
            <li>Total Checks: <strong>{quality_report['total_checks']}</strong></li>
            <li>Passed: <strong>{quality_report['passed_checks']}</strong></li>
            <li>Failed: <strong>{quality_report['failed_checks']}</strong></li>
            <li>Critical Failures: <strong>{quality_report['critical_failures']}</strong></li>
            <li>Total Failed Rows: <strong>{quality_report['total_failed_rows']}</strong></li>
        </ul>
        
        <h3>Failed Checks:</h3>
        <ul>
            {checks_html}
        </ul>
        
        <p>Please review the data quality issues. Pipeline may have been stopped.</p>
        
        <hr>
        <p><small>This is an automated message from E-Commerce Data Platform v2.0</small></p>
    </body>
    </html>
    """
    
    send_email(subject, body, is_html=True)


def generate_pipeline_report(rows_processed, quality_report, duration, next_run):
    """
    Generate daily pipeline summary report.
    
    Args:
        rows_processed (int): Number of rows processed
        quality_report (dict): Quality check results
        duration (float): Pipeline duration in seconds
        next_run (datetime): Next scheduled run time
        
    Returns:
        dict: Pipeline report summary
    """
    logger.info("📊 Generating pipeline report...")
    
    report = {
        'timestamp': datetime.now().isoformat(),
        'rows_processed': rows_processed,
        'duration_seconds': duration,
        'duration_minutes': duration / 60,
        'throughput_rows_per_sec': rows_processed / duration if duration > 0 else 0,
        'quality_report': quality_report,
        'next_run': next_run.isoformat() if next_run else None,
        'status': 'SUCCESS' if quality_report['critical_failures'] == 0 else 'FAILED'
    }
    
    logger.info(f"   Rows processed: {rows_processed:,}")
    logger.info(f"   Duration: {duration:.2f}s ({report['duration_minutes']:.2f}min)")
    logger.info(f"   Throughput: {report['throughput_rows_per_sec']:.2f} rows/sec")
    logger.info(f"   Status: {report['status']}")
    
    return report


def send_daily_report(pipeline_report):
    """
    Send daily pipeline summary report.
    
    Args:
        pipeline_report (dict): Pipeline report from generate_pipeline_report
    """
    logger.info("📧 Sending daily pipeline report...")
    
    subject = f"📊 Daily Report: E-Commerce ETL Pipeline - {datetime.now().strftime('%Y-%m-%d')}"
    
    quality_status = "✅ All Checks Passed" if pipeline_report['status'] == 'SUCCESS' else "❌ Quality Issues Found"
    
    quality_checks_html = ""
    for check in pipeline_report['quality_report']['checks']:
        status_icon = "✅" if check['status'] else "❌"
        quality_checks_html += f"<li>{status_icon} <strong>{check['check_name']}</strong>: {check['message']}</li>"
    
    body = f"""
    <html>
    <body>
        <h2>📊 Daily Pipeline Report</h2>
        <p><strong>Date:</strong> {datetime.now().strftime('%Y-%m-%d')}</p>
        <p><strong>Status:</strong> {quality_status}</p>
        
        <h3>Performance Metrics:</h3>
        <ul>
            <li>Rows Processed: <strong>{pipeline_report['rows_processed']:,}</strong></li>
            <li>Duration: <strong>{pipeline_report['duration_minutes']:.2f} minutes</strong></li>
            <li>Throughput: <strong>{pipeline_report['throughput_rows_per_sec']:.2f} rows/sec</strong></li>
        </ul>
        
        <h3>Data Quality:</h3>
        <ul>
            <li>Total Checks: <strong>{pipeline_report['quality_report']['total_checks']}</strong></li>
            <li>Passed: <strong>{pipeline_report['quality_report']['passed_checks']}</strong></li>
            <li>Failed: <strong>{pipeline_report['quality_report']['failed_checks']}</strong></li>
            <li>Critical Failures: <strong>{pipeline_report['quality_report']['critical_failures']}</strong></li>
        </ul>
        
        <h3>Quality Check Details:</h3>
        <ul>
            {quality_checks_html}
        </ul>
        
        <h3>Schedule:</h3>
        <ul>
            <li>Next Run: <strong>{pipeline_report['next_run']}</strong></li>
        </ul>
        
        <hr>
        <p><small>This is an automated daily report from E-Commerce Data Platform v2.0</small></p>
    </body>
    </html>
    """
    
    send_email(subject, body, is_html=True)


def send_sla_warning(pipeline_name, sla_threshold, actual_duration):
    """
    Send SLA warning when pipeline exceeds threshold.
    
    Args:
        pipeline_name (str): Name of the pipeline
        sla_threshold (float): SLA threshold in seconds
        actual_duration (float): Actual duration in seconds
    """
    logger.info(f"⏱️ SLA Warning: {pipeline_name} exceeded threshold")
    
    subject = f"⏱️ SLA Warning: {pipeline_name} Exceeded Threshold"
    
    body = f"""
    <html>
    <body>
        <h2>⏱️ SLA Warning</h2>
        <p><strong>Pipeline:</strong> {pipeline_name}</p>
        <p><strong>Timestamp:</strong> {datetime.now().strftime('%Y-%m-%d %H:%M:%S')}</p>
        
        <h3>SLA Details:</h3>
        <ul>
            <li>SLA Threshold: <strong>{sla_threshold:.2f} seconds</strong></li>
            <li>Actual Duration: <strong>{actual_duration:.2f} seconds</strong></li>
            <li>Exceeded By: <strong>{actual_duration - sla_threshold:.2f} seconds</strong></li>
        </ul>
        
        <p>The pipeline has exceeded the SLA threshold. Please investigate performance issues.</p>
        
        <hr>
        <p><small>This is an automated message from E-Commerce Data Platform v2.0</small></p>
    </body>
    </html>
    """
    
    send_email(subject, body, is_html=True)


# ========================================
# MAIN EXECUTION
# ========================================
if __name__ == "__main__":
    # Configure logging
    logging.basicConfig(
        level=logging.INFO,
        format='%(asctime)s - %(levelname)s - %(message)s'
    )
    
    # Test email sending
    try:
        print("Alerting module loaded successfully")
        print("To test email sending, configure SMTP credentials in environment variables")
        
    except Exception as e:
        print(f"Error: {e}")
