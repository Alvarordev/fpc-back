CREATE TABLE IF NOT EXISTS contacts (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    patient_id UUID NOT NULL,
    agent_id UUID,
    type VARCHAR(20) NOT NULL
        CHECK (type IN ('WHATSAPP', 'CALL', 'VIDEO_CALL', 'EMAIL', 'IN_PERSON')),
    status VARCHAR(20) NOT NULL
        CHECK (status IN ('SCHEDULED', 'COMPLETED', 'CANCELLED', 'NO_ANSWER')),
    purpose VARCHAR(30) NOT NULL
        CHECK (purpose IN ('FIRST_CONTACT', 'ENROLLMENT', 'FOLLOW_UP',
            'PSYCHOONCOLOGY_REFERRAL', 'OTHER')),
    scheduled_at TIMESTAMP WITH TIME ZONE,
    completed_at TIMESTAMP WITH TIME ZONE,
    notes TEXT,
    scheduled_next_contact_id UUID,
    created_at TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT NOW(),
    updated_at TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT NOW(),
    CONSTRAINT fk_contacts_patient FOREIGN KEY (patient_id) REFERENCES patients(id),
    CONSTRAINT fk_contacts_agent FOREIGN KEY (agent_id) REFERENCES agents(id),
    CONSTRAINT fk_contacts_scheduled_next FOREIGN KEY (scheduled_next_contact_id) REFERENCES contacts(id)
);

CREATE INDEX IF NOT EXISTS idx_contacts_patient_id ON contacts(patient_id);
CREATE INDEX IF NOT EXISTS idx_contacts_agent_id ON contacts(agent_id);
CREATE INDEX IF NOT EXISTS idx_contacts_type ON contacts(type);
CREATE INDEX IF NOT EXISTS idx_contacts_status ON contacts(status);
CREATE INDEX IF NOT EXISTS idx_contacts_purpose ON contacts(purpose);
CREATE INDEX IF NOT EXISTS idx_contacts_created_at ON contacts(created_at);
