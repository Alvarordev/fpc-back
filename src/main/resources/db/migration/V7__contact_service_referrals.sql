CREATE TABLE IF NOT EXISTS contact_service_referrals (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    contact_id UUID NOT NULL UNIQUE,
    referred_to_social_worker BOOLEAN,
    referred_to_susalud BOOLEAN,
    susalud_registration_number VARCHAR(100),
    received_food_guide BOOLEAN,
    participates_in_gam BOOLEAN,
    program_satisfaction TEXT,
    wellbeing_changes TEXT,
    knows_about_fissal BOOLEAN,
    referred_to_paus BOOLEAN,
    referred_to_dae BOOLEAN,
    referred_to_fissal BOOLEAN,
    created_at TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    updated_at TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    CONSTRAINT fk_contact_service_referrals_contact
        FOREIGN KEY (contact_id) REFERENCES contacts(id) ON DELETE CASCADE
);

CREATE INDEX IF NOT EXISTS idx_contact_service_referrals_contact_id
    ON contact_service_referrals(contact_id);
