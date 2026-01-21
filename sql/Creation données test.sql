-- ============================================================
-- SCRIPT DE NETTOYAGE ET CONFIGURATION ADMIN SUPERVISION
-- Admin (id=1) n'est PAS un animal : il ne possède pas de données
-- Il supervise les données de tous les utilisateurs (dauphin, aras)
-- @author Anas EL HOUDI
-- ============================================================

-- 1. SUPPRESSION DES DONNEES ADMIN (owner_id = 1)
-- Admin n'est pas un animal, il supervise uniquement

DELETE FROM public.Interaction WHERE owner_id = 1;
DELETE FROM public.Sound WHERE owner_id = 1;
DELETE FROM public.Module WHERE owner_id = 1;

-- 2. INSERER DES SONS (sans owner_id, ils seront assignés après)
-- Ces données de base seront assignées à dauphin et aras

-- 3. DONNEES TEST POUR DAUPHIN (id_user = 3)
-- TypeInteraction : "Head" et "Tail" (spécifique au dauphin)
-- @author Anas EL HOUDI

-- Sons pour Dauphin
INSERT INTO public.Sound (nom, type_son, extension, owner_id) VALUES
('Chant des Baleines', 'Naturel', 'mp3', 3),
('Clics de Dauphin', 'Vocal', 'wav', 3),
('Vagues Océan', 'Ambiance', 'mp3', 3),
('Sonar Marin', 'Vocal', 'mp3', 3),
('Musique Aquatique', 'Ambiance', 'wav', 3)
ON CONFLICT DO NOTHING;

-- Modules pour Dauphin
INSERT INTO public.Module (nom, ip_adress, status, volume, current_mode, actif, last_seen, owner_id) VALUES
('Module Bassin Principal', '192.168.2.10', 'actif', 90, 'Automatique', true, NOW() - INTERVAL '5 minutes', 3),
('Module Zone Repos', '192.168.2.11', 'actif', 50, 'Manuel', true, NOW() - INTERVAL '15 minutes', 3),
('Module Piscine Entrainement', '192.168.2.12', 'actif', 70, 'Automatique', true, NOW() - INTERVAL '1 minute', 3)
ON CONFLICT DO NOTHING;

-- Interactions pour Dauphin (typeInteraction: Head / Tail)
INSERT INTO public.Interaction (idsound, idmodule, typeInteraction, time_lancement, owner_id)
SELECT s.idsound, m.idmodule, 'Head', NOW() - INTERVAL '10 minutes', 3
FROM public.Sound s, public.Module m
WHERE s.nom = 'Chant des Baleines' AND s.owner_id = 3 AND m.nom = 'Module Bassin Principal' AND m.owner_id = 3;

INSERT INTO public.Interaction (idsound, idmodule, typeInteraction, time_lancement, owner_id)
SELECT s.idsound, m.idmodule, 'Tail', NOW() - INTERVAL '8 minutes', 3
FROM public.Sound s, public.Module m
WHERE s.nom = 'Clics de Dauphin' AND s.owner_id = 3 AND m.nom = 'Module Bassin Principal' AND m.owner_id = 3;

INSERT INTO public.Interaction (idsound, idmodule, typeInteraction, time_lancement, owner_id)
SELECT s.idsound, m.idmodule, 'Head', NOW() - INTERVAL '3 minutes', 3
FROM public.Sound s, public.Module m
WHERE s.nom = 'Vagues Océan' AND s.owner_id = 3 AND m.nom = 'Module Zone Repos' AND m.owner_id = 3;

INSERT INTO public.Interaction (idsound, idmodule, typeInteraction, time_lancement, owner_id)
SELECT s.idsound, m.idmodule, 'Tail', NOW() - INTERVAL '1 minute', 3
FROM public.Sound s, public.Module m
WHERE s.nom = 'Sonar Marin' AND s.owner_id = 3 AND m.nom = 'Module Piscine Entrainement' AND m.owner_id = 3;

INSERT INTO public.Interaction (idsound, idmodule, typeInteraction, time_lancement, owner_id)
SELECT s.idsound, m.idmodule, 'Head', NOW() - INTERVAL '30 seconds', 3
FROM public.Sound s, public.Module m
WHERE s.nom = 'Musique Aquatique' AND s.owner_id = 3 AND m.nom = 'Module Bassin Principal' AND m.owner_id = 3;

INSERT INTO public.Interaction (idsound, idmodule, typeInteraction, time_lancement, owner_id)
SELECT s.idsound, m.idmodule, 'Tail', NOW() - INTERVAL '15 seconds', 3
FROM public.Sound s, public.Module m
WHERE s.nom = 'Vagues Océan' AND s.owner_id = 3 AND m.nom = 'Module Piscine Entrainement' AND m.owner_id = 3;


-- ============================================================
-- DONNEES TEST POUR ARAS (id_user = 2)
-- TypeInteraction : "Bec" et "Patte" (spécifique aux perroquets)
-- @author Anas EL HOUDI
-- ============================================================

-- Sons pour Aras
INSERT INTO public.Sound (nom, type_son, extension, owner_id) VALUES
('Cri Ara Bleu', 'Vocal', 'mp3', 2),
('Musique Tropicale', 'Ambiance', 'mp3', 2),
('Pluie Amazonienne', 'Naturel', 'wav', 2),
('Chant Perroquet', 'Vocal', 'mp3', 2),
('Foret Tropicale', 'Ambiance', 'wav', 2)
ON CONFLICT DO NOTHING;

-- Modules pour Aras
INSERT INTO public.Module (nom, ip_adress, status, volume, current_mode, actif, last_seen, owner_id) VALUES
('Module Volière Ara', '192.168.3.10', 'actif', 75, 'Automatique', true, NOW() - INTERVAL '2 minutes', 2),
('Module Perchoir Ara', '192.168.3.11', 'inactif', 60, 'Manuel', false, NOW() - INTERVAL '1 hour', 2),
('Module Cage Principale', '192.168.3.12', 'actif', 85, 'Automatique', true, NOW() - INTERVAL '5 minutes', 2)
ON CONFLICT DO NOTHING;

-- Interactions pour Aras (typeInteraction: Bec / Patte)
INSERT INTO public.Interaction (idsound, idmodule, typeInteraction, time_lancement, owner_id)
SELECT s.idsound, m.idmodule, 'Bec', NOW() - INTERVAL '20 minutes', 2
FROM public.Sound s, public.Module m
WHERE s.nom = 'Cri Ara Bleu' AND s.owner_id = 2 AND m.nom = 'Module Volière Ara' AND m.owner_id = 2;

INSERT INTO public.Interaction (idsound, idmodule, typeInteraction, time_lancement, owner_id)
SELECT s.idsound, m.idmodule, 'Patte', NOW() - INTERVAL '15 minutes', 2
FROM public.Sound s, public.Module m
WHERE s.nom = 'Musique Tropicale' AND s.owner_id = 2 AND m.nom = 'Module Perchoir Ara' AND m.owner_id = 2;

INSERT INTO public.Interaction (idsound, idmodule, typeInteraction, time_lancement, owner_id)
SELECT s.idsound, m.idmodule, 'Bec', NOW() - INTERVAL '5 minutes', 2
FROM public.Sound s, public.Module m
WHERE s.nom = 'Pluie Amazonienne' AND s.owner_id = 2 AND m.nom = 'Module Volière Ara' AND m.owner_id = 2;

INSERT INTO public.Interaction (idsound, idmodule, typeInteraction, time_lancement, owner_id)
SELECT s.idsound, m.idmodule, 'Patte', NOW() - INTERVAL '2 minutes', 2
FROM public.Sound s, public.Module m
WHERE s.nom = 'Chant Perroquet' AND s.owner_id = 2 AND m.nom = 'Module Cage Principale' AND m.owner_id = 2;


-- ============================================================
-- VERIFICATION DES DONNEES
-- ============================================================

-- Vérifier que l'admin n'a plus de données personnelles
SELECT 'Interactions par owner' as check_type, owner_id, COUNT(*) as total FROM public.Interaction GROUP BY owner_id;
SELECT 'Modules par owner' as check_type, owner_id, COUNT(*) as total FROM public.Module GROUP BY owner_id;
SELECT 'Sons par owner' as check_type, owner_id, COUNT(*) as total FROM public.Sound GROUP BY owner_id;
