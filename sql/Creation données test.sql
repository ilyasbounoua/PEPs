-- 1. INSERER DES SONS
INSERT INTO public.Sound (nom, type_son, extension) VALUES
('Chant Mali', 'Ambiance', 'mp3'),
('Cri et Communication Perroquet', 'Vocal', 'mp3'),
('Son Eau Qui Coule', 'Naturel', 'wav');

-- 2. INSERER DES MODULES
INSERT INTO public.Module (nom, ip_adress, status, volume, current_mode, actif, last_seen) VALUES
('Module Perchoir 1', '192.168.1.10', 'actif', 80, 'Automatique', true, NOW() - INTERVAL '10 minutes'),
('Module Nid 2', '192.168.1.11', 'inactif', 65, 'Manuel', false, NOW() - INTERVAL '2 hours'),
('Module Abreuvoir', '192.168.1.12', 'actif', 100, 'Automatique', true, NOW() - INTERVAL '1 minute');

-- 3. INSERER DES INTERACTIONS
INSERT INTO public.Interaction (idsound, idmodule, typeInteraction, time_lancement) VALUES
-- Interaction : Son 1 (Cri Ara) sur Module 1 (Perchoir)
(1, 1, 'Bec', NOW() - INTERVAL '5 minutes'),

-- Interaction : Son 3 (Eau) sur Module 3 (Abreuvoir)
(3, 3, 'Patte', NOW() - INTERVAL '3 minutes'),

-- Interaction : Son 1 (Cri Ara) à nouveau sur Module 1 (Perchoir)
(1, 1, 'Bec', NOW() - INTERVAL '1 minute'),

-- Interaction : Son 2 (Foret) sur Module 1 (Perchoir)
(2, 1, 'Patte', NOW() - INTERVAL '30 seconds');


-- ============================================================
-- MISE A JOUR DES DONNEES EXISTANTES - Assignation à l'ADMIN
-- (Données actuelles avec owner_id NULL -> liées à admin id=1)
-- @author Anas EL HOUDI
-- ============================================================

-- Assigner tous les sons existants à l'admin (id_user = 1)
UPDATE public.Sound SET owner_id = 1 WHERE owner_id IS NULL;

-- Assigner tous les modules existants à l'admin (id_user = 1)
UPDATE public.Module SET owner_id = 1 WHERE owner_id IS NULL;

-- Assigner toutes les interactions existantes à l'admin (id_user = 1)
UPDATE public.Interaction SET owner_id = 1 WHERE owner_id IS NULL;


-- ============================================================
-- DONNEES TEST POUR DAUPHIN (id_user = 3)
-- TypeInteraction : "Head" et "Tail" (spécifiques au dauphin)
-- @author Anas EL HOUDI
-- ============================================================

-- Sons pour Dauphin
INSERT INTO public.Sound (nom, type_son, extension, owner_id) VALUES
('Chant des Baleines', 'Naturel', 'mp3', 3),
('Clics de Dauphin', 'Vocal', 'wav', 3),
('Vagues Océan', 'Ambiance', 'mp3', 3);

-- Modules pour Dauphin
INSERT INTO public.Module (nom, ip_adress, status, volume, current_mode, actif, last_seen, owner_id) VALUES
('Module Bassin Principal', '192.168.2.10', 'actif', 90, 'Automatique', true, NOW() - INTERVAL '5 minutes', 3),
('Module Zone Repos', '192.168.2.11', 'actif', 50, 'Manuel', true, NOW() - INTERVAL '15 minutes', 3);

-- Récupérer les IDs des sons et modules dauphin pour les interactions
-- (On utilise des sous-requêtes pour être sûr d'avoir les bons IDs)

-- Interactions pour Dauphin (typeInteraction: Head / Tail)
INSERT INTO public.Interaction (idsound, idmodule, typeInteraction, time_lancement, owner_id)
SELECT s.idsound, m.idmodule, 'Head', NOW() - INTERVAL '10 minutes', 3
FROM public.Sound s, public.Module m
WHERE s.nom = 'Chant des Baleines' AND m.nom = 'Module Bassin Principal';

INSERT INTO public.Interaction (idsound, idmodule, typeInteraction, time_lancement, owner_id)
SELECT s.idsound, m.idmodule, 'Tail', NOW() - INTERVAL '8 minutes', 3
FROM public.Sound s, public.Module m
WHERE s.nom = 'Clics de Dauphin' AND m.nom = 'Module Bassin Principal';

INSERT INTO public.Interaction (idsound, idmodule, typeInteraction, time_lancement, owner_id)
SELECT s.idsound, m.idmodule, 'Head', NOW() - INTERVAL '3 minutes', 3
FROM public.Sound s, public.Module m
WHERE s.nom = 'Vagues Océan' AND m.nom = 'Module Zone Repos';


-- ============================================================
-- DONNEES TEST POUR ARAS (id_user = 2)
-- TypeInteraction : "Bec" et "Patte" (spécifiques aux perroquets)
-- @author Anas EL HOUDI
-- ============================================================

-- Sons pour Aras
INSERT INTO public.Sound (nom, type_son, extension, owner_id) VALUES
('Cri Ara Bleu', 'Vocal', 'mp3', 2),
('Musique Tropicale', 'Ambiance', 'mp3', 2),
('Pluie Amazonienne', 'Naturel', 'wav', 2);

-- Modules pour Aras
INSERT INTO public.Module (nom, ip_adress, status, volume, current_mode, actif, last_seen, owner_id) VALUES
('Module Volière Ara', '192.168.3.10', 'actif', 75, 'Automatique', true, NOW() - INTERVAL '2 minutes', 2),
('Module Perchoir Ara', '192.168.3.11', 'inactif', 60, 'Manuel', false, NOW() - INTERVAL '1 hour', 2);

-- Interactions pour Aras (typeInteraction: Bec / Patte)
INSERT INTO public.Interaction (idsound, idmodule, typeInteraction, time_lancement, owner_id)
SELECT s.idsound, m.idmodule, 'Bec', NOW() - INTERVAL '20 minutes', 2
FROM public.Sound s, public.Module m
WHERE s.nom = 'Cri Ara Bleu' AND m.nom = 'Module Volière Ara';

INSERT INTO public.Interaction (idsound, idmodule, typeInteraction, time_lancement, owner_id)
SELECT s.idsound, m.idmodule, 'Patte', NOW() - INTERVAL '15 minutes', 2
FROM public.Sound s, public.Module m
WHERE s.nom = 'Musique Tropicale' AND m.nom = 'Module Perchoir Ara';

INSERT INTO public.Interaction (idsound, idmodule, typeInteraction, time_lancement, owner_id)
SELECT s.idsound, m.idmodule, 'Bec', NOW() - INTERVAL '5 minutes', 2
FROM public.Sound s, public.Module m
WHERE s.nom = 'Pluie Amazonienne' AND m.nom = 'Module Volière Ara';
