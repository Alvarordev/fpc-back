ALTER TABLE patient_diagnoses DROP COLUMN IF EXISTS diagnosis_location;

ALTER TABLE patient_diagnoses ADD COLUMN IF NOT EXISTS health_center_id UUID;

ALTER TABLE patient_diagnoses
    ADD CONSTRAINT fk_patient_diagnoses_health_center
    FOREIGN KEY (health_center_id) REFERENCES health_centers(id);

ALTER TABLE patient_treatments DROP COLUMN IF EXISTS health_establishment;

ALTER TABLE patient_treatments ADD COLUMN IF NOT EXISTS health_center_id UUID;

ALTER TABLE patient_treatments
    ADD CONSTRAINT fk_patient_treatments_health_center
    FOREIGN KEY (health_center_id) REFERENCES health_centers(id);

ALTER TABLE patient_medical_appointments DROP COLUMN IF EXISTS health_establishment;

ALTER TABLE patient_medical_appointments ADD COLUMN IF NOT EXISTS health_center_id UUID;

ALTER TABLE patient_medical_appointments
    ADD CONSTRAINT fk_patient_medical_appointments_health_center
    FOREIGN KEY (health_center_id) REFERENCES health_centers(id);
