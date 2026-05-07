CREATE TABLE IF NOT EXISTS patients (
    id BIGSERIAL PRIMARY KEY,
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
