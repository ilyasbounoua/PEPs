/**
 * @author BOUNOUA Ilyas and VAZEILLE Clément
 * @description This file defines the ModuleRepository interface, which extends JpaRepository for Module entities.
 */
package peps.peps_back.repositories;

import peps.peps_back.items.Module;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface ModuleRepository extends JpaRepository<Module, Integer>, ModuleRepositoryCustom {

}