-- ============================================================
-- SCRIPT DE CORRECTION DES OWNER_ROLE
-- Corrige les données qui ont 'admin' au lieu du bon rôle
-- @author Anas EL HOUDI
-- ============================================================

-- Option 1: Mettre à jour tous les modules/sons/interactions qui ont 'admin' vers 'aras'
-- (modifiez 'aras' par le rôle souhaité)

UPDATE peps.Module SET owner_role = 'aras' WHERE owner_role = 'admin';
UPDATE peps.Sound SET owner_role = 'aras' WHERE owner_role = 'admin';
UPDATE peps.Interaction SET owner_role = 'aras' WHERE owner_role = 'admin';

-- Vérification
SELECT 'Modules' as table_name, owner_role, COUNT(*) FROM peps.Module GROUP BY owner_role;
SELECT 'Sounds' as table_name, owner_role, COUNT(*) FROM peps.Sound GROUP BY owner_role;
SELECT 'Interactions' as table_name, owner_role, COUNT(*) FROM peps.Interaction GROUP BY owner_role;

-- ============================================================
-- OU Option 2: Répartir les données entre plusieurs rôles
-- Par exemple, diviser entre 'dauphin' et 'aras'
-- ============================================================

-- UPDATE peps.Module SET owner_role = 'dauphin' WHERE idmodule % 2 = 0 AND owner_role = 'admin';
-- UPDATE peps.Module SET owner_role = 'aras' WHERE idmodule % 2 = 1 AND owner_role = 'admin';
