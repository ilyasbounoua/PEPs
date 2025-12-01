/**
 * @author BOUNOUA Ilyas and VAZEILLE Clément
 * @description This file defines the InteractionRepository interface, which extends JpaRepository for Interaction entities.
 */
package peps.peps_back.repositories;

import peps.peps_back.items.Interaction;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface InteractionRepository extends JpaRepository<Interaction, Integer>, InteractionRepositoryCustom {

}