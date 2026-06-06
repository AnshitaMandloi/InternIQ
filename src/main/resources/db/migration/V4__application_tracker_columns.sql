-- V4__application_tracker_columns.sql
-- MySQL-safe version: checks information_schema before adding each column
-- (MySQL 8.0 does not support ADD COLUMN IF NOT EXISTS unlike MariaDB)

SET @dbname = DATABASE();

-- Add 'deadline' if missing
SET @exist = (SELECT COUNT(*) FROM information_schema.COLUMNS WHERE TABLE_SCHEMA=@dbname AND TABLE_NAME='applications' AND COLUMN_NAME='deadline');
SET @sql = IF(@exist=0, 'ALTER TABLE applications ADD COLUMN deadline DATETIME NULL', 'SELECT 1');
PREPARE stmt FROM @sql; EXECUTE stmt; DEALLOCATE PREPARE stmt;

-- Add 'updated_at' if missing
SET @exist = (SELECT COUNT(*) FROM information_schema.COLUMNS WHERE TABLE_SCHEMA=@dbname AND TABLE_NAME='applications' AND COLUMN_NAME='updated_at');
SET @sql = IF(@exist=0, 'ALTER TABLE applications ADD COLUMN updated_at DATETIME NULL', 'SELECT 1');
PREPARE stmt FROM @sql; EXECUTE stmt; DEALLOCATE PREPARE stmt;

-- Add 'applied_at' if missing
SET @exist = (SELECT COUNT(*) FROM information_schema.COLUMNS WHERE TABLE_SCHEMA=@dbname AND TABLE_NAME='applications' AND COLUMN_NAME='applied_at');
SET @sql = IF(@exist=0, 'ALTER TABLE applications ADD COLUMN applied_at DATETIME NULL DEFAULT CURRENT_TIMESTAMP', 'SELECT 1');
PREPARE stmt FROM @sql; EXECUTE stmt; DEALLOCATE PREPARE stmt;

-- Add 'notes' if missing
SET @exist = (SELECT COUNT(*) FROM information_schema.COLUMNS WHERE TABLE_SCHEMA=@dbname AND TABLE_NAME='applications' AND COLUMN_NAME='notes');
SET @sql = IF(@exist=0, 'ALTER TABLE applications ADD COLUMN notes TEXT NULL', 'SELECT 1');
PREPARE stmt FROM @sql; EXECUTE stmt; DEALLOCATE PREPARE stmt;

-- Ensure status ENUM is correct (safe to run always)
ALTER TABLE applications
    MODIFY COLUMN status ENUM('SAVED','APPLIED','INTERVIEW','OFFER','REJECTED') NOT NULL DEFAULT 'SAVED';