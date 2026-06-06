-- ==========================================
-- InternIQ - V3 Fix external_id column size
-- SerpAPI job_id is longer than 255 chars
-- VARCHAR(500) × 4 bytes (utf8mb4) = 2000 bytes — within MySQL's 3072 byte limit
-- ==========================================

ALTER TABLE internship_listings
MODIFY COLUMN external_id VARCHAR(500);