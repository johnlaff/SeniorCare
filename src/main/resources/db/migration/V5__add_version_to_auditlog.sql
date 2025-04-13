-- Add version column for optimistic locking
ALTER TABLE audit_logs ADD COLUMN version BIGINT DEFAULT 0 NOT NULL;