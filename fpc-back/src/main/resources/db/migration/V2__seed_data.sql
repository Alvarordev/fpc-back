INSERT INTO users (id, email, password_hash, role, is_active, created_at, updated_at)
SELECT gen_random_uuid(), 'admin@gmail.com', '$2a$10$oW26XCuGwyWaRwbGuT5jBeJERhMUcnwkK6WselaiYnc5GdrDnNNsy', 'ADMIN', true, NOW(), NOW()
WHERE NOT EXISTS (SELECT 1 FROM users WHERE email = 'admin@gmail.com');

INSERT INTO users (id, email, password_hash, role, is_active, created_at, updated_at)
SELECT gen_random_uuid(), 'callcenter@gmail.com', '$2b$10$9Oa0ldDPSaUdYKymZpKqausQHxUDaNsUzF1gK1M4diZiux3xziPhm', 'AGENT', true, NOW(), NOW()
WHERE NOT EXISTS (SELECT 1 FROM users WHERE email = 'callcenter@gmail.com');

INSERT INTO agents (id, user_id, full_name, phone, created_at)
SELECT gen_random_uuid(), u.id, 'Call Center Agent', '+0000000000', NOW()
FROM users u
WHERE u.email = 'callcenter@gmail.com'
  AND NOT EXISTS (SELECT 1 FROM agents a WHERE a.user_id = u.id);
