-- Add derivation and under review fields to alerts table
ALTER TABLE alerts ADD COLUMN under_review BOOLEAN NOT NULL DEFAULT FALSE;
ALTER TABLE alerts ADD COLUMN derived_to VARCHAR(50) NULL;
ALTER TABLE alerts ADD COLUMN derivation_notes TEXT NULL;
