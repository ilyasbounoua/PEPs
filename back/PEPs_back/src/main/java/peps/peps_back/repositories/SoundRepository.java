/**
 * @author BOUNOUA Ilyas, VAZEILLE Clément, Anas EL HOUDI
 * @description This file defines the SoundRepository interface, which extends JpaRepository for Sound entities.
 */
package peps.peps_back.repositories;

import peps.peps_back.items.Sound;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface SoundRepository extends JpaRepository<Sound, Integer>, SoundRepositoryCustom {

    /**
     * Retrieves all sounds belonging to a specific role.
     * For multi-profile system (data isolation by role).
     * 
     * @author Anas EL HOUDI
     */
    List<Sound> findByOwnerRole(String ownerRole);
}
