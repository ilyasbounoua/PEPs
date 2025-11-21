package peps.peps_back.controllers;

public class ModuleDTO {
    private Integer id;
    private String name;
    private String location;
    private String status;
    private String ip;
    private ModuleConfigDTO config;

    public ModuleDTO() {
    }

    public ModuleDTO(Integer id, String name, String location, String status, String ip, ModuleConfigDTO config) {
        this.id = id;
        this.name = name;
        this.location = location;
        this.status = status;
        this.ip = ip;
        this.config = config;
    }

    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getLocation() {
        return location;
    }

    public void setLocation(String location) {
        this.location = location;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public String getIp() {
        return ip;
    }

    public void setIp(String ip) {
        this.ip = ip;
    }

    public ModuleConfigDTO getConfig() {
        return config;
    }

    public void setConfig(ModuleConfigDTO config) {
        this.config = config;
    }
}
