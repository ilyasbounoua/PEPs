package peps.peps_back.controllers;

public class SoundDTO {
    private Integer id;
    private String name;
    private String type;
    private String extension;
    private String fileName;

    public SoundDTO() {
    }

    public SoundDTO(Integer id, String name, String type, String extension) {
        this.id = id;
        this.name = name;
        this.type = type;
        this.extension = extension;
        this.fileName = generateFileName(name, extension);
    }

    private String generateFileName(String name, String extension) {
        if (name == null || extension == null) return null;
        return name.replaceAll("[^a-zA-Z0-9\\s]", "_").replaceAll("\\s+", "_") + "." + extension;
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
        this.fileName = generateFileName(name, extension);
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public String getExtension() {
        return extension;
    }

    public void setExtension(String extension) {
        this.extension = extension;
        this.fileName = generateFileName(name, extension);
    }

    public String getFileName() {
        return fileName;
    }

    public void setFileName(String fileName) {
        this.fileName = fileName;
    }
}
