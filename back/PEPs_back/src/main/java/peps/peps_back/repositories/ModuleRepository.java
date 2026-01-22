/**
 * @author BOUNOUA Ilyas, VAZEILLE Clément, Anas EL HOUDI
 * @description This file defines the ModuleRepository interface, which extends JpaRepository for Module entities.
 */
package peps.peps_back.repositories;

import peps.peps_back.items.Module;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ModuleRepository extends JpaRepository<Module, Integer>, ModuleRepositoryCustom {

    /**
     * Retrieves all modules belonging to a specific role.
     * For multi-profile system (data isolation by role).
     * 
     * @author Anas EL HOUDI
     */
    List<Module> findByOwnerRole(String ownerRole);
}
