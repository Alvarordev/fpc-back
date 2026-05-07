CREATE TABLE users (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    email VARCHAR(255) NOT NULL UNIQUE,
    password_hash VARCHAR(255) NOT NULL,
    role VARCHAR(50) NOT NULL CHECK (role IN ('ADMIN', 'AGENT', 'PSYCHOLOGIST')),
    is_active BOOLEAN NOT NULL DEFAULT true,
    created_at TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT NOW(),
    updated_at TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT NOW()
);

INSERT INTO users (id, email, password_hash, role, is_active, created_at, updated_at)
VALUES (
    gen_random_uuid(),
    'admin@gmail.com',
    '$2a$10$oW26XCuGwyWaRwbGuT5jBeJERhMUcnwkK6WselaiYnc5GdrDnNNsy',
    'ADMIN',
    true,
    NOW(),
    NOW()
);
