/**
 * @author BOUNOUA Ilyas and VAZEILLE Clément
 * @description This file defines the SoundRepository interface, which extends JpaRepository for Sound entities.
 */
package peps.peps_back.repositories;

import peps.peps_back.items.Sound;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface SoundRepository extends JpaRepository<Sound, Integer>, SoundRepositoryCustom {

}