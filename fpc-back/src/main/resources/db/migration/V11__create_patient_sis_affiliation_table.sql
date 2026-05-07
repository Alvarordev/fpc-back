CREATE TABLE IF NOT EXISTS patient_sis_affiliation (
    id BIGSERIAL PRIMARY KEY,
    patient_id BIGINT NOT NULL,
    contact_id BIGINT NOT NULL,
    can_affiliate BOOLEAN NOT NULL,
    expected_date DATE,
    cant_affiliate_reason TEXT,
    affiliated_at TIMESTAMP WITH TIME ZONE,
    created_at TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT NOW(),
    CONSTRAINT fk_patient_sis_affiliation_patient FOREIGN KEY (patient_id) REFERENCES patients(id),
    CONSTRAINT fk_patient_sis_affiliation_contact FOREIGN KEY (contact_id) REFERENCES contacts(id)
);

CREATE INDEX IF NOT EXISTS idx_patient_sis_affiliation_patient_id ON patient_sis_affiliation(patient_id);
