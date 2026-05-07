CREATE TABLE IF NOT EXISTS patient_diagnoses (
    id BIGSERIAL PRIMARY KEY,
    patient_id BIGINT NOT NULL,
    contact_id BIGINT NOT NULL,
    diagnosis TEXT NOT NULL,
    cancer_stage VARCHAR(20)
        CHECK (cancer_stage IS NULL OR cancer_stage IN (
            'STAGE_1', 'STAGE_2', 'STAGE_3', 'STAGE_4', 'UNKNOWN'
        )),
    diagnosis_date DATE,
    diagnosis_location VARCHAR(255),
    diagnosis_specialty VARCHAR(255),
    symptom_leading_to_checkup TEXT,
    wait_time_for_diagnosis VARCHAR(100),
    has_medical_report BOOLEAN NOT NULL DEFAULT false,
    is_current BOOLEAN NOT NULL,
    change_reason TEXT,
    created_at TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT NOW(),
    CONSTRAINT fk_patient_diagnoses_patient FOREIGN KEY (patient_id) REFERENCES patients(id),
    CONSTRAINT fk_patient_diagnoses_contact FOREIGN KEY (contact_id) REFERENCES contacts(id)
);

CREATE INDEX IF NOT EXISTS idx_patient_diagnoses_patient_id ON patient_diagnoses(patient_id);
CREATE INDEX IF NOT EXISTS idx_patient_diagnoses_is_current ON patient_diagnoses(is_current);
