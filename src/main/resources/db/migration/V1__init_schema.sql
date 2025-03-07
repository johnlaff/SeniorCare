-- Criação do schema inicial com suporte multi-tenant

-- 1. Tabela organizations
CREATE TABLE IF NOT EXISTS organizations (
                                             id UUID PRIMARY KEY,
                                             name VARCHAR(150) NOT NULL,
    domain VARCHAR(100) NOT NULL,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP
    );

-- 2. Tabela users
CREATE TABLE IF NOT EXISTS users (
                                     id UUID PRIMARY KEY,
                                     organization_id UUID NOT NULL,
                                     name VARCHAR(100) NOT NULL,
    email VARCHAR(100) NOT NULL,
    password VARCHAR(255) NOT NULL,
    role VARCHAR(20) NOT NULL,  -- Valores esperados: 'ADMIN', 'CAREGIVER', 'FAMILY'
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT fk_users_organization FOREIGN KEY (organization_id)
    REFERENCES organizations(id)
    );

-- 3. Tabela elderly
CREATE TABLE IF NOT EXISTS elderly (
                                       id UUID PRIMARY KEY,
                                       organization_id UUID NOT NULL,
                                       name VARCHAR(100) NOT NULL,
    birth_date DATE NOT NULL,
    emergency_contact VARCHAR(100),
    address TEXT,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT fk_elderly_organization FOREIGN KEY (organization_id)
    REFERENCES organizations(id)
    );

-- 4. Tabela caregiver
CREATE TABLE IF NOT EXISTS caregiver (
                                         id UUID PRIMARY KEY,
                                         organization_id UUID NOT NULL,
                                         user_id UUID NOT NULL,
                                         specialty VARCHAR(100),
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT fk_caregiver_organization FOREIGN KEY (organization_id)
    REFERENCES organizations(id),
    CONSTRAINT fk_caregiver_user FOREIGN KEY (user_id)
    REFERENCES users(id)
    );

-- 5. Tabela family_members
CREATE TABLE IF NOT EXISTS family_members (
                                              id UUID PRIMARY KEY,
                                              organization_id UUID NOT NULL,
                                              user_id UUID NOT NULL,
                                              elderly_id UUID NOT NULL,
                                              relationship VARCHAR(20) NOT NULL,  -- Valores esperados: 'FILHO', 'NETO', 'SOBRINHO', 'OUTRO'
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT fk_family_members_organization FOREIGN KEY (organization_id)
    REFERENCES organizations(id),
    CONSTRAINT fk_family_members_user FOREIGN KEY (user_id)
    REFERENCES users(id),
    CONSTRAINT fk_family_members_elderly FOREIGN KEY (elderly_id)
    REFERENCES elderly(id)
    );

-- 6. Tabela appointments
CREATE TABLE IF NOT EXISTS appointments (
                                            id UUID PRIMARY KEY,
                                            organization_id UUID NOT NULL,
                                            elderly_id UUID NOT NULL,
                                            caregiver_id UUID NOT NULL,
                                            date_time TIMESTAMP NOT NULL,
                                            description TEXT,
                                            created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
                                            CONSTRAINT fk_appointments_organization FOREIGN KEY (organization_id)
    REFERENCES organizations(id),
    CONSTRAINT fk_appointments_elderly FOREIGN KEY (elderly_id)
    REFERENCES elderly(id),
    CONSTRAINT fk_appointments_caregiver FOREIGN KEY (caregiver_id)
    REFERENCES caregiver(id)
    );

-- 7. Tabela medical_history
CREATE TABLE IF NOT EXISTS medical_history (
                                               id UUID PRIMARY KEY,
                                               organization_id UUID NOT NULL,
                                               elderly_id UUID NOT NULL,
                                               condition TEXT,
                                               date_recorded TIMESTAMP,
                                               created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
                                               CONSTRAINT fk_medical_history_organization FOREIGN KEY (organization_id)
    REFERENCES organizations(id),
    CONSTRAINT fk_medical_history_elderly FOREIGN KEY (elderly_id)
    REFERENCES elderly(id)
    );

-- 8. Tabela medications
CREATE TABLE IF NOT EXISTS medications (
                                           id UUID PRIMARY KEY,
                                           organization_id UUID NOT NULL,
                                           elderly_id UUID NOT NULL,
                                           name VARCHAR(100) NOT NULL,
    dosage VARCHAR(100),
    frequency VARCHAR(50),
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT fk_medications_organization FOREIGN KEY (organization_id)
    REFERENCES organizations(id),
    CONSTRAINT fk_medications_elderly FOREIGN KEY (elderly_id)
    REFERENCES elderly(id)
    );

-- 9. Tabela notifications
CREATE TABLE IF NOT EXISTS notifications (
                                             id UUID PRIMARY KEY,
                                             organization_id UUID NOT NULL,
                                             user_id UUID NOT NULL,
                                             message TEXT,
                                             created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
                                             status VARCHAR(20) NOT NULL,  -- Valores esperados: 'PENDENTE', 'LIDA'
    CONSTRAINT fk_notifications_organization FOREIGN KEY (organization_id)
    REFERENCES organizations(id),
    CONSTRAINT fk_notifications_user FOREIGN KEY (user_id)
    REFERENCES users(id)
    );

-- 10. Tabela documents
CREATE TABLE IF NOT EXISTS documents (
                                         id UUID PRIMARY KEY,
                                         organization_id UUID NOT NULL,
                                         elderly_id UUID NOT NULL,
                                         file_path VARCHAR(255),
    document_type VARCHAR(50),
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT fk_documents_organization FOREIGN KEY (organization_id)
    REFERENCES organizations(id),
    CONSTRAINT fk_documents_elderly FOREIGN KEY (elderly_id)
    REFERENCES elderly(id)
    );

-- 11. Tabela audit_logs
CREATE TABLE IF NOT EXISTS audit_logs (
                                          id UUID PRIMARY KEY,
                                          organization_id UUID NOT NULL,
                                          user_id UUID NOT NULL,
                                          action VARCHAR(100) NOT NULL,
    entity_name VARCHAR(50) NOT NULL,
    entity_id UUID,
    timestamp TIMESTAMP NOT NULL,
    description TEXT,
    CONSTRAINT fk_audit_logs_organization FOREIGN KEY (organization_id)
    REFERENCES organizations(id),
    CONSTRAINT fk_audit_logs_user FOREIGN KEY (user_id)
    REFERENCES users(id)
    );
