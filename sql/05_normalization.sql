-- ============================================================
-- SCRIPT DE NORMALISATION DES OWNER_ROLE (LOWERCASE)
-- Le backend cherche en lowercase, donc les données doivent l'être aussi
-- @author Anas EL HOUDI
-- ============================================================

-- Convertir tous les owner_role en minuscules
UPDATE peps.Module SET owner_role = LOWER(owner_role) WHERE owner_role IS NOT NULL;
UPDATE peps.Sound SET owner_role = LOWER(owner_role) WHERE owner_role IS NOT NULL;
UPDATE peps.Interaction SET owner_role = LOWER(owner_role) WHERE owner_role IS NOT NULL;

-- Vérification - voir les valeurs actuelles
SELECT 'Modules' as table_name, owner_role, COUNT(*) FROM peps.Module GROUP BY owner_role
UNION ALL
SELECT 'Sounds' as table_name, owner_role, COUNT(*) FROM peps.Sound GROUP BY owner_role
UNION ALL
SELECT 'Interactions' as table_name, owner_role, COUNT(*) FROM peps.Interaction GROUP BY owner_role;

-- Si vous voulez voir les données détaillées pour debug:
-- SELECT idsound, nom, owner_role FROM peps.Sound;
-- SELECT idmodule, nom, owner_role FROM peps.Module;
