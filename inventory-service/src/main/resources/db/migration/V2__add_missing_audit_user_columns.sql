-- Align inventory audit-enabled entities with AuditableEntity.
-- V1 already created tenant/org/timestamp/version columns on these tables.

ALTER TABLE stock_levels
    ADD COLUMN IF NOT EXISTS created_by VARCHAR(64) NOT NULL DEFAULT 'system',
    ADD COLUMN IF NOT EXISTS updated_by VARCHAR(64) NOT NULL DEFAULT 'system';

ALTER TABLE reservations
    ADD COLUMN IF NOT EXISTS created_by VARCHAR(64) NOT NULL DEFAULT 'system',
    ADD COLUMN IF NOT EXISTS updated_by VARCHAR(64) NOT NULL DEFAULT 'system';
