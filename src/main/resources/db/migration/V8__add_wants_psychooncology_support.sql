-- ==============================================================================
-- V8: Add wants_psychooncology_support to enrollments
-- ==============================================================================

ALTER TABLE enrollments
    ADD COLUMN IF NOT EXISTS wants_psychooncology_support BOOLEAN;
