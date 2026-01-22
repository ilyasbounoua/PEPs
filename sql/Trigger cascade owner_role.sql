-- ============================================================
-- TRIGGER : Mise à jour cascade de owner_role
-- Quand le rôle d'un utilisateur change dans la table users,
-- mettre à jour automatiquement owner_role dans :
-- - Module
-- - Sound
-- - Interaction
-- @author Anas EL HOUDI
-- ============================================================

-- 1. Créer la fonction trigger
CREATE OR REPLACE FUNCTION peps.update_owner_role_on_user_role_change()
RETURNS TRIGGER AS $$
BEGIN
    -- Vérifier si le rôle a changé
    IF OLD.role IS DISTINCT FROM NEW.role THEN
        -- Mettre à jour owner_role dans Module
        UPDATE peps.Module 
        SET owner_role = NEW.role 
        WHERE owner_role = OLD.role;
        
        -- Mettre à jour owner_role dans Sound
        UPDATE peps.Sound 
        SET owner_role = NEW.role 
        WHERE owner_role = OLD.role;
        
        -- Mettre à jour owner_role dans Interaction
        UPDATE peps.Interaction 
        SET owner_role = NEW.role 
        WHERE owner_role = OLD.role;
        
        RAISE NOTICE 'owner_role mis à jour de "%" vers "%" dans toutes les tables', OLD.role, NEW.role;
    END IF;
    
    RETURN NEW;
END;
$$ LANGUAGE plpgsql;

-- 2. Créer le trigger sur la table users
DROP TRIGGER IF EXISTS trigger_cascade_owner_role ON peps.users;

CREATE TRIGGER trigger_cascade_owner_role
AFTER UPDATE OF role ON peps.users
FOR EACH ROW
EXECUTE FUNCTION peps.update_owner_role_on_user_role_change();

-- 3. Vérification : Afficher le trigger créé
SELECT trigger_name, event_manipulation, action_statement 
FROM information_schema.triggers 
WHERE trigger_name = 'trigger_cascade_owner_role';

-- ============================================================
-- UTILISATION :
-- Exécutez ce script une seule fois dans PostgreSQL (pgAdmin)
-- Ensuite, chaque modification de rôle dans users sera
-- automatiquement propagée aux autres tables.
-- ============================================================
