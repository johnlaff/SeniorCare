CREATE TABLE IF NOT EXISTS elderly_caregiver (
                                                 id UUID PRIMARY KEY,
                                                 organization_id UUID NOT NULL,
                                                 elderly_id UUID NOT NULL,
                                                 caregiver_id UUID NOT NULL,
                                                 created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
                                                 CONSTRAINT fk_elderly_caregiver_organization FOREIGN KEY (organization_id)
    REFERENCES organizations(id),
    CONSTRAINT fk_elderly_caregiver_elderly FOREIGN KEY (elderly_id)
    REFERENCES elderly(id),
    CONSTRAINT fk_elderly_caregiver_caregiver FOREIGN KEY (caregiver_id)
    REFERENCES caregiver(id),
    CONSTRAINT uk_elderly_caregiver UNIQUE (elderly_id, caregiver_id)
    );