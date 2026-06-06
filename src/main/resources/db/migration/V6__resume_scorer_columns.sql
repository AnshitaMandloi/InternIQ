-- V6__resume_scorer_columns.sql
-- MySQL-safe version using information_schema checks

SET @dbname = DATABASE();

-- Add 'resume_text' if missing
SET @exist = (SELECT COUNT(*) FROM information_schema.COLUMNS WHERE TABLE_SCHEMA=@dbname AND TABLE_NAME='resume_scores' AND COLUMN_NAME='resume_text');
SET @sql = IF(@exist=0, 'ALTER TABLE resume_scores ADD COLUMN resume_text MEDIUMTEXT NULL', 'SELECT 1');
PREPARE stmt FROM @sql; EXECUTE stmt; DEALLOCATE PREPARE stmt;

-- Add 'feedback_json' if missing
SET @exist = (SELECT COUNT(*) FROM information_schema.COLUMNS WHERE TABLE_SCHEMA=@dbname AND TABLE_NAME='resume_scores' AND COLUMN_NAME='feedback_json');
SET @sql = IF(@exist=0, 'ALTER TABLE resume_scores ADD COLUMN feedback_json TEXT NULL', 'SELECT 1');
PREPARE stmt FROM @sql; EXECUTE stmt; DEALLOCATE PREPARE stmt;

-- Add 'file_name' if missing
SET @exist = (SELECT COUNT(*) FROM information_schema.COLUMNS WHERE TABLE_SCHEMA=@dbname AND TABLE_NAME='resume_scores' AND COLUMN_NAME='file_name');
SET @sql = IF(@exist=0, 'ALTER TABLE resume_scores ADD COLUMN file_name VARCHAR(255) NULL', 'SELECT 1');
PREPARE stmt FROM @sql; EXECUTE stmt; DEALLOCATE PREPARE stmt;