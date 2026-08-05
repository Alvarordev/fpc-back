-- Migration V6: Add ticket_number to alerts table and create sequence

ALTER TABLE alerts ADD COLUMN IF NOT EXISTS ticket_number VARCHAR(30) UNIQUE NULL;
CREATE SEQUENCE IF NOT EXISTS alert_ticket_seq START WITH 1001 INCREMENT BY 1;
