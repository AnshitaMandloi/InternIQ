-- V10__fix_deadline_reminders_columns.sql
-- V1 originally created deadline_reminders with (remind_at, sent).
-- V7 tried to redefine it with (reminder_type, sent_at) via
-- CREATE TABLE IF NOT EXISTS, but since the table already existed
-- from V1, that statement was a no-op — so reminder_type/sent_at
-- were never actually added. This migration adds them safely.

SET @dbname = DATABASE();

-- Add 'reminder_type' if missing
SET @exist = (SELECT COUNT(*) FROM information_schema.COLUMNS WHERE TABLE_SCHEMA=@dbname AND TABLE_NAME='deadline_reminders' AND COLUMN_NAME='reminder_type');
SET @sql = IF(@exist=0, 'ALTER TABLE deadline_reminders ADD COLUMN reminder_type VARCHAR(20) NOT NULL DEFAULT ''24H'' COMMENT ''24H or 48H''', 'SELECT 1');
PREPARE stmt FROM @sql; EXECUTE stmt; DEALLOCATE PREPARE stmt;

-- Add 'sent_at' if missing
SET @exist = (SELECT COUNT(*) FROM information_schema.COLUMNS WHERE TABLE_SCHEMA=@dbname AND TABLE_NAME='deadline_reminders' AND COLUMN_NAME='sent_at');
SET @sql = IF(@exist=0, 'ALTER TABLE deadline_reminders ADD COLUMN sent_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP', 'SELECT 1');
PREPARE stmt FROM @sql; EXECUTE stmt; DEALLOCATE PREPARE stmt;

-- Drop the default now that the column exists, so future inserts
-- must supply an explicit value (matches entity: reminderType has no default in Java)
ALTER TABLE deadline_reminders ALTER COLUMN reminder_type DROP DEFAULT;
