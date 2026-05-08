CREATE TABLE IF NOT EXISTS alerts (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    health_center_id UUID NOT NULL,
    contact_id UUID NOT NULL,
    created_by_id UUID NOT NULL,
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
