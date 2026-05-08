CREATE TABLE IF NOT EXISTS patient_details (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    patient_id UUID NOT NULL,
    birth_department VARCHAR(255),
    current_address TEXT,
    current_district VARCHAR(255),
    current_department VARCHAR(255),
    dni_matches_address BOOLEAN,
    travel_time_to_hospital VARCHAR(100),
    emergency_contact_name VARCHAR(255),
    emergency_contact_phone VARCHAR(50),
    education_level VARCHAR(30)
        CHECK (education_level IS NULL OR education_level IN (
            'INITIAL', 'PRIMARY_INCOMPLETE', 'PRIMARY',
            'SECONDARY_INCOMPLETE', 'SECONDARY', 'TECHNICAL',
            'TECHNICAL_INCOMPLETE', 'HIGHER', 'HIGHER_INCOMPLETE', 'NONE'
        )),
    native_language VARCHAR(100),
    requires_translation BOOLEAN NOT NULL DEFAULT false,
    created_at TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT NOW(),
    updated_at TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT NOW(),
    CONSTRAINT fk_patient_details_patient FOREIGN KEY (patient_id) REFERENCES patients(id),
    CONSTRAINT uq_patient_details_patient_id UNIQUE (patient_id)
);
