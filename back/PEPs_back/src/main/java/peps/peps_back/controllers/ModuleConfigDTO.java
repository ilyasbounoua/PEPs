/**
 * @author BOUNOUA Ilyas and VAZEILLE Clément
 * @description This file defines the ModuleConfigDTO class, which is a Data Transfer Object for module configuration data.
 */
package peps.peps_back.controllers;

public class ModuleConfigDTO {
    private int volume;
    private String mode;
    private boolean actif;
    private boolean son;

    public ModuleConfigDTO() {
    }

    public ModuleConfigDTO(int volume, String mode, boolean actif, boolean son) {
        this.volume = volume;
        this.mode = mode;
        this.actif = actif;
        this.son = son;
    }

    public int getVolume() {
        return volume;
    }

    public void setVolume(int volume) {
        this.volume = volume;
    }

    public String getMode() {
        return mode;
    }

    public void setMode(String mode) {
        this.mode = mode;
    }

    public boolean isActif() {
        return actif;
    }

    public void setActif(boolean actif) {
        this.actif = actif;
    }

    public boolean isSon() {
        return son;
    }

    public void setSon(boolean son) {
        this.son = son;
    }
}
