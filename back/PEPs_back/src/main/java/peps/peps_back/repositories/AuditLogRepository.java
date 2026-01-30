package peps.peps_back.repositories;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;
import peps.peps_back.items.AuditLog;

import java.util.Date;
import java.util.List;

/**
 * Repository pour accéder aux entrées du journal d'audit.
 * 
 * @author Anas EL HOUDI
 */
@Repository
public interface AuditLogRepository extends JpaRepository<AuditLog, Integer> {

    /**
     * Récupère les logs triés par date décroissante (plus récent en premier).
     */
    List<AuditLog> findAllByOrderByTimestampDesc();

    /**
     * Récupère les logs d'un utilisateur spécifique.
     */
    List<AuditLog> findByUserLoginOrderByTimestampDesc(String userLogin);

    /**
     * Récupère les logs pour un type d'entité spécifique.
     */
    List<AuditLog> findByEntityTypeOrderByTimestampDesc(String entityType);

    /**
     * Récupère les logs dans une plage de dates.
     */
    @Query("SELECT a FROM AuditLog a WHERE a.timestamp BETWEEN :start AND :end ORDER BY a.timestamp DESC")
    List<AuditLog> findByDateRange(@Param("start") Date start, @Param("end") Date end);

    /* ===================== */
    /* Archive Methods */
    /* ===================== */

    /**
     * Récupère les logs avant une date spécifique.
     * Utilisé par le système d'archive pour trouver les anciens logs.
     * 
     * @author Anas EL HOUDI
     */
    List<AuditLog> findByTimestampBefore(Date date);

    /**
     * Compte les logs dans une plage de dates.
     * Utilisé par le système d'archive pour afficher les statistiques.
     * 
     * @author Anas EL HOUDI
     */
    long countByTimestampBetween(Date start, Date end);

    /**
     * Supprime les logs dans une plage de dates.
     * Utilisé par le système d'archive après export réussi.
     * 
     * @author Anas EL HOUDI
     */
    @Transactional
    void deleteByTimestampBetween(Date start, Date end);

    /**
     * Récupère les logs après une date spécifique (triés par date décroissante).
     * Utilisé pour filtrer les données archivées de la vue principale.
     * 
     * @author Anas EL HOUDI
     */
    List<AuditLog> findByTimestampAfterOrderByTimestampDesc(Date date);

    /**
     * Récupère les logs par type d'entité après une date spécifique.
     * 
     * @author Anas EL HOUDI
     */
    List<AuditLog> findByEntityTypeAndTimestampAfterOrderByTimestampDesc(String entityType, Date date);
}
