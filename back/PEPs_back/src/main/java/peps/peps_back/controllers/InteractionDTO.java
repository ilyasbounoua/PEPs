/**
 * @author BOUNOUA Ilyas and VAZEILLE Clément
 * @description This file defines the InteractionDTO class, which is a Data Transfer Object for interaction data.
 */
package peps.peps_back.controllers;

import java.util.Date;

public class InteractionDTO {
    private Integer id;
    private Date date;
    private String module;
    private String type;

    public InteractionDTO(Integer id, Date date, String module, String type) {
        this.id = id;
        this.date = date;
        this.module = module;
        this.type = type;
    }

    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public Date getDate() {
        return date;
    }

    public void setDate(Date date) {
        this.date = date;
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
