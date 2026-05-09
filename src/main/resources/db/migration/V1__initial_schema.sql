CREATE TABLE IF NOT EXISTS users (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    email VARCHAR(255) NOT NULL UNIQUE,
    password_hash VARCHAR(255) NOT NULL,
    role VARCHAR(50) NOT NULL CHECK (role IN ('ADMIN', 'AGENT', 'VOLUNTEER')),
    is_active BOOLEAN NOT NULL DEFAULT true,
    created_at TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT NOW(),
    updated_at TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT NOW()
);

CREATE TABLE IF NOT EXISTS agents (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    user_id UUID NOT NULL,
    full_name VARCHAR(255) NOT NULL,
    phone VARCHAR(50) NOT NULL,
    created_at TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT NOW(),
    CONSTRAINT fk_agents_user FOREIGN KEY (user_id) REFERENCES users(id),
    CONSTRAINT uq_agents_user_id UNIQUE (user_id)
);

CREATE INDEX IF NOT EXISTS idx_agents_phone ON agents(phone);
CREATE INDEX IF NOT EXISTS idx_agents_created_at ON agents(created_at);

CREATE TABLE IF NOT EXISTS patients (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    full_name VARCHAR(255) NOT NULL,
    dni VARCHAR(20) UNIQUE,
    birth_date DATE,
    primary_phone VARCHAR(50) NOT NULL,
    secondary_phone VARCHAR(50),
    has_whatsapp BOOLEAN NOT NULL DEFAULT false,
    role VARCHAR(20) NOT NULL DEFAULT 'UNKNOWN'
        CHECK (role IN ('UNKNOWN', 'PATIENT', 'COMPANION')),
    status VARCHAR(20) NOT NULL DEFAULT 'PROSPECT'
        CHECK (status IN ('PROSPECT', 'ENROLLED', 'ACTIVE', 'INACTIVE')),
    created_at TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT NOW(),
    updated_at TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT NOW()
);

CREATE INDEX IF NOT EXISTS idx_patients_dni ON patients(dni);
CREATE INDEX IF NOT EXISTS idx_patients_primary_phone ON patients(primary_phone);
CREATE INDEX IF NOT EXISTS idx_patients_role ON patients(role);
CREATE INDEX IF NOT EXISTS idx_patients_status ON patients(status);
CREATE INDEX IF NOT EXISTS idx_patients_created_at ON patients(created_at);

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

CREATE TABLE IF NOT EXISTS health_centers (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    name VARCHAR(255) NOT NULL,
    slug VARCHAR(255) NOT NULL UNIQUE,
    department VARCHAR(50) NOT NULL
        CHECK (department IN (
            'AMAZONAS', 'ANCASH', 'APURIMAC', 'AREQUIPA', 'AYACUCHO',
            'CAJAMARCA', 'CALLAO', 'CUSCO', 'HUANCAVELICA', 'HUANUCO',
            'ICA', 'JUNIN', 'LA_LIBERTAD', 'LAMBAYEQUE', 'LIMA',
            'LORETO', 'MADRE_DE_DIOS', 'MOQUEGUA', 'PASCO', 'PIURA',
            'PUNO', 'SAN_MARTIN', 'TACNA', 'TUMBES', 'UCAYALI'
        )),
    is_active BOOLEAN NOT NULL DEFAULT TRUE,
    created_at TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT NOW(),
    updated_at TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT NOW()
);

CREATE INDEX IF NOT EXISTS idx_health_centers_slug ON health_centers(slug);
CREATE INDEX IF NOT EXISTS idx_health_centers_department ON health_centers(department);

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

CREATE TABLE IF NOT EXISTS patient_diagnoses (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    patient_id UUID NOT NULL,
    contact_id UUID NOT NULL,
    diagnosis TEXT NOT NULL,
    cancer_stage VARCHAR(20)
        CHECK (cancer_stage IS NULL OR cancer_stage IN (
            'STAGE_1', 'STAGE_2', 'STAGE_3', 'STAGE_4', 'UNKNOWN'
        )),
    diagnosis_date DATE,
    diagnosis_specialty VARCHAR(255),
    symptom_leading_to_checkup TEXT,
    wait_time_for_diagnosis VARCHAR(100),
    has_medical_report BOOLEAN NOT NULL DEFAULT false,
    is_current BOOLEAN NOT NULL,
    change_reason TEXT,
    health_center_id UUID,
    created_at TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT NOW(),
    CONSTRAINT fk_patient_diagnoses_patient FOREIGN KEY (patient_id) REFERENCES patients(id),
    CONSTRAINT fk_patient_diagnoses_contact FOREIGN KEY (contact_id) REFERENCES contacts(id),
    CONSTRAINT fk_patient_diagnoses_health_center FOREIGN KEY (health_center_id) REFERENCES health_centers(id)
);

CREATE INDEX IF NOT EXISTS idx_patient_diagnoses_patient_id ON patient_diagnoses(patient_id);
CREATE INDEX IF NOT EXISTS idx_patient_diagnoses_is_current ON patient_diagnoses(is_current);

CREATE TABLE IF NOT EXISTS patient_treatments (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    patient_id UUID NOT NULL,
    contact_id UUID NOT NULL,
    diagnosis_id UUID NOT NULL,
    treatment_type VARCHAR(255) NOT NULL,
    treatment_frequency VARCHAR(100),
    start_date DATE,
    end_date DATE,
    is_current BOOLEAN NOT NULL,
    change_reason TEXT,
    not_receiving_reason TEXT,
    health_center_id UUID,
    created_at TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT NOW(),
    CONSTRAINT fk_patient_treatments_patient FOREIGN KEY (patient_id) REFERENCES patients(id),
    CONSTRAINT fk_patient_treatments_contact FOREIGN KEY (contact_id) REFERENCES contacts(id),
    CONSTRAINT fk_patient_treatments_diagnosis FOREIGN KEY (diagnosis_id) REFERENCES patient_diagnoses(id),
    CONSTRAINT fk_patient_treatments_health_center FOREIGN KEY (health_center_id) REFERENCES health_centers(id)
);

CREATE INDEX IF NOT EXISTS idx_patient_treatments_patient_id ON patient_treatments(patient_id);
CREATE INDEX IF NOT EXISTS idx_patient_treatments_diagnosis_id ON patient_treatments(diagnosis_id);
CREATE INDEX IF NOT EXISTS idx_patient_treatments_is_current ON patient_treatments(is_current);

CREATE TABLE IF NOT EXISTS patient_medical_appointments (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    patient_id UUID NOT NULL,
    contact_id UUID NOT NULL,
    specialty VARCHAR(255),
    appointment_date DATE,
    next_appointment_date DATE,
    has_referral_sheet BOOLEAN NOT NULL DEFAULT false,
    referred_to VARCHAR(255),
    difficulties TEXT,
    health_center_id UUID,
    created_at TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT NOW(),
    CONSTRAINT fk_patient_medical_appointments_patient FOREIGN KEY (patient_id) REFERENCES patients(id),
    CONSTRAINT fk_patient_medical_appointments_contact FOREIGN KEY (contact_id) REFERENCES contacts(id),
    CONSTRAINT fk_patient_medical_appointments_health_center FOREIGN KEY (health_center_id) REFERENCES health_centers(id)
);

CREATE INDEX IF NOT EXISTS idx_pat_med_appts_patient_id ON patient_medical_appointments(patient_id);

CREATE TABLE IF NOT EXISTS patient_sis_affiliation (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    patient_id UUID NOT NULL,
    contact_id UUID NOT NULL,
    can_affiliate BOOLEAN NOT NULL,
    expected_date DATE,
    cant_affiliate_reason TEXT,
    affiliated_at TIMESTAMP WITH TIME ZONE,
    created_at TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT NOW(),
    CONSTRAINT fk_patient_sis_affiliation_patient FOREIGN KEY (patient_id) REFERENCES patients(id),
    CONSTRAINT fk_patient_sis_affiliation_contact FOREIGN KEY (contact_id) REFERENCES contacts(id)
);

CREATE INDEX IF NOT EXISTS idx_patient_sis_affiliation_patient_id ON patient_sis_affiliation(patient_id);

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

CREATE TABLE IF NOT EXISTS volunteers (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    user_id UUID NOT NULL,
    first_name VARCHAR(255) NOT NULL,
    last_name VARCHAR(255) NOT NULL,
    specialty VARCHAR(255) NOT NULL,
    email VARCHAR(255) NOT NULL,
    phone VARCHAR(50) NOT NULL,
    is_active BOOLEAN NOT NULL DEFAULT true,
    created_at TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT NOW(),
    updated_at TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT NOW(),
    CONSTRAINT fk_volunteers_user FOREIGN KEY (user_id) REFERENCES users(id),
    CONSTRAINT uq_volunteers_user_id UNIQUE (user_id)
);

CREATE INDEX IF NOT EXISTS idx_volunteers_email ON volunteers(email);
CREATE INDEX IF NOT EXISTS idx_volunteers_phone ON volunteers(phone);
CREATE INDEX IF NOT EXISTS idx_volunteers_is_active ON volunteers(is_active);
CREATE INDEX IF NOT EXISTS idx_volunteers_created_at ON volunteers(created_at);

CREATE TABLE IF NOT EXISTS volunteer_availability (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    volunteer_id UUID NOT NULL,
    date DATE NOT NULL,
    start_time TIME NOT NULL,
    end_time TIME NOT NULL,
    status VARCHAR(20) NOT NULL DEFAULT 'AVAILABLE'
        CHECK (status IN ('AVAILABLE', 'RESERVED')),
    created_at TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT NOW(),
    CONSTRAINT fk_volunteer_availability_volunteer FOREIGN KEY (volunteer_id) REFERENCES volunteers(id),
    CONSTRAINT uq_volunteer_availability_slot UNIQUE (volunteer_id, date, start_time)
);

CREATE INDEX IF NOT EXISTS idx_volunteer_availability_volunteer_id ON volunteer_availability(volunteer_id);
CREATE INDEX IF NOT EXISTS idx_volunteer_availability_date ON volunteer_availability(date);
CREATE INDEX IF NOT EXISTS idx_volunteer_availability_status ON volunteer_availability(status);

CREATE TABLE IF NOT EXISTS psychooncology_appointments (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    patient_id UUID NOT NULL,
    volunteer_id UUID NOT NULL,
    contact_id UUID NOT NULL,
    availability_id UUID NOT NULL,
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

CREATE TABLE IF NOT EXISTS alerts (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    health_center_id UUID NOT NULL,
    contact_id UUID NOT NULL,
    created_by_id UUID NOT NULL,
    title TEXT NOT NULL,
    description TEXT NOT NULL,
    status VARCHAR(30) NOT NULL DEFAULT 'ACTIVE'
        CHECK (status IN ('ACTIVE', 'RESOLVED')),
    resolved_at TIMESTAMP WITH TIME ZONE,
    resolved_by_id UUID,
    created_at TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT NOW(),
    updated_at TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT NOW(),
    CONSTRAINT fk_alerts_health_center FOREIGN KEY (health_center_id) REFERENCES health_centers(id),
    CONSTRAINT fk_alerts_contact FOREIGN KEY (contact_id) REFERENCES contacts(id),
    CONSTRAINT fk_alerts_created_by FOREIGN KEY (created_by_id) REFERENCES agents(id),
    CONSTRAINT fk_alerts_resolved_by FOREIGN KEY (resolved_by_id) REFERENCES agents(id)
);

CREATE INDEX IF NOT EXISTS idx_alerts_health_center_id ON alerts(health_center_id);
CREATE INDEX IF NOT EXISTS idx_alerts_contact_id ON alerts(contact_id);
CREATE INDEX IF NOT EXISTS idx_alerts_created_by_id ON alerts(created_by_id);
CREATE INDEX IF NOT EXISTS idx_alerts_status ON alerts(status);
