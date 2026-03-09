package peps.peps_back.controllers;

public class InteractionRequestDTO {
    private String typeInteraction;
    private Integer idmodule;
    private Integer idsound;

    // Getters and Setters
    public String getTypeInteraction() { return typeInteraction; }
    public void setTypeInteraction(String typeInteraction) { this.typeInteraction = typeInteraction; }

    public Integer getIdmodule() { return idmodule; }
    public void setIdmodule(Integer idmodule) { this.idmodule = idmodule; }

    public Integer getIdsound() { return idsound; }
    public void setIdsound(Integer idsound) { this.idsound = idsound; }
}