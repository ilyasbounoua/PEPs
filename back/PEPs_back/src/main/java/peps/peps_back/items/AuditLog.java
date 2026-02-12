package peps.peps_back.items;

import javax.persistence.*;
import java.util.Date;

/**
 * Entité représentant une entrée dans le journal d'audit.
 * Trace toutes les modifications (CREATE, UPDATE, DELETE) faites via le
 * frontend.
 * 
 * @author Anas EL HOUDI
 */
@Entity
@Table(name = "audit_logs")
public class AuditLog {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @Column(nullable = false, length = 20)
    private String action; // 'CREATE', 'UPDATE', 'DELETE'

    @Column(name = "entity_type", nullable = false, length = 50)
    private String entityType; // 'module', 'sound', 'user'

    @Column(name = "entity_id")
    private Integer entityId;

    @Column(name = "entity_name", length = 255)
    private String entityName;

    @Column(name = "entity_role", length = 50)
    private String entityRole; // Rôle cible (dauphin, aras, etc.)

    @Column(name = "user_login", nullable = false, length = 100)
    private String userLogin;

    @Basic(optional = false)
    @Column(nullable = false)
    @Temporal(TemporalType.TIMESTAMP)
    private Date timestamp = new Date();

    @Column(name = "old_value", columnDefinition = "TEXT")
    private String oldValue; // JSON

    @Column(name = "new_value", columnDefinition = "TEXT")
    private String newValue; // JSON

    @Column(columnDefinition = "TEXT")
    private String details;

    /* ===================== */
    /* Constructeurs */
    /* ===================== */

    public AuditLog() {
    }

    public AuditLog(String action, String entityType, Integer entityId, String entityName,
            String entityRole, String userLogin, String oldValue, String newValue, String details) {
        this.action = action;
        this.entityType = entityType;
        this.entityId = entityId;
        this.entityName = entityName;
        this.entityRole = entityRole;
        this.userLogin = userLogin;
        this.oldValue = oldValue;
        this.newValue = newValue;
        this.details = details;
        this.timestamp = new Date();
    }

    /* ===================== */
    /* Getters & Setters */
    /* ===================== */

    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public String getAction() {
        return action;
    }

    public void setAction(String action) {
        this.action = action;
    }

    public String getEntityType() {
        return entityType;
    }

    public void setEntityType(String entityType) {
        this.entityType = entityType;
    }

    public Integer getEntityId() {
        return entityId;
    }

    public void setEntityId(Integer entityId) {
        this.entityId = entityId;
    }

    public String getEntityName() {
        return entityName;
    }

    public void setEntityName(String entityName) {
        this.entityName = entityName;
    }

    public String getEntityRole() {
        return entityRole;
    }

    public void setEntityRole(String entityRole) {
        this.entityRole = entityRole;
    }

    public String getUserLogin() {
        return userLogin;
    }

    public void setUserLogin(String userLogin) {
        this.userLogin = userLogin;
    }

    public Date getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(Date timestamp) {
        this.timestamp = timestamp;
    }

    public String getOldValue() {
        return oldValue;
    }

    public void setOldValue(String oldValue) {
        this.oldValue = oldValue;
    }

    public String getNewValue() {
        return newValue;
    }

    public void setNewValue(String newValue) {
        this.newValue = newValue;
    }

    public String getDetails() {
        return details;
    }

    public void setDetails(String details) {
        this.details = details;
    }
}
