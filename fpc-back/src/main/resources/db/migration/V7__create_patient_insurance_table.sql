CREATE TABLE IF NOT EXISTS patient_insurance (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    patient_id UUID NOT NULL,
    contact_id UUID NOT NULL,
    insurance_type VARCHAR(30) NOT NULL
        CHECK (insurance_type IN ('SIS', 'ESSALUD', 'EPS', 'FUERZAS_ARMADAS', 'SALUDPOL', 'NONE')),
    eps_provider VARCHAR(30)
        CHECK (eps_provider IS NULL OR eps_provider IN (
            'PACIFICO', 'RIMAC', 'MAPFRE', 'LA_POSITIVA', 'SANITAS', 'ONCOSALUD', 'OTHER'
        )),
    is_current BOOLEAN NOT NULL,
    change_reason TEXT,
    start_date DATE,
    end_date DATE,
    created_at TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT NOW(),
    CONSTRAINT fk_patient_insurance_patient FOREIGN KEY (patient_id) REFERENCES patients(id),
    CONSTRAINT fk_patient_insurance_contact FOREIGN KEY (contact_id) REFERENCES contacts(id)
);

CREATE INDEX IF NOT EXISTS idx_patient_insurance_patient_id ON patient_insurance(patient_id);
CREATE INDEX IF NOT EXISTS idx_patient_insurance_is_current ON patient_insurance(is_current);
