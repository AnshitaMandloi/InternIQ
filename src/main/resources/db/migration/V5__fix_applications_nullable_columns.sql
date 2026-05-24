-- V5__fix_applications_nullable_columns.sql
-- The applications table (V1) has 'role' and 'company_name' as NOT NULL,
-- but the tracker API doesn't set these — it reads them from internship_listings.
-- Make them nullable so inserts don't fail.

ALTER TABLE applications
    MODIFY COLUMN role VARCHAR(200) NULL DEFAULT NULL;

ALTER TABLE applications
    MODIFY COLUMN company_name VARCHAR(200) NULL DEFAULT NULL;

-- applied_date was DATE NOT NULL in V1, but V4 added applied_at DATETIME.
-- Make applied_date optional too so the entity insert doesn't fail.
ALTER TABLE applications
    MODIFY COLUMN applied_date DATE NULL DEFAULT NULL;
