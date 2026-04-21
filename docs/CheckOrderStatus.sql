-- Check status of specific orders
SELECT id, status, created_date, total_amount 
FROM orders 
WHERE id IN (20065, 20113);

-- Check all orders with their current status
SELECT id, status, created_date, total_amount 
FROM orders 
ORDER BY created_date DESC;
