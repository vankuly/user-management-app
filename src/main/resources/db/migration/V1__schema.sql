-- ============================================================
-- V1: Schema
-- ============================================================

CREATE TYPE user_role AS ENUM ('USER', 'ADMIN');

CREATE TABLE users (
    id         BIGSERIAL    PRIMARY KEY,
    name       VARCHAR(255) NOT NULL,
    email      VARCHAR(255) NOT NULL UNIQUE,
    password   VARCHAR(255) NOT NULL,
    role       user_role    NOT NULL DEFAULT 'USER',
    created_at TIMESTAMPTZ  NOT NULL DEFAULT NOW(),
    updated_at TIMESTAMPTZ  NOT NULL DEFAULT NOW()
);

CREATE INDEX idx_users_name  ON users (LOWER(name));
CREATE INDEX idx_users_email ON users (LOWER(email));

CREATE TABLE audit_logs (
    id               BIGSERIAL    PRIMARY KEY,
    action           VARCHAR(50)  NOT NULL,   -- CREATE | UPDATE | DELETE
    target_user_id   BIGINT       NOT NULL,
    target_user_email VARCHAR(255) NOT NULL,
    performed_by     VARCHAR(255) NOT NULL,
    details          TEXT,
    created_at       TIMESTAMPTZ  NOT NULL DEFAULT NOW()
);

CREATE INDEX idx_audit_target  ON audit_logs (target_user_id);
CREATE INDEX idx_audit_created ON audit_logs (created_at DESC);
