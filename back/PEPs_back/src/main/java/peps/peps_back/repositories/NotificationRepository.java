package peps.peps_back.repositories;

/**
 * @author Anas EL HOUDI
 * @description Repository for Notification entity
 */
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import peps.peps_back.items.Notification;
import java.util.List;

@Repository
public interface NotificationRepository extends JpaRepository<Notification, Integer> {

    // Get all notifications for a specific profile, ordered by newest first
    List<Notification> findByOwnerRoleOrderByTimestampDesc(String ownerRole);

    // Get unread notifications for a specific profile
    List<Notification> findByOwnerRoleAndIsReadFalseOrderByTimestampDesc(String ownerRole);

    // Get unread notifications for a specific profile newer than an ID (for
    // real-time polling)
    List<Notification> findByOwnerRoleAndIsReadFalseAndIdGreaterThanOrderByIdAsc(String ownerRole, Integer id);

    // Count unread notifications
    long countByOwnerRoleAndIsReadFalse(String ownerRole);

    // --- Admin generic methods (no role filtering) ---

    List<Notification> findAllByOrderByTimestampDesc();

    List<Notification> findByIsReadFalseOrderByTimestampDesc();

    List<Notification> findByIsReadFalseAndIdGreaterThanOrderByIdAsc(Integer id);

    long countByIsReadFalse();

    @Transactional
    @Modifying
    @Query("UPDATE Notification n SET n.isRead = true WHERE n.id = :id AND n.ownerRole = :ownerRole")
    void markAsRead(Integer id, String ownerRole);

    @Transactional
    @Modifying
    @Query("UPDATE Notification n SET n.isRead = true WHERE n.id = :id")
    void markAsReadAdmin(Integer id);

    @Transactional
    void deleteByOwnerRole(String ownerRole);
}
