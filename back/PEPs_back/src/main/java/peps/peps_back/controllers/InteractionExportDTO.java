/**
 * @author Anas EL HOUDI
 * @description DTO for exporting interaction data to JSON.
 * Contains only the essential fields: date/time, module name, and type.
 */
package peps.peps_back.controllers;

public class InteractionExportDTO {

    private String dateTime; // Formatted date/time string
    private String module; // Module name
    private String type; // Interaction type

    public InteractionExportDTO() {
    }

    public InteractionExportDTO(String dateTime, String module, String type) {
        this.dateTime = dateTime;
        this.module = module;
        this.type = type;
    }

    public String getDateTime() {
        return dateTime;
    }

    public void setDateTime(String dateTime) {
        this.dateTime = dateTime;
    }

    public String getModule() {
        return module;
    }

    public void setModule(String module) {
        this.module = module;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }
}
