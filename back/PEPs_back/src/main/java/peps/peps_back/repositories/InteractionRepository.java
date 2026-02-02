/**
 * @author BOUNOUA Ilyas, VAZEILLE Clément, Anas EL HOUDI
 * @description This file defines the InteractionRepository interface, which extends JpaRepository for Interaction entities.
 */
package peps.peps_back.repositories;

import peps.peps_back.items.Interaction;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import org.springframework.transaction.annotation.Transactional;

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

    /**
     * Retrieves all interactions before a specific date.
     * Used by archive system to find old interactions.
     * 
     * @author Anas EL HOUDI
     */
    List<Interaction> findByTimeLancementBefore(java.util.Date date);

    /**
     * Counts interactions within a specific time range.
     * Used by archive system to display period statistics.
     * 
     * @author Anas EL HOUDI
     */
    long countByTimeLancementBetween(java.util.Date startDate, java.util.Date endDate);

    /**
     * Deletes all interactions within a specific time range.
     * Used by archive system after successful export.
     * 
     * @author Anas EL HOUDI
     */
    @Transactional
    void deleteByTimeLancementBetween(java.util.Date startDate, java.util.Date endDate);

    /**
     * Retrieves interactions after a specific date.
     * Used to filter out archived data from the main Interactions view.
     * 
     * @author Anas EL HOUDI
     */
    List<Interaction> findByTimeLancementAfter(java.util.Date date);

    /**
     * Retrieves interactions by role after a specific date.
     * Used to filter out archived data from the main Interactions view
     * (role-filtered).
     * 
     * @author Anas EL HOUDI
     */
    List<Interaction> findByOwnerRoleAndTimeLancementAfter(String ownerRole, java.util.Date date);
}
