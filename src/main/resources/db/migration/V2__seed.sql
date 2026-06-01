-- ============================================================
-- V2: Seed Data – 500 test users
--
-- All seed users share password "password"
-- BCrypt(cost=10) hash: $2a$10$N9qo8uLOickgx2ZMRZoMyeIjZAgcfl7p92ldGxad68LJZdL17lhWy
--
-- Default admin/user accounts are created by DataInitializer.kt
-- on first startup (BCrypt-encoded at runtime).
-- ============================================================
DO $$
DECLARE
    i   INTEGER;
    ts  TIMESTAMPTZ;
    ts2 TIMESTAMPTZ;
BEGIN
    FOR i IN 1..500 LOOP
        ts  := NOW() - (random() * INTERVAL '365 days');
        ts2 := ts    + (random() * INTERVAL '30 days');
        IF ts2 > NOW() THEN ts2 := NOW(); END IF;

        INSERT INTO users (name, email, password, role, created_at, updated_at)
        VALUES (
            'Seed User ' || i,
            'seed' || i || '@example.com',
            '$2a$10$N9qo8uLOickgx2ZMRZoMyeIjZAgcfl7p92ldGxad68LJZdL17lhWy',
            'USER',
            ts,
            ts2
        )
        ON CONFLICT (email) DO NOTHING;
    END LOOP;
END;
$$;
