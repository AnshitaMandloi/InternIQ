-- Fix deadline: DATE → DATETIME(6)
ALTER TABLE applications 
MODIFY COLUMN deadline DATETIME(6) NULL;

-- Fix status: ENUM → VARCHAR
ALTER TABLE applications 
MODIFY COLUMN status VARCHAR(255) NOT NULL;

-- Fix applied_at: DATE → DATETIME(6) (agar hai toh)
ALTER TABLE applications 
MODIFY COLUMN applied_at DATETIME(6) NULL;

-- Fix updated_at: DATE → DATETIME(6) (agar hai toh)
ALTER TABLE applications 
MODIFY COLUMN updated_at DATETIME(6) NULL;