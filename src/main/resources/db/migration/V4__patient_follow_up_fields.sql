ALTER TABLE patient_details
    ADD COLUMN IF NOT EXISTS evidence_of_domestic_violence BOOLEAN,
    ADD COLUMN IF NOT EXISTS uses_wood_stove BOOLEAN,
    ADD COLUMN IF NOT EXISTS is_working BOOLEAN,
    ADD COLUMN IF NOT EXISTS receives_financial_support BOOLEAN,
    ADD COLUMN IF NOT EXISTS program_dropout_reason TEXT,
    ADD COLUMN IF NOT EXISTS program_dropout_date DATE,
    ADD COLUMN IF NOT EXISTS referred_to_social_worker BOOLEAN,
    ADD COLUMN IF NOT EXISTS has_conadis_card BOOLEAN,
    ADD COLUMN IF NOT EXISTS knows_about_fissal BOOLEAN,
    ADD COLUMN IF NOT EXISTS is_deceased BOOLEAN;
