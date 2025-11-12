/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package peps.peps_back.items;

import java.io.Serializable;
import java.util.Collection;
import java.util.Date;
import javax.persistence.Basic;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.NamedQueries;
import javax.persistence.NamedQuery;
import javax.persistence.OneToMany;
import javax.persistence.Table;
import javax.persistence.Temporal;
import javax.persistence.TemporalType;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;

/**
 *
 * @author Clément
 */
@Entity
@Table(name = "module")
@NamedQueries({
    @NamedQuery(name = "Module.findAll", query = "SELECT m FROM Module m"),
    @NamedQuery(name = "Module.findByIdmodule", query = "SELECT m FROM Module m WHERE m.idmodule = :idmodule"),
    @NamedQuery(name = "Module.findByNom", query = "SELECT m FROM Module m WHERE m.nom = :nom"),
    @NamedQuery(name = "Module.findByIpAdress", query = "SELECT m FROM Module m WHERE m.ipAdress = :ipAdress"),
    @NamedQuery(name = "Module.findByStatus", query = "SELECT m FROM Module m WHERE m.status = :status"),
    @NamedQuery(name = "Module.findByVolume", query = "SELECT m FROM Module m WHERE m.volume = :volume"),
    @NamedQuery(name = "Module.findByCurrentMode", query = "SELECT m FROM Module m WHERE m.currentMode = :currentMode"),
    @NamedQuery(name = "Module.findByActif", query = "SELECT m FROM Module m WHERE m.actif = :actif"),
    @NamedQuery(name = "Module.findByLastSeen", query = "SELECT m FROM Module m WHERE m.lastSeen = :lastSeen")})
public class Module implements Serializable {

    private static final long serialVersionUID = 1L;
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Basic(optional = false)
    @Column(name = "idmodule")
    private Integer idmodule;
    @Basic(optional = false)
    @NotNull
    @Size(min = 1, max = 255)
    @Column(name = "nom")
    private String nom;
    @Basic(optional = false)
    @NotNull
    @Size(min = 1, max = 50)
    @Column(name = "ip_adress")
    private String ipAdress;
    @Basic(optional = false)
    @NotNull
    @Size(min = 1, max = 50)
    @Column(name = "status")
    private String status;
    @Basic(optional = false)
    @NotNull
    @Column(name = "volume")
    private int volume;
    @Basic(optional = false)
    @NotNull
    @Size(min = 1, max = 50)
    @Column(name = "current_mode")
    private String currentMode;
    @Basic(optional = false)
    @NotNull
    @Column(name = "actif")
    private boolean actif;
    @Basic(optional = false)
    @NotNull
    @Column(name = "last_seen")
    @Temporal(TemporalType.TIMESTAMP)
    private Date lastSeen;
    @OneToMany(mappedBy = "idmodule")
    private Collection<Interaction> interactionCollection;

    public Module() {
    }

    public Module(Integer idmodule) {
        this.idmodule = idmodule;
    }

    public Module(Integer idmodule, String nom, String ipAdress, String status, int volume, String currentMode, boolean actif, Date lastSeen) {
        this.idmodule = idmodule;
        this.nom = nom;
        this.ipAdress = ipAdress;
        this.status = status;
        this.volume = volume;
        this.currentMode = currentMode;
        this.actif = actif;
        this.lastSeen = lastSeen;
    }

    public Integer getIdmodule() {
        return idmodule;
    }

    public void setIdmodule(Integer idmodule) {
        this.idmodule = idmodule;
    }

    public String getNom() {
        return nom;
    }

    public void setNom(String nom) {
        this.nom = nom;
    }

    public String getIpAdress() {
        return ipAdress;
    }

    public void setIpAdress(String ipAdress) {
        this.ipAdress = ipAdress;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public int getVolume() {
        return volume;
    }

    public void setVolume(int volume) {
        this.volume = volume;
    }

    public String getCurrentMode() {
        return currentMode;
    }

    public void setCurrentMode(String currentMode) {
        this.currentMode = currentMode;
    }

    public boolean getActif() {
        return actif;
    }

    public void setActif(boolean actif) {
        this.actif = actif;
    }

    public Date getLastSeen() {
        return lastSeen;
    }

    public void setLastSeen(Date lastSeen) {
        this.lastSeen = lastSeen;
    }

    public Collection<Interaction> getInteractionCollection() {
        return interactionCollection;
    }

    public void setInteractionCollection(Collection<Interaction> interactionCollection) {
        this.interactionCollection = interactionCollection;
    }

    @Override
    public int hashCode() {
        int hash = 0;
        hash += (idmodule != null ? idmodule.hashCode() : 0);
        return hash;
    }

    @Override
    public boolean equals(Object object) {
        // TODO: Warning - this method won't work in the case the id fields are not set
        if (!(object instanceof Module)) {
            return false;
        }
        Module other = (Module) object;
        if ((this.idmodule == null && other.idmodule != null) || (this.idmodule != null && !this.idmodule.equals(other.idmodule))) {
            return false;
        }
        return true;
    }

    @Override
    public String toString() {
        return "peps.peps_back.items.Module[ idmodule=" + idmodule + " ]";
    }
    
}
