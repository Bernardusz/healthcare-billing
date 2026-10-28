CREATE TABLE IF NOT EXISTS users (
    id BIGSERIAL PRIMARY KEY,
    username VARCHAR(100) NOT NULL UNIQUE,
    email VARCHAR(255) NOT NULL UNIQUE,
    password VARCHAR(255) NOT NULL,
    role VARCHAR(50) NOT NULL DEFAULT 'ROLE_BILLER',
    created_at TIMESTAMPTZ DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT chk_user_role CHECK (role IN ('ROLE_ADMIN', 'ROLE_AUDITOR', 'ROLE_BILLER'))
);

CREATE TABLE IF NOT EXISTS refresh_tokens (
    id BIGSERIAL PRIMARY KEY,
    user_id BIGINT NOT NULL REFERENCES users(id) ON DELETE CASCADE,
    token_hash VARCHAR(64) NOT NULL UNIQUE, -- UUID Is already long, so we'll hash it using light algorithm
    expires_at TIMESTAMPTZ NOT NULL,
    created_at TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP,
    revoked BOOLEAN NOT NULL DEFAULT FALSE -- We won't delete Refresh Token immediately due to security logs
    -- we'll revoke and schedule will delete revoked tokens every defined time
);

-- Fast token lookups during refresh requests
CREATE UNIQUE INDEX IF NOT EXISTS idx_refresh_tokens_hash ON refresh_tokens(token_hash);

-- Fast user lookups (e.g., revoking all tokens for a user upon password reset)
CREATE INDEX IF NOT EXISTS idx_refresh_tokens_user_id ON refresh_tokens(user_id);

-- Speeds up scheduled cleanup queries for expired or revoked tokens
CREATE INDEX IF NOT EXISTS idx_refresh_tokens_cleanup ON refresh_tokens(expires_at, revoked);

---

-- -----------------------------------------------------------------------------
-- 1. PATIENTS TABLE
-- -----------------------------------------------------------------------------
CREATE TABLE IF NOT EXISTS patients (
    id BIGSERIAL PRIMARY KEY,
    patient_number VARCHAR(50) NOT NULL UNIQUE,
    first_name VARCHAR(100) NOT NULL,
    last_name VARCHAR(100) NOT NULL,
    date_of_birth DATE NOT NULL,
    insurance_provider VARCHAR(100) NOT NULL,
    policy_number VARCHAR(100) NOT NULL,
    created_at TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP
);

-- -----------------------------------------------------------------------------
-- 2. MEDICAL CLAIMS TABLE
-- -----------------------------------------------------------------------------
CREATE TABLE IF NOT EXISTS medical_claims (
    id BIGSERIAL PRIMARY KEY,
    claim_number VARCHAR(50) NOT NULL UNIQUE,
    patient_id BIGINT NOT NULL REFERENCES patients(id) ON DELETE CASCADE,
    biller_id BIGINT NOT NULL REFERENCES users(id),

    -- Medical Coding Standards
    icd_code VARCHAR(20) NOT NULL,      -- e.g., 'E11.9' (Type 2 Diabetes)
    cpt_code VARCHAR(20) NOT NULL,      -- e.g., '99214' (Office Visit)

    amount DECIMAL(12, 2) NOT NULL,
    status VARCHAR(30) NOT NULL DEFAULT 'PENDING',
    flagged_for_audit BOOLEAN NOT NULL DEFAULT FALSE,
    created_at TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP,

    CONSTRAINT chk_claim_status CHECK (status IN ('PENDING', 'APPROVED', 'REJECTED', 'FLAGGED', 'UNDER_AUDIT'))
);

-- -----------------------------------------------------------------------------
-- 3. AUDIT LOGS TABLE
-- -----------------------------------------------------------------------------
CREATE TABLE IF NOT EXISTS audit_logs (
    id BIGSERIAL PRIMARY KEY,
    claim_id BIGINT NOT NULL REFERENCES medical_claims(id) ON DELETE CASCADE,
    auditor_id BIGINT NOT NULL REFERENCES users(id),
    action VARCHAR(50) NOT NULL,         -- e.g., 'FLAGGED_UNBUNDLING', 'APPROVED_CLAIM'
    notes TEXT,
    flagged_reason VARCHAR(255),
    created_at TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP
);

-- Indexes for audit query performance
CREATE INDEX IF NOT EXISTS idx_claims_status ON medical_claims(status);
CREATE INDEX IF NOT EXISTS idx_claims_flagged ON medical_claims(flagged_for_audit);
CREATE INDEX IF NOT EXISTS idx_claims_patient ON medical_claims(patient_id);

ALTER TABLE users
    DROP CONSTRAINT IF EXISTS users_username_key,
    DROP CONSTRAINT IF EXISTS users_email_key;

ALTER TABLE users
    ADD CONSTRAINT uk_users_username UNIQUE (username),
    ADD CONSTRAINT uk_users_email UNIQUE (email);

ALTER TABLE patients
    DROP CONSTRAINT IF EXISTS patients_patient_number_key;

ALTER TABLE patients
    ADD CONSTRAINT uk_patients_patient_number UNIQUE (patient_number);