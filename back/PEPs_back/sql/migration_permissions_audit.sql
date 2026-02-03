-- ============================================
-- PEP'S: Script de migration pour Permissions & Audit Log
-- Exécuter dans pgAdmin ou psql
-- ============================================

-- 1. Ajouter colonne permission à la table users
ALTER TABLE users ADD COLUMN IF NOT EXISTS permission VARCHAR(20) NOT NULL DEFAULT 'viewer';

-- 2. Migration: admins existants → permission 'admin', autres → 'viewer'
UPDATE users SET permission = 'admin' WHERE role = 'admin';
UPDATE users SET permission = 'viewer' WHERE role != 'admin' AND permission = 'viewer';

-- 3. Créer la table audit_logs
CREATE TABLE IF NOT EXISTS audit_logs (
    id SERIAL PRIMARY KEY,
    action VARCHAR(20) NOT NULL,              -- 'CREATE', 'UPDATE', 'DELETE'
    entity_type VARCHAR(50) NOT NULL,         -- 'module', 'sound', 'user'
    entity_id INTEGER,                        -- ID de l'entité
    entity_name VARCHAR(255),                 -- Nom de l'entité
    entity_role VARCHAR(50),                  -- Rôle cible (dauphin, aras, etc.)
    user_login VARCHAR(100) NOT NULL,         -- Login de l'utilisateur
    timestamp TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    old_value TEXT,                           -- Valeur avant (JSON)
    new_value TEXT,                           -- Valeur après (JSON)
    details TEXT                              -- Description courte
);

-- Index pour optimiser les recherches
CREATE INDEX IF NOT EXISTS idx_audit_timestamp ON audit_logs(timestamp DESC);
CREATE INDEX IF NOT EXISTS idx_audit_user ON audit_logs(user_login);
CREATE INDEX IF NOT EXISTS idx_audit_entity ON audit_logs(entity_type, entity_id);

-- 4. Ajouter colonne version pour optimistic locking
ALTER TABLE modules ADD COLUMN IF NOT EXISTS version INTEGER NOT NULL DEFAULT 0;
ALTER TABLE sounds ADD COLUMN IF NOT EXISTS version INTEGER NOT NULL DEFAULT 0;

-- Vérification
SELECT 'Migration terminée!' AS status;
SELECT column_name, data_type FROM information_schema.columns WHERE table_name = 'users' AND column_name = 'permission';
SELECT column_name, data_type FROM information_schema.columns WHERE table_name = 'audit_logs';
