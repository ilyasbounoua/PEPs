-- ============================================================
-- Restore: Seed data compatible with current Java schema
-- 
-- User.java entity columns:
--   id_user (SERIAL PK), login, password_hash, enabled,
--   role, permission, created_at, updated_at
--
-- Roles:  admin | dauphin | aras
-- Perms:  admin | editor  | viewer
--
-- BCrypt hashes generated with BCryptPasswordEncoder (strength 10):
--   admin -> $2a$10$N9qo8uLOickgx2ZMRZoMyeIjZAgcfl7p92ldGxad68LJZdL17lhWy
--
-- This script is IDEMPOTENT: safe to run multiple times.
-- ============================================================

SET session_replication_role = 'replica';
SET statement_timeout = 0;
SET client_encoding = 'UTF8';
SET standard_conforming_strings = on;
SET row_security = off;

-- ============================================================
-- Users
-- ============================================================

INSERT INTO peps.users (login, password_hash, enabled, role, permission, created_at, updated_at)
VALUES
    ('dauphin', '$2a$10$N9qo8uLOickgx2ZMRZoMyeIjZAgcfl7p92ldGxad68LJZdL17lhWy', true, 'dauphin', 'editor', NOW(), NOW()),
    ('aras',    '$2a$10$N9qo8uLOickgx2ZMRZoMyeIjZAgcfl7p92ldGxad68LJZdL17lhWy', true, 'aras',    'editor', NOW(), NOW())
ON CONFLICT (login) DO NOTHING;

-- ============================================================
-- Modules
-- ============================================================
INSERT INTO peps.module (idmodule, nom, ip_adress, status, volume, current_mode, actif, last_seen, owner_role, version) VALUES
    (1, 'Module Bassin Principal',    '192.168.2.10', 'actif',   90, 'Automatique', true,  '2026-01-21 15:23:40.923426', 'dauphin', 0),
    (2, 'Module Zone Repos',          '192.168.2.11', 'actif',   50, 'Manuel',      true,  '2026-01-21 15:13:40.923426', 'dauphin', 0),
    (3, 'Module Piscine Entrainement','192.168.2.12', 'actif',   70, 'Automatique', true,  '2026-01-21 15:27:40.923426', 'dauphin', 0),
    (4, 'Module Volière Ara',         '192.168.3.10', 'actif',   75, 'Automatique', true,  '2026-01-30 11:10:19.392',    'aras',    0),
    (5, 'Module Perchoir Ara',        '192.168.3.11', 'actif',   60, 'Manuel',      true,  '2026-01-21 15:53:44.659',    'aras',    0),
    (6, 'Module Cage Principale',     '192.168.3.12', 'actif',   85, 'Automatique', true,  '2026-01-21 15:23:40.923426', 'aras',    0),
    (7, 'GESTION ARAS',               '192.168.1.1',  'inactif', 50, 'Manuel',      false, '2026-01-21 15:36:34.174',    'aras',    0)
ON CONFLICT (idmodule) DO NOTHING;

-- ============================================================
-- Sounds
-- ============================================================
INSERT INTO peps.sound (idsound, nom, type_son, extension, chemin, owner_role, version) VALUES
    (1, 'Chant des Baleines',  'Naturelle', 'mp3', NULL,                                   'dauphin', 0),
    (2, 'Clics de Dauphin',    'Vocal',     'wav', NULL,                                   'dauphin', 0),
    (3, 'Vagues Océan',        'Ambiance',  'mp3', NULL,                                   'dauphin', 0),
    (4, 'Sonar Marin',         'Vocal',     'mp3', NULL,                                   'dauphin', 0),
    (5, 'Musique Aquatique',   'Ambiance',  'wav', NULL,                                   'dauphin', 0),
    (6, 'Cri Ara Bleu',        'Vocal',     'mp3', NULL,                                   'aras',    0),
    (7, 'Musique Tropicale',   'Ambiance',  'mp3', NULL,                                   'aras',    0),
    (8, 'Pluie Amazonienne',   'Naturel',   'wav', NULL,                                   'aras',    0),
    (9, 'Chant Perroquet',     'Vocal',     'mp3', NULL,                                   'aras',    0),
    (10,'Foret Tropicale',     'Ambiance',  'wav', NULL,                                   'aras',    0)
ON CONFLICT (idsound) DO NOTHING;

-- ============================================================
-- Interactions
-- ============================================================
INSERT INTO peps.interaction (idinteraction, idsound, idmodule, typeinteraction, time_lancement, owner_role) VALUES
    (1, 1, 1, 'Head', '2026-01-21 15:18:40.923426', 'dauphin'),
    (2, 2, 1, 'Tail', '2026-01-21 15:20:40.923426', 'dauphin'),
    (3, 6, 4, 'Bec',  '2026-01-21 15:08:40.923426', 'aras'),
    (4, 7, 5, 'Patte','2026-01-21 15:13:40.923426', 'aras')
ON CONFLICT (idinteraction) DO NOTHING;

-- ============================================================
-- Sequences: advance past the seeded IDs
-- ============================================================
SELECT pg_catalog.setval('peps.module_idmodule_seq',       10, true);
SELECT pg_catalog.setval('peps.sound_idsound_seq',         11, true);
SELECT pg_catalog.setval('peps.interaction_idinteraction_seq', 5, true);
SELECT pg_catalog.setval('peps.users_id_user_seq',         4,  true);

SET session_replication_role = 'origin';
