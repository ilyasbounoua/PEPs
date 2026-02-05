-- ============================================================
-- SCRIPT DE DONNÉES TEST POUR L'ARCHIVE DU JOURNAL D'AUDIT
-- Crée des entrées d'audit anciennes (plus de 3 mois) pour tester
-- le système d'archivage
-- 
-- Date du script: 2026-01-30
-- Période actuelle (non archivable): Novembre 2025 - Janvier 2026
-- Périodes archivables: Avant Novembre 2025
-- 
-- @author Anas EL HOUDI
-- ============================================================

-- ============================================================
-- PÉRIODE 1: Août - Octobre 2025 (2025-08)
-- 3 entrées d'audit
-- ============================================================

INSERT INTO peps.audit_logs (action, entity_type, entity_id, entity_name, entity_role, user_login, timestamp, old_value, new_value, details)
VALUES 
('CREATE', 'module', 1, 'Module Test Août', 'aras', 'admin', '2025-08-15 10:30:00', NULL, '{"nom": "Module Test Août", "status": "actif"}', 'Création d''un nouveau module pour le profil aras'),
('UPDATE', 'sound', 2, 'Son Ambiance Forêt', 'dauphin', 'editor_dauphin', '2025-09-10 14:45:00', '{"volume": 50}', '{"volume": 75}', 'Modification du volume de 50 à 75'),
('DELETE', 'module', 3, 'Module Obsolète', 'aras', 'admin', '2025-10-05 09:15:00', '{"nom": "Module Obsolète", "status": "inactif"}', NULL, 'Suppression du module obsolète');


-- ============================================================
-- PÉRIODE 2: Mai - Juillet 2025 (2025-05)
-- 3 entrées d'audit
-- ============================================================

INSERT INTO peps.audit_logs (action, entity_type, entity_id, entity_name, entity_role, user_login, timestamp, old_value, new_value, details)
VALUES 
('CREATE', 'sound', 10, 'Nouveau Son Tropical', 'aras', 'editor_aras', '2025-05-12 13:20:00', NULL, '{"nom": "Nouveau Son Tropical", "type_son": "Ambiance"}', 'Ajout d''un nouveau son d''ambiance tropicale'),
('UPDATE', 'module', 5, 'Module Volière', 'aras', 'admin', '2025-06-18 15:00:00', '{"current_mode": "Manuel"}', '{"current_mode": "Automatique"}', 'Passage du mode manuel au mode automatique'),
('CREATE', 'user', 15, 'viewer_test', NULL, 'admin', '2025-07-22 10:10:00', NULL, '{"login": "viewer_test", "permission": "viewer"}', 'Création d''un nouveau compte utilisateur avec permission viewer');


-- ============================================================
-- PÉRIODE 3: Février - Avril 2025 (2025-02)
-- 3 entrées d'audit
-- ============================================================

INSERT INTO peps.audit_logs (action, entity_type, entity_id, entity_name, entity_role, user_login, timestamp, old_value, new_value, details)
VALUES 
('UPDATE', 'user', 3, 'editor_dauphin', NULL, 'admin', '2025-02-10 08:00:00', '{"permission": "viewer"}', '{"permission": "editor"}', 'Promotion de l''utilisateur de viewer à editor'),
('DELETE', 'sound', 8, 'Son Ancien', 'dauphin', 'editor_dauphin', '2025-03-15 12:30:00', '{"nom": "Son Ancien", "type_son": "Vocal"}', NULL, 'Suppression d''un son vocal obsolète'),
('CREATE', 'module', 12, 'Module Piscine Secondaire', 'dauphin', 'admin', '2025-04-20 17:45:00', NULL, '{"nom": "Module Piscine Secondaire", "status": "actif", "ip_adress": "192.168.2.20"}', 'Installation d''un nouveau module dans la piscine secondaire');


-- ============================================================
-- VÉRIFICATION DES DONNÉES D'AUDIT ARCHIVABLES
-- ============================================================

-- Afficher toutes les entrées d'audit par période
SELECT 
    'Période' as type,
    CASE 
        WHEN timestamp >= '2025-08-01' AND timestamp < '2025-11-01' THEN 'Août - Octobre 2025'
        WHEN timestamp >= '2025-05-01' AND timestamp < '2025-08-01' THEN 'Mai - Juillet 2025'
        WHEN timestamp >= '2025-02-01' AND timestamp < '2025-05-01' THEN 'Février - Avril 2025'
        ELSE 'Autre période'
    END as periode,
    COUNT(*) as nb_entrees
FROM peps.audit_logs
WHERE timestamp < NOW() - INTERVAL '3 months'
GROUP BY periode
ORDER BY MIN(timestamp);

-- Afficher les détails des entrées d'audit archivables
SELECT 
    id,
    action,
    entity_type,
    entity_name,
    user_login,
    timestamp,
    details
FROM peps.audit_logs
WHERE timestamp < NOW() - INTERVAL '3 months'
ORDER BY timestamp;

-- Compter le total
SELECT 
    'Total entrées d''audit archivables' as description,
    COUNT(*) as count
FROM peps.audit_logs
WHERE timestamp < NOW() - INTERVAL '3 months';
