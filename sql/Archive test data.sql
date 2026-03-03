-- ============================================================
-- SCRIPT DE DONNÉES TEST POUR L'ARCHIVE DES INTERACTIONS
-- Crée des interactions anciennes (plus de 3 mois) pour tester
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
-- 3 interactions par rôle (6 au total)
-- ============================================================

-- Interactions pour Dauphin - Période Août-Octobre 2025
INSERT INTO peps.Interaction (idmodule, typeInteraction, time_lancement, owner_role)
SELECT m.idmodule, 'Head', '2025-08-15 10:30:00', 'dauphin'
FROM peps.Module m WHERE m.owner_role = 'dauphin' LIMIT 1;

INSERT INTO peps.Interaction (idmodule, typeInteraction, time_lancement, owner_role)
SELECT m.idmodule, 'Tail', '2025-09-10 14:45:00', 'dauphin'
FROM peps.Module m WHERE m.owner_role = 'dauphin' LIMIT 1;

INSERT INTO peps.Interaction (idmodule, typeInteraction, time_lancement, owner_role)
SELECT m.idmodule, 'Head', '2025-10-05 09:15:00', 'dauphin'
FROM peps.Module m WHERE m.owner_role = 'dauphin' LIMIT 1;

-- Interactions pour Aras - Période Août-Octobre 2025
INSERT INTO peps.Interaction (idmodule, typeInteraction, time_lancement, owner_role)
SELECT m.idmodule, 'Bec', '2025-08-20 11:00:00', 'aras'
FROM peps.Module m WHERE m.owner_role = 'aras' LIMIT 1;

INSERT INTO peps.Interaction (idmodule, typeInteraction, time_lancement, owner_role)
SELECT m.idmodule, 'Patte', '2025-09-25 16:30:00', 'aras'
FROM peps.Module m WHERE m.owner_role = 'aras' LIMIT 1;

INSERT INTO peps.Interaction (idmodule, typeInteraction, time_lancement, owner_role)
SELECT m.idmodule, 'Bec', '2025-10-15 08:45:00', 'aras'
FROM peps.Module m WHERE m.owner_role = 'aras' LIMIT 1;


-- ============================================================
-- PÉRIODE 2: Mai - Juillet 2025 (2025-05)
-- 3 interactions par rôle (6 au total)
-- ============================================================

-- Interactions pour Dauphin - Période Mai-Juillet 2025
INSERT INTO peps.Interaction (idmodule, typeInteraction, time_lancement, owner_role)
SELECT m.idmodule, 'Tail', '2025-05-12 13:20:00', 'dauphin'
FROM peps.Module m WHERE m.owner_role = 'dauphin' LIMIT 1;

INSERT INTO peps.Interaction (idmodule, typeInteraction, time_lancement, owner_role)
SELECT m.idmodule, 'Head', '2025-06-18 15:00:00', 'dauphin'
FROM peps.Module m WHERE m.owner_role = 'dauphin' LIMIT 1;

INSERT INTO peps.Interaction (idmodule, typeInteraction, time_lancement, owner_role)
SELECT m.idmodule, 'Tail', '2025-07-22 10:10:00', 'dauphin'
FROM peps.Module m WHERE m.owner_role = 'dauphin' LIMIT 1;

-- Interactions pour Aras - Période Mai-Juillet 2025
INSERT INTO peps.Interaction (idmodule, typeInteraction, time_lancement, owner_role)
SELECT m.idmodule, 'Patte', '2025-05-08 09:30:00', 'aras'
FROM peps.Module m WHERE m.owner_role = 'aras' LIMIT 1;

INSERT INTO peps.Interaction (idmodule, typeInteraction, time_lancement, owner_role)
SELECT m.idmodule, 'Bec', '2025-06-25 11:45:00', 'aras'
FROM peps.Module m WHERE m.owner_role = 'aras' LIMIT 1;

INSERT INTO peps.Interaction (idmodule, typeInteraction, time_lancement, owner_role)
SELECT m.idmodule, 'Patte', '2025-07-30 14:15:00', 'aras'
FROM peps.Module m WHERE m.owner_role = 'aras' LIMIT 1;


-- ============================================================
-- PÉRIODE 3: Février - Avril 2025 (2025-02)
-- 3 interactions par rôle (6 au total)
-- ============================================================

-- Interactions pour Dauphin - Période Février-Avril 2025
INSERT INTO peps.Interaction (idmodule, typeInteraction, time_lancement, owner_role)
SELECT m.idmodule, 'Head', '2025-02-10 08:00:00', 'dauphin'
FROM peps.Module m WHERE m.owner_role = 'dauphin' LIMIT 1;

INSERT INTO peps.Interaction (idmodule, typeInteraction, time_lancement, owner_role)
SELECT m.idmodule, 'Tail', '2025-03-15 12:30:00', 'dauphin'
FROM peps.Module m WHERE m.owner_role = 'dauphin' LIMIT 1;

INSERT INTO peps.Interaction (idmodule, typeInteraction, time_lancement, owner_role)
SELECT m.idmodule, 'Head', '2025-04-20 17:45:00', 'dauphin'
FROM peps.Module m WHERE m.owner_role = 'dauphin' LIMIT 1;

-- Interactions pour Aras - Période Février-Avril 2025
INSERT INTO peps.Interaction (idmodule, typeInteraction, time_lancement, owner_role)
SELECT m.idmodule, 'Bec', '2025-02-05 10:00:00', 'aras'
FROM peps.Module m WHERE m.owner_role = 'aras' LIMIT 1;

INSERT INTO peps.Interaction (idmodule, typeInteraction, time_lancement, owner_role)
SELECT m.idmodule, 'Patte', '2025-03-22 14:00:00', 'aras'
FROM peps.Module m WHERE m.owner_role = 'aras' LIMIT 1;

INSERT INTO peps.Interaction (idmodule, typeInteraction, time_lancement, owner_role)
SELECT m.idmodule, 'Bec', '2025-04-28 16:30:00', 'aras'
FROM peps.Module m WHERE m.owner_role = 'aras' LIMIT 1;


-- ============================================================
-- VÉRIFICATION DES DONNÉES ARCHIVABLES
-- ============================================================

-- Afficher toutes les interactions par période
SELECT 
    'Période' as type,
    CASE 
        WHEN time_lancement >= '2025-08-01' AND time_lancement < '2025-11-01' THEN 'Août - Octobre 2025'
        WHEN time_lancement >= '2025-05-01' AND time_lancement < '2025-08-01' THEN 'Mai - Juillet 2025'
        WHEN time_lancement >= '2025-02-01' AND time_lancement < '2025-05-01' THEN 'Février - Avril 2025'
        ELSE 'Autre période'
    END as periode,
    COUNT(*) as nb_interactions
FROM peps.Interaction
WHERE time_lancement < NOW() - INTERVAL '3 months'
GROUP BY periode
ORDER BY MIN(time_lancement);

-- Afficher les détails des interactions archivables
SELECT 
    idinteraction,
    time_lancement,
    typeInteraction,
    owner_role,
    (SELECT nom FROM peps.Module WHERE idmodule = i.idmodule) as module_name
FROM peps.Interaction i
WHERE time_lancement < NOW() - INTERVAL '3 months'
ORDER BY time_lancement;

-- Compter le total
SELECT 
    'Total interactions archivables' as description,
    COUNT(*) as count
FROM peps.Interaction
WHERE time_lancement < NOW() - INTERVAL '3 months';
