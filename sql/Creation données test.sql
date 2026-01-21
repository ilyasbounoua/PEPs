-- ============================================================
-- SCRIPT DE NETTOYAGE ET CONFIGURATION ADMIN SUPERVISION
-- ============================================================

-- 1. SUPPRESSION DES DONNEES ADMIN (owner_id = 1)
DELETE FROM peps.Interaction WHERE owner_id = 1;
DELETE FROM peps.Sound WHERE owner_id = 1;
DELETE FROM peps.Module WHERE owner_id = 1;

-- ============================================================
-- DONNEES TEST POUR DAUPHIN (id_user = 3)
-- ============================================================

-- Sons pour Dauphin
INSERT INTO peps.Sound (nom, type_son, extension, owner_id) VALUES
('Chant des Baleines', 'Naturel', 'mp3', 3),
('Clics de Dauphin', 'Vocal', 'wav', 3),
('Vagues Océan', 'Ambiance', 'mp3', 3),
('Sonar Marin', 'Vocal', 'mp3', 3),
('Musique Aquatique', 'Ambiance', 'wav', 3)
ON CONFLICT DO NOTHING;

-- Modules pour Dauphin
INSERT INTO peps.Module (nom, ip_adress, status, volume, current_mode, actif, last_seen, owner_id) VALUES
('Module Bassin Principal', '192.168.2.10', 'actif', 90, 'Automatique', true, NOW() - INTERVAL '5 minutes', 3),
('Module Zone Repos', '192.168.2.11', 'actif', 50, 'Manuel', true, NOW() - INTERVAL '15 minutes', 3),
('Module Piscine Entrainement', '192.168.2.12', 'actif', 70, 'Automatique', true, NOW() - INTERVAL '1 minute', 3)
ON CONFLICT DO NOTHING;

-- Interactions Dauphin
INSERT INTO peps.Interaction (idsound, idmodule, typeInteraction, time_lancement, owner_id)
SELECT s.idsound, m.idmodule, 'Head', NOW() - INTERVAL '10 minutes', 3
FROM peps.Sound s
JOIN peps.Module m ON m.owner_id = 3
WHERE s.nom = 'Chant des Baleines' AND s.owner_id = 3
  AND m.nom = 'Module Bassin Principal';

INSERT INTO peps.Interaction (idsound, idmodule, typeInteraction, time_lancement, owner_id)
SELECT s.idsound, m.idmodule, 'Tail', NOW() - INTERVAL '8 minutes', 3
FROM peps.Sound s
JOIN peps.Module m ON m.owner_id = 3
WHERE s.nom = 'Clics de Dauphin'
  AND m.nom = 'Module Bassin Principal';

-- ============================================================
-- DONNEES TEST POUR ARAS (id_user = 2)
-- ============================================================

-- Sons pour Aras
INSERT INTO peps.Sound (nom, type_son, extension, owner_id) VALUES
('Cri Ara Bleu', 'Vocal', 'mp3', 2),
('Musique Tropicale', 'Ambiance', 'mp3', 2),
('Pluie Amazonienne', 'Naturel', 'wav', 2),
('Chant Perroquet', 'Vocal', 'mp3', 2),
('Foret Tropicale', 'Ambiance', 'wav', 2)
ON CONFLICT DO NOTHING;

-- Modules pour Aras
INSERT INTO peps.Module (nom, ip_adress, status, volume, current_mode, actif, last_seen, owner_id) VALUES
('Module Volière Ara', '192.168.3.10', 'actif', 75, 'Automatique', true, NOW() - INTERVAL '2 minutes', 2),
('Module Perchoir Ara', '192.168.3.11', 'inactif', 60, 'Manuel', false, NOW() - INTERVAL '1 hour', 2),
('Module Cage Principale', '192.168.3.12', 'actif', 85, 'Automatique', true, NOW() - INTERVAL '5 minutes', 2)
ON CONFLICT DO NOTHING;

-- Interactions Aras
INSERT INTO peps.Interaction (idsound, idmodule, typeInteraction, time_lancement, owner_id)
SELECT s.idsound, m.idmodule, 'Bec', NOW() - INTERVAL '20 minutes', 2
FROM peps.Sound s
JOIN peps.Module m ON m.owner_id = 2
WHERE s.nom = 'Cri Ara Bleu'
  AND m.nom = 'Module Volière Ara';

INSERT INTO peps.Interaction (idsound, idmodule, typeInteraction, time_lancement, owner_id)
SELECT s.idsound, m.idmodule, 'Patte', NOW() - INTERVAL '15 minutes', 2
FROM peps.Sound s
JOIN peps.Module m ON m.owner_id = 2
WHERE s.nom = 'Musique Tropicale'
  AND m.nom = 'Module Perchoir Ara';

-- ============================================================
-- VERIFICATIONS
-- ============================================================

SELECT owner_id, COUNT(*) FROM peps.Interaction GROUP BY owner_id;
SELECT owner_id, COUNT(*) FROM peps.Module GROUP BY owner_id;
SELECT owner_id, COUNT(*) FROM peps.Sound GROUP BY owner_id;
