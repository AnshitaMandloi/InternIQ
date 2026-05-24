-- ==========================================
-- InternIQ - V3 Fix external_id column size
-- SerpAPI job_id is longer than 255 chars
-- ==========================================

ALTER TABLE internship_listings 
MODIFY COLUMN external_id VARCHAR(800);