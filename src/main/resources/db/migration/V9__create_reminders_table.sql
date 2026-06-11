-- ==============================================================================
-- V9: Create reminders table
-- ==============================================================================

CREATE TABLE reminders (
    id UUID DEFAULT gen_random_uuid() PRIMARY KEY,
    patient_id UUID NOT NULL REFERENCES patients(id),
    contact_id UUID NOT NULL REFERENCES contacts(id),
    type VARCHAR(20) NOT NULL CHECK (type IN ('LABORATORIO', 'IMAGEN', 'CONSULTA', 'PROCEDIMIENTO', 'MEDICACION', 'OTRO')),
    description VARCHAR(500) NOT NULL,
    scheduled_date DATE NOT NULL,
    status VARCHAR(20) NOT NULL DEFAULT 'PENDIENTE' CHECK (status IN ('PENDIENTE', 'COMPLETADO', 'CANCELADO')),
    notes TEXT,
    created_at TIMESTAMP NOT NULL DEFAULT NOW(),
    updated_at TIMESTAMP NOT NULL DEFAULT NOW()
);

CREATE INDEX idx_reminders_patient_id ON reminders(patient_id);
