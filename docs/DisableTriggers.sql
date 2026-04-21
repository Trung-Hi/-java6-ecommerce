-- ============================================
-- DISABLE: Tắt tất cả trigger trên Product
-- ============================================

-- Tìm và tắt tất cả trigger trên Product
DECLARE @triggerName NVARCHAR(128);
DECLARE @sql NVARCHAR(MAX);

DECLARE trigger_cursor CURSOR FOR
SELECT t.name
FROM sys.triggers t
INNER JOIN sys.tables tb ON t.parent_id = tb.object_id
WHERE tb.name = 'Product' OR tb.name = 'products';

OPEN trigger_cursor;
FETCH NEXT FROM trigger_cursor INTO @triggerName;

WHILE @@FETCH_STATUS = 0
BEGIN
    SET @sql = 'DISABLE TRIGGER ' + QUOTENAME(@triggerName) + ' ON ' + QUOTENAME('Product');
    PRINT 'Disabling: ' + @triggerName;
    EXEC sp_executesql @sql;
    FETCH NEXT FROM trigger_cursor INTO @triggerName;
END;

CLOSE trigger_cursor;
DEALLOCATE trigger_cursor;

PRINT 'All triggers on Product disabled.';
