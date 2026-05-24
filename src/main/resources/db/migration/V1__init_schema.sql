-- ==========================================
-- InternIQ DB Schema - V1
-- ==========================================

CREATE TABLE users (
    id          BIGINT AUTO_INCREMENT PRIMARY KEY,
    name        VARCHAR(100) NOT NULL,
    email       VARCHAR(150) NOT NULL UNIQUE,
    password    VARCHAR(255) NOT NULL,
    college     VARCHAR(200),
    branch      VARCHAR(100),
    year        INT,
    created_at  TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at  TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP
);

CREATE TABLE skills (
    id      BIGINT AUTO_INCREMENT PRIMARY KEY,
    user_id BIGINT NOT NULL,
    name    VARCHAR(100) NOT NULL,
    FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE
);

CREATE TABLE projects (
    id          BIGINT AUTO_INCREMENT PRIMARY KEY,
    user_id     BIGINT NOT NULL,
    title       VARCHAR(200) NOT NULL,
    description TEXT,
    tech_stack  VARCHAR(300),
    github_url  VARCHAR(300),
    FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE
);

CREATE TABLE internship_listings (
    id              BIGINT AUTO_INCREMENT PRIMARY KEY,
    external_id     VARCHAR(255) UNIQUE,
    title           VARCHAR(200) NOT NULL,
    company         VARCHAR(200) NOT NULL,
    location        VARCHAR(200),
    is_remote       BOOLEAN DEFAULT FALSE,
    description     TEXT,
    apply_url       VARCHAR(500),
    source          ENUM('JSEARCH','COMMUNITY') DEFAULT 'JSEARCH',
    posted_by       BIGINT,
    posted_at       TIMESTAMP,
    fetched_at      TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (posted_by) REFERENCES users(id) ON DELETE SET NULL
);

CREATE TABLE applications (
    id              BIGINT AUTO_INCREMENT PRIMARY KEY,
    user_id         BIGINT NOT NULL,
    listing_id      BIGINT,
    company_name    VARCHAR(200) NOT NULL,
    role            VARCHAR(200) NOT NULL,
    apply_url       VARCHAR(500),
    status          ENUM('SAVED','APPLIED','INTERVIEW','OFFER','REJECTED') DEFAULT 'SAVED',
    applied_date    DATE,
    deadline        DATE,
    notes           TEXT,
    created_at      TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at      TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE,
    FOREIGN KEY (listing_id) REFERENCES internship_listings(id) ON DELETE SET NULL
);

CREATE TABLE resume_scores (
    id              BIGINT AUTO_INCREMENT PRIMARY KEY,
    user_id         BIGINT NOT NULL,
    job_description TEXT NOT NULL,
    score           INT,
    matched_skills  TEXT,
    missing_skills  TEXT,
    suggestions     TEXT,
    generated_at    TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE
);

CREATE TABLE deadline_reminders (
    id              BIGINT AUTO_INCREMENT PRIMARY KEY,
    application_id  BIGINT NOT NULL,
    remind_at       TIMESTAMP NOT NULL,
    sent            BOOLEAN DEFAULT FALSE,
    FOREIGN KEY (application_id) REFERENCES applications(id) ON DELETE CASCADE
);

-- Indexes
CREATE INDEX idx_applications_user   ON applications(user_id);
CREATE INDEX idx_applications_status ON applications(status);
CREATE INDEX idx_listings_source     ON internship_listings(source);
CREATE INDEX idx_skills_user         ON skills(user_id);
CREATE INDEX idx_reminders_sent      ON deadline_reminders(sent, remind_at);