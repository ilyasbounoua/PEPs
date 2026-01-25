/**
 * @author BOUNOUA Ilyas, VAZEILLE Clément, Anas EL HOUDI
 * @description This file defines the InteractionRepository interface, which extends JpaRepository for Interaction entities.
 */
package peps.peps_back.repositories;

import peps.peps_back.items.Interaction;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface InteractionRepository extends JpaRepository<Interaction, Integer>, InteractionRepositoryCustom {

    /**
     * Retrieves all interactions belonging to a specific role.
     * For multi-profile system (data isolation by role).
     * 
     * @author Anas EL HOUDI
     */
    List<Interaction> findByOwnerRole(String ownerRole);

    /**
     * Retrieves interactions by role within a specific time range.
     */
    List<Interaction> findByOwnerRoleAndTimeLancementBetween(String ownerRole, java.util.Date startDate,
            java.util.Date endDate);

    /**
     * Retrieves all interactions within a specific time range.
     */
    List<Interaction> findByTimeLancementBetween(java.util.Date startDate, java.util.Date endDate);
}
