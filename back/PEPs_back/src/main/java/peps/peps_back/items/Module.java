/**
 * @author BOUNOUA Ilyas and VAZEILLE Clément
 * @description This file defines the Module entity, which represents a physical module in the system.
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
import javax.persistence.NamedQuery;
import javax.persistence.OneToMany;
import javax.persistence.Table;
import javax.persistence.Temporal;
import javax.persistence.TemporalType;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;

@Entity
@Table(name = "module")
@NamedQuery(name = "Module.findAll", query = "SELECT m FROM Module m")
@NamedQuery(name = "Module.findByIdmodule", query = "SELECT m FROM Module m WHERE m.idmodule = :idmodule")
@NamedQuery(name = "Module.findByNom", query = "SELECT m FROM Module m WHERE m.nom = :nom")
@NamedQuery(name = "Module.findByIpAdress", query = "SELECT m FROM Module m WHERE m.ipAdress = :ipAdress")
@NamedQuery(name = "Module.findByStatus", query = "SELECT m FROM Module m WHERE m.status = :status")
@NamedQuery(name = "Module.findByVolume", query = "SELECT m FROM Module m WHERE m.volume = :volume")
@NamedQuery(name = "Module.findByCurrentMode", query = "SELECT m FROM Module m WHERE m.currentMode = :currentMode")
@NamedQuery(name = "Module.findByActif", query = "SELECT m FROM Module m WHERE m.actif = :actif")
@NamedQuery(name = "Module.findByLastSeen", query = "SELECT m FROM Module m WHERE m.lastSeen = :lastSeen")
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

    /**
     * Constructs a new Module with the given configuration.
     * @param config The module configuration.
     */
    public Module(ModuleConfig config) {
        this.idmodule = config.idmodule;
        this.nom = config.nom;
        this.ipAdress = config.ipAdress;
        this.status = config.status;
        this.volume = config.volume;
        this.currentMode = config.currentMode;
        this.actif = config.actif;
        this.lastSeen = config.lastSeen;
    }

    /**
     * This class defines the configuration for a module.
     */
    public static class ModuleConfig {
        private Integer idmodule;
        private String nom;
        private String ipAdress;
        private String status;
        private int volume;
        private String currentMode;
        private boolean actif;
        private Date lastSeen;

        public ModuleConfig setIdmodule(Integer idmodule) {
            this.idmodule = idmodule;
            return this;
        }

        public ModuleConfig setNom(String nom) {
            this.nom = nom;
            return this;
        }

        public ModuleConfig setIpAdress(String ipAdress) {
            this.ipAdress = ipAdress;
            return this;
        }

        public ModuleConfig setStatus(String status) {
            this.status = status;
            return this;
        }

        public ModuleConfig setVolume(int volume) {
            this.volume = volume;
            return this;
        }

        public ModuleConfig setCurrentMode(String currentMode) {
            this.currentMode = currentMode;
            return this;
        }

        public ModuleConfig setActif(boolean actif) {
            this.actif = actif;
            return this;
        }

        public ModuleConfig setLastSeen(Date lastSeen) {
            this.lastSeen = lastSeen;
            return this;
        }
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
        if (!(object instanceof Module)) {
            return false;
        }
        Module other = (Module) object;
        // This method was changed to handle null idmodule values.
        return (this.idmodule != null && this.idmodule.equals(other.idmodule)) || (this.idmodule == null && other.idmodule == null);
    }

    @Override
    public String toString() {
        return "peps.peps_back.items.Module[ idmodule=" + idmodule + " ]";
    }
    
}
