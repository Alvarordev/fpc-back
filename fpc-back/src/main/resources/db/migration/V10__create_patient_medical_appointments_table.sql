CREATE TABLE IF NOT EXISTS patient_medical_appointments (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    patient_id UUID NOT NULL,
    contact_id UUID NOT NULL,
    health_establishment VARCHAR(255),
    specialty VARCHAR(255),
    appointment_date DATE,
    next_appointment_date DATE,
    has_referral_sheet BOOLEAN NOT NULL DEFAULT false,
    referred_to VARCHAR(255),
    difficulties TEXT,
    created_at TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT NOW(),
    CONSTRAINT fk_patient_medical_appointments_patient FOREIGN KEY (patient_id) REFERENCES patients(id),
    CONSTRAINT fk_patient_medical_appointments_contact FOREIGN KEY (contact_id) REFERENCES contacts(id)
);

CREATE INDEX IF NOT EXISTS idx_pat_med_appts_patient_id ON patient_medical_appointments(patient_id);
