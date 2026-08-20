CREATE TABLE IF NOT EXISTS patient_family_prevention_talk_interests (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    patient_id UUID NOT NULL,
    talk_name VARCHAR(255) NOT NULL,
    family_member_name VARCHAR(255) NOT NULL,
    family_member_phone VARCHAR(50) NOT NULL,
    family_member_email VARCHAR(255) NOT NULL,
    created_at TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT NOW(),
    CONSTRAINT fk_patient_family_prevention_talk_interests_patient
        FOREIGN KEY (patient_id) REFERENCES patients(id)
);

CREATE INDEX IF NOT EXISTS idx_patient_family_talk_interests_patient_id
    ON patient_family_prevention_talk_interests(patient_id);

CREATE INDEX IF NOT EXISTS idx_patient_family_talk_interests_patient_talk_name
    ON patient_family_prevention_talk_interests(patient_id, talk_name);
