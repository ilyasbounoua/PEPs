package peps.peps_back.items;

import javax.persistence.*;
import java.time.LocalDateTime;

/**
 * Entité représentant un utilisateur du système PEP'S.
 * Prend en charge le système multi-profils avec 3 rôles possibles :
 * - "admin" : accès complet + gestion des utilisateurs
 * - "dauphin" : accès limité à ses propres données
 * - "aras" : accès limité à ses propres données
 * 
 * @author Équipe PEP'S, Anas EL HOUDI
 */
@Entity
@Table(name = "users")
public class User {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_user")
    private Integer idUser;

    @Column(nullable = false, unique = true, length = 100)
    private String login;

    @Column(name = "password_hash", nullable = false, length = 255)
    private String passwordHash;

    @Column(nullable = false)
    private Boolean enabled = true;

    /**
     * Rôle de l'utilisateur : "admin", "dauphin" ou "aras".
     * Utilisé pour :
     * - Contrôler l'accès aux fonctionnalités (ex: gestion users pour admin)
     * - Filtrer les données visibles (chaque user ne voit que ses propres
     * modules/sons/interactions)
     */
    @Column(nullable = false, length = 20)
    private String role = "dauphin";

    /**
     * Permission de l'utilisateur : "viewer", "editor" ou "admin".
     * - viewer : lecture seule
     * - editor : lecture + modification
     * - admin : tout + gestion users + voir audit logs
     */
    @Column(nullable = false, length = 20)
    private String permission = "viewer";

    /**
     * Langue préférée de l'utilisateur : "fr" ou "en".
     * Utilisée pour l'internationalisation de l'interface.
     */
    @Column(name = "preferred_lang", nullable = false, length = 2)
    private String preferredLang = "fr";

    @Column(name = "created_at", nullable = false)
    private LocalDateTime createdAt = LocalDateTime.now();

    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt = LocalDateTime.now();

    /* ===================== */
    /* Getters & Setters */
    /* ===================== */

    public Integer getIdUser() {
        return idUser;
    }

    public void setIdUser(Integer idUser) {
        this.idUser = idUser;
    }

    public String getLogin() {
        return login;
    }

    public void setLogin(String login) {
        this.login = login;
    }

    public String getPasswordHash() {
        return passwordHash;
    }

    public void setPasswordHash(String passwordHash) {
        this.passwordHash = passwordHash;
    }

    public Boolean getEnabled() {
        return enabled;
    }

    public void setEnabled(Boolean enabled) {
        this.enabled = enabled;
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

    public String getPreferredLang() {
        return preferredLang;
    }

    public void setPreferredLang(String preferredLang) {
        this.preferredLang = preferredLang;
    }
}
