/**
 * @author BOUNOUA Ilyas, VAZEILLE Clément, Anas EL HOUDI
 * @description This file defines the SoundRepository interface, which extends JpaRepository for Sound entities.
 */
package peps.peps_back.repositories;

import peps.peps_back.items.Sound;
import peps.peps_back.items.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface SoundRepository extends JpaRepository<Sound, Integer>, SoundRepositoryCustom {

    /**
     * Retrieves all sounds belonging to a user.
     * For multi-profile system (data isolation).
     * 
     * @author Anas EL HOUDI
     */
    List<Sound> findByOwner(User owner);
}
