/**
 * @author BOUNOUA Ilyas, VAZEILLE Clément, Anas EL HOUDI
 * @description This file defines the ModuleRepository interface, which extends JpaRepository for Module entities.
 */
package peps.peps_back.repositories;

import peps.peps_back.items.Module;
import peps.peps_back.items.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ModuleRepository extends JpaRepository<Module, Integer>, ModuleRepositoryCustom {

    /**
     * Récupère tous les modules appartenant à un utilisateur.
     * Pour le système multi-profils (isolation des données).
     * 
     * @author Anas EL HOUDI
     */
    List<Module> findByOwner(User owner);
}
