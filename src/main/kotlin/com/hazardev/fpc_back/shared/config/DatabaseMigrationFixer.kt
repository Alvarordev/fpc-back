package com.hazardev.fpc_back.shared.config

import jakarta.annotation.PostConstruct
import org.springframework.beans.factory.annotation.Value
import org.springframework.context.annotation.Configuration
import java.sql.DriverManager

@Configuration
class DatabaseMigrationFixer(
    @Value("\${spring.datasource.url}") private val dbUrl: String,
    @Value("\${spring.datasource.username}") private val dbUser: String,
    @Value("\${spring.datasource.password}") private val dbPass: String
) {
    @PostConstruct
    fun fixDatabaseSchema() {
        try {
            DriverManager.getConnection(dbUrl, dbUser, dbPass).use { conn ->
                conn.createStatement().use { stmt ->
                    stmt.execute("ALTER TABLE alerts ADD COLUMN IF NOT EXISTS under_review BOOLEAN DEFAULT FALSE;")
                    stmt.execute("UPDATE alerts SET under_review = FALSE WHERE under_review IS NULL;")
                    stmt.execute("ALTER TABLE alerts ADD COLUMN IF NOT EXISTS derived_to VARCHAR(50);")
                    stmt.execute("ALTER TABLE alerts ADD COLUMN IF NOT EXISTS derivation_notes TEXT;")
                    stmt.execute("ALTER TABLE alerts ADD COLUMN IF NOT EXISTS severity VARCHAR(20) DEFAULT 'HIGH';")
                    stmt.execute("UPDATE alerts SET severity = 'HIGH' WHERE severity IS NULL;")
                    stmt.execute("ALTER TABLE alerts ADD COLUMN IF NOT EXISTS category VARCHAR(50) DEFAULT 'GENERAL';")
                    stmt.execute("UPDATE alerts SET category = 'GENERAL' WHERE category IS NULL;")
                    stmt.execute("ALTER TABLE alerts ADD COLUMN IF NOT EXISTS ai_summary TEXT;")
                    stmt.execute("ALTER TABLE alerts ADD COLUMN IF NOT EXISTS ticket_number VARCHAR(30);")
                    stmt.execute("CREATE SEQUENCE IF NOT EXISTS alert_ticket_seq START WITH 1001 INCREMENT BY 1;")
                    
                    // Assign ticket numbers to any existing null rows
                    stmt.execute("""
                        DO $$
                        DECLARE
                            r RECORD;
                            seq_val INT;
                        BEGIN
                            FOR r IN SELECT id FROM alerts WHERE ticket_number IS NULL LOOP
                                seq_val := nextval('alert_ticket_seq');
                                UPDATE alerts SET ticket_number = 'ALT-2026-' || seq_val WHERE id = r.id;
                            END LOOP;
                        END $$;
                    """.trimIndent())

                    stmt.execute("""
                        CREATE TABLE IF NOT EXISTS alert_events (
                            id UUID PRIMARY KEY,
                            alert_id UUID NOT NULL REFERENCES alerts(id) ON DELETE CASCADE,
                            agent_id UUID NULL REFERENCES agents(id) ON DELETE SET NULL,
                            event_type VARCHAR(50) NOT NULL,
                            title VARCHAR(255) NOT NULL,
                            description TEXT NULL,
                            created_at TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP
                        );
                    """.trimIndent())
                }
            }
        } catch (e: Exception) {
            println("Database migration fix note: \${e.message}")
        }
    }
}
