/**
 * @author Anas EL HOUDI
 * @description DTO for exporting audit log data to JSON.
 * Contains the fields displayed in the Journal d'Audit section:
 * action, entity, user, date, and details (complete text, no ellipsis).
 */
package peps.peps_back.controllers;

public class AuditLogExportDTO {

    private String action; // CREATE, UPDATE, DELETE
    private String entity; // Entity type + name (e.g., "Module: Test Module (aras)")
    private String user; // User login
    private String date; // Formatted timestamp
    private String details; // Full details text

    public AuditLogExportDTO() {
    }

    public AuditLogExportDTO(String action, String entity, String user, String date, String details) {
        this.action = action;
        this.entity = entity;
        this.user = user;
        this.date = date;
        this.details = details;
    }

    public String getAction() {
        return action;
    }

    public void setAction(String action) {
        this.action = action;
    }

    public String getEntity() {
        return entity;
    }

    public void setEntity(String entity) {
        this.entity = entity;
    }

    public String getUser() {
        return user;
    }

    public void setUser(String user) {
        this.user = user;
    }

    public String getDate() {
        return date;
    }

    public void setDate(String date) {
        this.date = date;
    }

    public String getDetails() {
        return details;
    }

    public void setDetails(String details) {
        this.details = details;
    }
}
