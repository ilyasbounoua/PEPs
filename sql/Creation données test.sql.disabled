-- ============================================================
-- SCRIPT DE NETTOYAGE ET CONFIGURATION ADMIN SUPERVISION
-- Admin (id=1) n'est PAS un animal : il ne possède pas de données
-- Il supervise les données de tous les utilisateurs (dauphin, aras)
-- 
-- MISE À JOUR: Utilise owner_role au lieu de owner_id
-- @author Anas EL HOUDI
-- ============================================================

-- 1. SUPPRESSION DES DONNEES ADMIN (owner_role = 'admin')
-- Admin n'est pas un animal, il supervise uniquement

DELETE FROM peps.Interaction WHERE owner_role = 'admin';
DELETE FROM peps.Sound WHERE owner_role = 'admin';
DELETE FROM peps.Module WHERE owner_role = 'admin';

-- 2. DONNEES TEST POUR DAUPHIN (owner_role = 'dauphin')
-- TypeInteraction : "Head" et "Tail" (spécifique au dauphin)
-- @author Anas EL HOUDI

-- Sons pour Dauphin
INSERT INTO peps.Sound (nom, type_son, extension, owner_role) VALUES
('Chant des Baleines', 'Naturel', 'mp3', 'dauphin'),
('Clics de Dauphin', 'Vocal', 'wav', 'dauphin'),
('Vagues Océan', 'Ambiance', 'mp3', 'dauphin'),
('Sonar Marin', 'Vocal', 'mp3', 'dauphin'),
('Musique Aquatique', 'Ambiance', 'wav', 'dauphin')
ON CONFLICT DO NOTHING;

-- Modules pour Dauphin
INSERT INTO peps.Module (nom, ip_adress, status, volume, current_mode, actif, last_seen, owner_role) VALUES
('Module Bassin Principal', '192.168.2.10', 'actif', 90, 'Automatique', true, NOW() - INTERVAL '5 minutes', 'dauphin'),
('Module Zone Repos', '192.168.2.11', 'actif', 50, 'Manuel', true, NOW() - INTERVAL '15 minutes', 'dauphin'),
('Module Piscine Entrainement', '192.168.2.12', 'actif', 70, 'Automatique', true, NOW() - INTERVAL '1 minute', 'dauphin')
ON CONFLICT DO NOTHING;

-- Interactions pour Dauphin (typeInteraction: Head / Tail)
INSERT INTO peps.Interaction (idsound, idmodule, typeInteraction, time_lancement, owner_role)
SELECT s.idsound, m.idmodule, 'Head', NOW() - INTERVAL '10 minutes', 'dauphin'
FROM peps.Sound s, peps.Module m
WHERE s.nom = 'Chant des Baleines' AND s.owner_role = 'dauphin' AND m.nom = 'Module Bassin Principal' AND m.owner_role = 'dauphin';

INSERT INTO peps.Interaction (idsound, idmodule, typeInteraction, time_lancement, owner_role)
SELECT s.idsound, m.idmodule, 'Tail', NOW() - INTERVAL '8 minutes', 'dauphin'
FROM peps.Sound s, peps.Module m
WHERE s.nom = 'Clics de Dauphin' AND s.owner_role = 'dauphin' AND m.nom = 'Module Bassin Principal' AND m.owner_role = 'dauphin';

INSERT INTO peps.Interaction (idsound, idmodule, typeInteraction, time_lancement, owner_role)
SELECT s.idsound, m.idmodule, 'Head', NOW() - INTERVAL '3 minutes', 'dauphin'
FROM peps.Sound s, peps.Module m
WHERE s.nom = 'Vagues Océan' AND s.owner_role = 'dauphin' AND m.nom = 'Module Zone Repos' AND m.owner_role = 'dauphin';

INSERT INTO peps.Interaction (idsound, idmodule, typeInteraction, time_lancement, owner_role)
SELECT s.idsound, m.idmodule, 'Tail', NOW() - INTERVAL '1 minute', 'dauphin'
FROM peps.Sound s, peps.Module m
WHERE s.nom = 'Sonar Marin' AND s.owner_role = 'dauphin' AND m.nom = 'Module Piscine Entrainement' AND m.owner_role = 'dauphin';

INSERT INTO peps.Interaction (idsound, idmodule, typeInteraction, time_lancement, owner_role)
SELECT s.idsound, m.idmodule, 'Head', NOW() - INTERVAL '30 seconds', 'dauphin'
FROM peps.Sound s, peps.Module m
WHERE s.nom = 'Musique Aquatique' AND s.owner_role = 'dauphin' AND m.nom = 'Module Bassin Principal' AND m.owner_role = 'dauphin';

INSERT INTO peps.Interaction (idsound, idmodule, typeInteraction, time_lancement, owner_role)
SELECT s.idsound, m.idmodule, 'Tail', NOW() - INTERVAL '15 seconds', 'dauphin'
FROM peps.Sound s, peps.Module m
WHERE s.nom = 'Vagues Océan' AND s.owner_role = 'dauphin' AND m.nom = 'Module Piscine Entrainement' AND m.owner_role = 'dauphin';


-- ============================================================
-- DONNEES TEST POUR ARAS (owner_role = 'aras')
-- TypeInteraction : "Bec" et "Patte" (spécifique aux perroquets)
-- @author Anas EL HOUDI
-- ============================================================

-- Sons pour Aras
INSERT INTO peps.Sound (nom, type_son, extension, owner_role) VALUES
('Cri Ara Bleu', 'Vocal', 'mp3', 'aras'),
('Musique Tropicale', 'Ambiance', 'mp3', 'aras'),
('Pluie Amazonienne', 'Naturel', 'wav', 'aras'),
('Chant Perroquet', 'Vocal', 'mp3', 'aras'),
('Foret Tropicale', 'Ambiance', 'wav', 'aras')
ON CONFLICT DO NOTHING;

-- Modules pour Aras
INSERT INTO peps.Module (nom, ip_adress, status, volume, current_mode, actif, last_seen, owner_role) VALUES
('Module Volière Ara', '192.168.3.10', 'actif', 75, 'Automatique', true, NOW() - INTERVAL '2 minutes', 'aras'),
('Module Perchoir Ara', '192.168.3.11', 'inactif', 60, 'Manuel', false, NOW() - INTERVAL '1 hour', 'aras'),
('Module Cage Principale', '192.168.3.12', 'actif', 85, 'Automatique', true, NOW() - INTERVAL '5 minutes', 'aras')
ON CONFLICT DO NOTHING;

-- Interactions pour Aras (typeInteraction: Bec / Patte)
INSERT INTO peps.Interaction (idsound, idmodule, typeInteraction, time_lancement, owner_role)
SELECT s.idsound, m.idmodule, 'Bec', NOW() - INTERVAL '20 minutes', 'aras'
FROM peps.Sound s, peps.Module m
WHERE s.nom = 'Cri Ara Bleu' AND s.owner_role = 'aras' AND m.nom = 'Module Volière Ara' AND m.owner_role = 'aras';

INSERT INTO peps.Interaction (idsound, idmodule, typeInteraction, time_lancement, owner_role)
SELECT s.idsound, m.idmodule, 'Patte', NOW() - INTERVAL '15 minutes', 'aras'
FROM peps.Sound s, peps.Module m
WHERE s.nom = 'Musique Tropicale' AND s.owner_role = 'aras' AND m.nom = 'Module Perchoir Ara' AND m.owner_role = 'aras';

INSERT INTO peps.Interaction (idsound, idmodule, typeInteraction, time_lancement, owner_role)
SELECT s.idsound, m.idmodule, 'Bec', NOW() - INTERVAL '5 minutes', 'aras'
FROM peps.Sound s, peps.Module m
WHERE s.nom = 'Pluie Amazonienne' AND s.owner_role = 'aras' AND m.nom = 'Module Volière Ara' AND m.owner_role = 'aras';

INSERT INTO peps.Interaction (idsound, idmodule, typeInteraction, time_lancement, owner_role)
SELECT s.idsound, m.idmodule, 'Patte', NOW() - INTERVAL '2 minutes', 'aras'
FROM peps.Sound s, peps.Module m
WHERE s.nom = 'Chant Perroquet' AND s.owner_role = 'aras' AND m.nom = 'Module Cage Principale' AND m.owner_role = 'aras';


-- ============================================================
-- VERIFICATION DES DONNEES
-- ============================================================

-- Vérifier que l'admin n'a plus de données personnelles
SELECT 'Interactions par role' as check_type, owner_role, COUNT(*) as total FROM peps.Interaction GROUP BY owner_role;
SELECT 'Modules par role' as check_type, owner_role, COUNT(*) as total FROM peps.Module GROUP BY owner_role;
SELECT 'Sons par role' as check_type, owner_role, COUNT(*) as total FROM peps.Sound GROUP BY owner_role;
