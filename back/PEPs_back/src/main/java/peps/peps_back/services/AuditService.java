package peps.peps_back.services;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import peps.peps_back.items.AuditLog;
import peps.peps_back.repositories.AuditLogRepository;

/**
 * Service pour enregistrer les actions dans le journal d'audit.
 * Utilisé par les contrôleurs pour tracer les modifications.
 * 
 * @author Anas EL HOUDI
 */
@Service
public class AuditService {

    @Autowired
    private AuditLogRepository auditLogRepository;

    /**
     * Enregistre une action dans le journal d'audit.
     *
     * @param action     Type d'action: "CREATE", "UPDATE", "DELETE"
     * @param entityType Type d'entité: "module", "sound", "user"
     * @param entityId   ID de l'entité
     * @param entityName Nom de l'entité (module name, sound name, user login)
     * @param entityRole Rôle cible (dauphin, aras, etc.) - peut être null
     * @param userLogin  Login de l'utilisateur effectuant l'action
     * @param oldValue   Valeur avant modification (JSON) - null pour CREATE
     * @param newValue   Valeur après modification (JSON) - null pour DELETE
     * @param details    Description courte de l'action
     */
    public void log(String action, String entityType, Integer entityId, String entityName,
            String entityRole, String userLogin, String oldValue, String newValue, String details) {

        AuditLog auditLog = new AuditLog(
                action, entityType, entityId, entityName,
                entityRole, userLogin, oldValue, newValue, details);

        auditLogRepository.save(auditLog);
        System.out.println("[AUDIT] " + action + " " + entityType + " #" + entityId + " by " + userLogin);
    }

    /**
     * Masque les mots de passe dans les valeurs JSON.
     * Remplace les champs password/passwordHash par "****".
     */
    public String maskPassword(String jsonValue) {
        if (jsonValue == null)
            return null;
        // Simple replacement - for production, use a proper JSON parser
        return jsonValue
                .replaceAll("\"password\"\\s*:\\s*\"[^\"]*\"", "\"password\":\"****\"")
                .replaceAll("\"passwordHash\"\\s*:\\s*\"[^\"]*\"", "\"passwordHash\":\"****\"");
    }
}
