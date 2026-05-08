CREATE TABLE IF NOT EXISTS patient_treatments (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    patient_id UUID NOT NULL,
    contact_id UUID NOT NULL,
    diagnosis_id UUID NOT NULL,
    treatment_type VARCHAR(255) NOT NULL,
    treatment_frequency VARCHAR(100),
    health_establishment VARCHAR(255),
    start_date DATE,
    end_date DATE,
    is_current BOOLEAN NOT NULL,
    change_reason TEXT,
    not_receiving_reason TEXT,
    created_at TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT NOW(),
    CONSTRAINT fk_patient_treatments_patient FOREIGN KEY (patient_id) REFERENCES patients(id),
    CONSTRAINT fk_patient_treatments_contact FOREIGN KEY (contact_id) REFERENCES contacts(id),
    CONSTRAINT fk_patient_treatments_diagnosis FOREIGN KEY (diagnosis_id) REFERENCES patient_diagnoses(id)
);

CREATE INDEX IF NOT EXISTS idx_patient_treatments_patient_id ON patient_treatments(patient_id);
CREATE INDEX IF NOT EXISTS idx_patient_treatments_diagnosis_id ON patient_treatments(diagnosis_id);
CREATE INDEX IF NOT EXISTS idx_patient_treatments_is_current ON patient_treatments(is_current);
