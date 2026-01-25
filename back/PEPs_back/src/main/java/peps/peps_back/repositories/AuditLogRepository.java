package peps.peps_back.repositories;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;
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
    List<AuditLog> findByDateRange(Date start, Date end);
}
