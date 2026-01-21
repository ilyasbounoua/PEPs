package peps.peps_back.controllers;

/**
 * DTO pour le transfert des données utilisateur vers le frontend.
 * N'expose PAS le password_hash pour des raisons de sécurité.
 * 
 * Utilisé pour :
 * - Lister les utilisateurs (admin)
 * - Réponse après création/modification d'un utilisateur
 * 
 * @author Anas EL HOUDI
 */
public class UserDTO {

    private Integer id;
    private String login;
    private String role;
    private Boolean enabled;

    // Constructeur par défaut
    public UserDTO() {
    }

    // Constructeur complet
    public UserDTO(Integer id, String login, String role, Boolean enabled) {
        this.id = id;
        this.login = login;
        this.role = role;
        this.enabled = enabled;
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

    public String getLogin() {
        return login;
    }

    public void setLogin(String login) {
        this.login = login;
    }

    public String getRole() {
        return role;
    }

    public void setRole(String role) {
        this.role = role;
    }

    public Boolean getEnabled() {
        return enabled;
    }

    public void setEnabled(Boolean enabled) {
        this.enabled = enabled;
    }
}
