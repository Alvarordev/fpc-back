-- ==============================================================================
-- V3: Enrollment Wizard Fields
-- Adds 6 nullable columns to 5 existing tables and creates 2 new tables
-- for enrollment metadata and patient symptom reports.
-- ==============================================================================

-- New columns on existing tables (all nullable for backward compat)
ALTER TABLE patients ADD COLUMN IF NOT EXISTS gender VARCHAR(10);

ALTER TABLE patient_details ADD COLUMN IF NOT EXISTS zone_type VARCHAR(10);
ALTER TABLE patient_details ADD COLUMN IF NOT EXISTS emergency_contact_gender VARCHAR(10);

ALTER TABLE patient_treatments ADD COLUMN IF NOT EXISTS treatment_situation VARCHAR(50);

ALTER TABLE patient_medical_appointments ADD COLUMN IF NOT EXISTS is_first_consultation BOOLEAN DEFAULT FALSE;

ALTER TABLE patient_sis_affiliation ADD COLUMN IF NOT EXISTS comments TEXT;

-- New table: enrollments
CREATE TABLE IF NOT EXISTS enrollments (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    patient_id UUID NOT NULL,
    contact_id UUID NOT NULL,
    currently_attending_consultations BOOLEAN,
    currently_receiving_treatment BOOLEAN,
    entry_source VARCHAR(50),
    entry_sub_source VARCHAR(50),
    consent_to_contact BOOLEAN,
    consent_to_share_data BOOLEAN,
    affiliation_type VARCHAR(10) CHECK (affiliation_type IS NULL OR affiliation_type IN ('PATIENT', 'FAMILY')),
    affiliated_patient_name VARCHAR(255),
    affiliated_patient_dni VARCHAR(20),
    requires_transportation BOOLEAN,
    has_mobility_issues BOOLEAN,
    is_oncological_patient BOOLEAN NOT NULL DEFAULT false,
    survey_accepted BOOLEAN NOT NULL DEFAULT false,
    created_at TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    CONSTRAINT fk_enrollments_patient FOREIGN KEY (patient_id) REFERENCES patients(id),
    CONSTRAINT fk_enrollments_contact FOREIGN KEY (contact_id) REFERENCES contacts(id)
);

CREATE INDEX IF NOT EXISTS idx_enrollments_patient_id ON enrollments(patient_id);
CREATE INDEX IF NOT EXISTS idx_enrollments_contact_id ON enrollments(contact_id);
CREATE INDEX IF NOT EXISTS idx_enrollments_created_at ON enrollments(created_at);

-- New table: patient_symptom_reports
CREATE TABLE IF NOT EXISTS patient_symptom_reports (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    patient_id UUID NOT NULL,
    contact_id UUID NOT NULL,
    enrollment_id UUID,
    discomfort_severity VARCHAR(20),
    discomfort_description TEXT,
    symptom_duration VARCHAR(50),
    symptom_frequency VARCHAR(50),
    is_pain_present BOOLEAN,
    pain_intensity INTEGER,
    pain_location VARCHAR(255),
    pain_description TEXT,
    has_sought_medical_consultation BOOLEAN NOT NULL DEFAULT false,
    health_center_id UUID REFERENCES health_centers(id),
    specialty VARCHAR(255),
    created_at TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    CONSTRAINT fk_symptom_reports_patient FOREIGN KEY (patient_id) REFERENCES patients(id),
    CONSTRAINT fk_symptom_reports_contact FOREIGN KEY (contact_id) REFERENCES contacts(id),
    CONSTRAINT fk_symptom_reports_enrollment FOREIGN KEY (enrollment_id) REFERENCES enrollments(id)
);

CREATE INDEX IF NOT EXISTS idx_symptom_reports_patient_id ON patient_symptom_reports(patient_id);
CREATE INDEX IF NOT EXISTS idx_symptom_reports_contact_id ON patient_symptom_reports(contact_id);
CREATE INDEX IF NOT EXISTS idx_symptom_reports_enrollment_id ON patient_symptom_reports(enrollment_id);
CREATE INDEX IF NOT EXISTS idx_symptom_reports_created_at ON patient_symptom_reports(created_at);
