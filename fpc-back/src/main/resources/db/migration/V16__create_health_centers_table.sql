CREATE TABLE IF NOT EXISTS health_centers (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    name VARCHAR(255) NOT NULL,
    slug VARCHAR(255) NOT NULL UNIQUE,
    department VARCHAR(50) NOT NULL
        CHECK (department IN (
            'AMAZONAS', 'ANCASH', 'APURIMAC', 'AREQUIPA', 'AYACUCHO',
            'CAJAMARCA', 'CALLAO', 'CUSCO', 'HUANCAVELICA', 'HUANUCO',
            'ICA', 'JUNIN', 'LA_LIBERTAD', 'LAMBAYEQUE', 'LIMA',
            'LORETO', 'MADRE_DE_DIOS', 'MOQUEGUA', 'PASCO', 'PIURA',
            'PUNO', 'SAN_MARTIN', 'TACNA', 'TUMBES', 'UCAYALI'
        )),
    is_active BOOLEAN NOT NULL DEFAULT TRUE,
    created_at TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT NOW(),
    updated_at TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT NOW()
);

CREATE INDEX IF NOT EXISTS idx_health_centers_slug ON health_centers(slug);
CREATE INDEX IF NOT EXISTS idx_health_centers_department ON health_centers(department);
