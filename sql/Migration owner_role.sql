-- ============================================================
-- MIGRATION : owner_id → owner_role (filtrage par rôle)
-- Utiliser le schéma 'peps'
-- @author Anas EL HOUDI
-- ============================================================

-- 1. Ajouter la nouvelle colonne owner_role
ALTER TABLE peps.Module ADD COLUMN owner_role character varying(50);
ALTER TABLE peps.Sound ADD COLUMN owner_role character varying(50);
ALTER TABLE peps.Interaction ADD COLUMN owner_role character varying(50);

-- 2. Migrer les données existantes (copier le rôle depuis users)
UPDATE peps.Module m SET owner_role = (SELECT role FROM peps.users WHERE id_user = m.owner_id);
UPDATE peps.Sound s SET owner_role = (SELECT role FROM peps.users WHERE id_user = s.owner_id);
UPDATE peps.Interaction i SET owner_role = (SELECT role FROM peps.users WHERE id_user = i.owner_id);

-- 3. (Optionnel) Supprimer l'ancienne colonne owner_id après vérification
-- ALTER TABLE peps.Module DROP COLUMN owner_id;
-- ALTER TABLE peps.Sound DROP COLUMN owner_id;
-- ALTER TABLE peps.Interaction DROP COLUMN owner_id;

-- 4. Vérification
SELECT 'Modules' as table_name, owner_role, COUNT(*) FROM peps.Module GROUP BY owner_role;
SELECT 'Sounds' as table_name, owner_role, COUNT(*) FROM peps.Sound GROUP BY owner_role;
SELECT 'Interactions' as table_name, owner_role, COUNT(*) FROM peps.Interaction GROUP BY owner_role;
