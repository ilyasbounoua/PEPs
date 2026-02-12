package peps.peps_back.controllers;

/**
 * DTO pour la création d'un nouvel utilisateur.
 * Reçu du frontend lors d'une requête POST /users.
 * 
 * Champs requis :
 * - login : identifiant unique de l'utilisateur
 * - password : mot de passe en clair (sera hashé côté serveur avec BCrypt)
 * - role : "admin", "dauphin" ou "aras"
 * - permission : "viewer", "editor" ou "admin"
 * 
 * @author Anas EL HOUDI
 */
public class CreateUserRequest {

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
