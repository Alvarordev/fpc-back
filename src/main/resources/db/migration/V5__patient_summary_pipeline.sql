ALTER TABLE patients
    ADD COLUMN IF NOT EXISTS summary_source_updated_at TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT NOW();

CREATE TABLE IF NOT EXISTS patient_summaries (
    patient_id UUID PRIMARY KEY,
    status VARCHAR(20) NOT NULL
        CHECK (status IN ('PENDING', 'PROCESSING', 'READY', 'RETRY_WAIT', 'FAILED_PERMANENT')),
    summary_json TEXT,
    generated_at TIMESTAMP WITH TIME ZONE,
    generated_from_source_updated_at TIMESTAMP WITH TIME ZONE,
    retry_count INTEGER NOT NULL DEFAULT 0,
    last_attempt_at TIMESTAMP WITH TIME ZONE,
    next_attempt_at TIMESTAMP WITH TIME ZONE,
    processing_started_at TIMESTAMP WITH TIME ZONE,
    last_error_code VARCHAR(50),
    last_error_message TEXT,
    schema_version INTEGER NOT NULL DEFAULT 1,
    created_at TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT NOW(),
    updated_at TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT NOW(),
    CONSTRAINT fk_patient_summaries_patient FOREIGN KEY (patient_id) REFERENCES patients(id)
);

CREATE INDEX IF NOT EXISTS idx_patient_summaries_status_next_attempt
    ON patient_summaries(status, next_attempt_at);

CREATE INDEX IF NOT EXISTS idx_patient_summaries_processing_started_at
    ON patient_summaries(processing_started_at);

CREATE TABLE IF NOT EXISTS llm_rate_limit_guards (
    provider VARCHAR(20) PRIMARY KEY,
    created_at TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT NOW()
);

CREATE TABLE IF NOT EXISTS llm_rate_limit_calls (
    id BIGSERIAL PRIMARY KEY,
    provider VARCHAR(20) NOT NULL,
    reserved_at TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT NOW()
);

CREATE INDEX IF NOT EXISTS idx_llm_rate_limit_calls_provider_reserved_at
    ON llm_rate_limit_calls(provider, reserved_at);
