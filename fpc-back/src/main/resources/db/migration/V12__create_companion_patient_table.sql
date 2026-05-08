CREATE TABLE IF NOT EXISTS companion_patient (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    companion_id UUID NOT NULL,
    patient_id UUID NOT NULL,
    is_primary_informant BOOLEAN NOT NULL DEFAULT false,
    created_at TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT NOW(),
    CONSTRAINT fk_companion_patient_companion FOREIGN KEY (companion_id) REFERENCES patients(id),
    CONSTRAINT fk_companion_patient_patient FOREIGN KEY (patient_id) REFERENCES patients(id)
);

CREATE INDEX IF NOT EXISTS idx_companion_patient_companion_id ON companion_patient(companion_id);
CREATE INDEX IF NOT EXISTS idx_companion_patient_patient_id ON companion_patient(patient_id);
