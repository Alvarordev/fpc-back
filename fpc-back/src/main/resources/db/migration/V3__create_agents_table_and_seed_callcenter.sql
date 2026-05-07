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

INSERT INTO users (id, email, password_hash, role, is_active, created_at, updated_at)
SELECT
    gen_random_uuid(),
    'callcenter@gmail.com',
    '$2b$10$9Oa0ldDPSaUdYKymZpKqausQHxUDaNsUzF1gK1M4diZiux3xziPhm',
    'AGENT',
    true,
    NOW(),
    NOW()
WHERE NOT EXISTS (
    SELECT 1
    FROM users
    WHERE email = 'callcenter@gmail.com'
);

INSERT INTO agents (id, user_id, full_name, phone, created_at)
SELECT
    gen_random_uuid(),
    u.id,
    'Call Center Agent',
    '+0000000000',
    NOW()
FROM users u
WHERE u.email = 'callcenter@gmail.com'
  AND NOT EXISTS (
      SELECT 1
      FROM agents a
      WHERE a.user_id = u.id
  );
