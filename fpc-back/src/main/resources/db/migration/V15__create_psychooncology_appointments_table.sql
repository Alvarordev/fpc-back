CREATE TABLE IF NOT EXISTS psychooncology_appointments (
    id BIGSERIAL PRIMARY KEY,
    patient_id BIGINT NOT NULL,
    volunteer_id BIGINT NOT NULL,
    contact_id BIGINT NOT NULL,
    availability_id BIGINT NOT NULL,
    patient_email VARCHAR(255),
    session_number INTEGER NOT NULL,
    is_additional_session BOOLEAN NOT NULL DEFAULT false,
    modality VARCHAR(20) NOT NULL
        CHECK (modality IN ('CALL', 'VIDEO_CALL')),
    status VARCHAR(20) NOT NULL DEFAULT 'SCHEDULED'
        CHECK (status IN ('SCHEDULED', 'COMPLETED', 'CANCELLED', 'NO_ANSWER')),
    scheduled_at TIMESTAMP WITH TIME ZONE NOT NULL,
    completed_at TIMESTAMP WITH TIME ZONE,
    topic_addressed TEXT,
    session_details TEXT,
    additional_observations TEXT,
    recommendations TEXT,
    referral VARCHAR(30)
        CHECK (referral IS NULL OR referral IN ('PSYCHIATRY', 'NEUROLOGY', 'CONTINUE_PSYCHOLOGY', 'PSYCHOONCOLOGIST', 'NONE')),
    created_at TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT NOW(),
    updated_at TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT NOW(),
    CONSTRAINT fk_psychooncology_appointments_patient FOREIGN KEY (patient_id) REFERENCES patients(id),
    CONSTRAINT fk_psychooncology_appointments_volunteer FOREIGN KEY (volunteer_id) REFERENCES volunteers(id),
    CONSTRAINT fk_psychooncology_appointments_contact FOREIGN KEY (contact_id) REFERENCES contacts(id),
    CONSTRAINT fk_psychooncology_appointments_availability FOREIGN KEY (availability_id) REFERENCES volunteer_availability(id)
);

CREATE INDEX IF NOT EXISTS idx_psychooncology_appointments_patient_id ON psychooncology_appointments(patient_id);
CREATE INDEX IF NOT EXISTS idx_psychooncology_appointments_volunteer_id ON psychooncology_appointments(volunteer_id);
CREATE INDEX IF NOT EXISTS idx_psychooncology_appointments_contact_id ON psychooncology_appointments(contact_id);
CREATE INDEX IF NOT EXISTS idx_psychooncology_appointments_availability_id ON psychooncology_appointments(availability_id);
CREATE INDEX IF NOT EXISTS idx_psychooncology_appointments_status ON psychooncology_appointments(status);
CREATE INDEX IF NOT EXISTS idx_psychooncology_appointments_scheduled_at ON psychooncology_appointments(scheduled_at);
CREATE INDEX IF NOT EXISTS idx_psychooncology_appointments_created_at ON psychooncology_appointments(created_at);
