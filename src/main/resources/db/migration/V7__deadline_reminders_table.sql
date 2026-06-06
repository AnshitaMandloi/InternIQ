-- V7: deadline_reminders — tracks which reminder emails have already been sent
-- so the scheduler is idempotent (safe to re-run every hour).

CREATE TABLE IF NOT EXISTS deadline_reminders (
    id            BIGINT       NOT NULL AUTO_INCREMENT,
    application_id BIGINT      NOT NULL,
    reminder_type VARCHAR(20)  NOT NULL COMMENT '24H or 48H',
    sent_at       DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP,

    PRIMARY KEY (id),
    UNIQUE KEY uq_app_reminder (application_id, reminder_type),
    CONSTRAINT fk_reminder_application
        FOREIGN KEY (application_id)
        REFERENCES applications(id)
        ON DELETE CASCADE
);