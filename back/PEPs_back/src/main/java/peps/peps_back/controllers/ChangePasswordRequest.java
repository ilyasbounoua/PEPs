package peps.peps_back.controllers;

/**
 * DTO pour la requête de changement de mot de passe par l'utilisateur.
 * L'utilisateur doit fournir son ancien et son nouveau mot de passe.
 * 
 * @author Anas EL HOUDI
 */
public class ChangePasswordRequest {

    private String currentPassword;
    private String newPassword;

    public ChangePasswordRequest() {
    }

    public ChangePasswordRequest(String currentPassword, String newPassword) {
        this.currentPassword = currentPassword;
        this.newPassword = newPassword;
    }

    public String getCurrentPassword() {
        return currentPassword;
    }

    public void setCurrentPassword(String currentPassword) {
        this.currentPassword = currentPassword;
    }

    public String getNewPassword() {
        return newPassword;
    }

    public void setNewPassword(String newPassword) {
        this.newPassword = newPassword;
    }
}
