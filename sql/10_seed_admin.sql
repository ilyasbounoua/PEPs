-- ============================================================
-- Seed: Default admin user
-- Login:    admin
-- Password: admin  (BCrypt hash below)
-- Role:     admin
-- Permission: admin
--
-- This script is idempotent: it won't fail if the user already exists.
-- Run it manually to restore the admin account after a fresh volume:
--
--   docker exec peps-database psql -U <DB_USER> -d <DB_NAME> -f /docker-entrypoint-initdb.d/10_seed_admin.sql
-- ============================================================

INSERT INTO peps.users (login, password_hash, enabled, role, permission, created_at, updated_at)
VALUES (
    'admin',
    '$2a$10$addEtMm/UEHiwBiVDQ9N8OT0OLfsGAwhnu03mUz.oJ1wMvlGhlS5C',
    true,
    'admin',
    'admin',
    NOW(),
    NOW()
)
ON CONFLICT (login) DO UPDATE SET password_hash = EXCLUDED.password_hash;
