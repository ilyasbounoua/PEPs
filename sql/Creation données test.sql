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