/**
 * @author BOUNOUA Ilyas, VAZEILLE Clément, Anas EL HOUDI
 * @description This file defines the InteractionRepository interface, which extends JpaRepository for Interaction entities.
 */
package peps.peps_back.repositories;

import peps.peps_back.items.Interaction;
import peps.peps_back.items.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface InteractionRepository extends JpaRepository<Interaction, Integer>, InteractionRepositoryCustom {

    /**
     * Récupère toutes les interactions appartenant à un utilisateur.
     * Pour le système multi-profils (isolation des données).
     * 
     * @author Anas EL HOUDI
     */
    List<Interaction> findByOwner(User owner);
}
