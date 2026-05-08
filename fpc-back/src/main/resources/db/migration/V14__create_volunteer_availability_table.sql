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
