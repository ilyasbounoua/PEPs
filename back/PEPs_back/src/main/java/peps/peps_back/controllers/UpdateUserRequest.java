package peps.peps_back.controllers;

/**
 * DTO pour la modification d'un utilisateur existant.
 * Reçu du frontend lors d'une requête PUT /users/{id}.
 * 
 * Tous les champs sont optionnels :
 * - login : nouveau login (si modification)
 * - password : nouveau mot de passe (si modification, sera hashé)
 * - role : nouveau rôle ("admin", "dauphin" ou "aras")
 * - permission : nouvelle permission ("viewer", "editor" ou "admin")
 * 
 * @author Anas EL HOUDI
 */
public class UpdateUserRequest {

    private String login;
    private String password;
    private String role;
    private String permission;

    /* ===================== */
    /* Getters & Setters */
    /* ===================== */

    public String getLogin() {
        return login;
    }

    public void setLogin(String login) {
        this.login = login;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public String getRole() {
        return role;
    }

    public void setRole(String role) {
        this.role = role;
    }

    public String getPermission() {
        return permission;
    }

    public void setPermission(String permission) {
        this.permission = permission;
    }
}
