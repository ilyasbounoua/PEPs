/**
 * @author BOUNOUA Ilyas and VAZEILLE Clément
 * @description This file defines the Interaction entity, which represents an interaction with a module.
 */
package peps.peps_back.items;

import java.io.Serializable;
import java.util.Date;
import javax.persistence.Basic;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.NamedQuery;
import javax.persistence.Table;
import javax.persistence.Temporal;
import javax.persistence.TemporalType;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;

@Entity
@Table(name = "interaction")
@NamedQuery(name = "Interaction.findAll", query = "SELECT i FROM Interaction i")
@NamedQuery(name = "Interaction.findByIdinteraction", query = "SELECT i FROM Interaction i WHERE i.idinteraction = :idinteraction")
@NamedQuery(name = "Interaction.findByTypeinteraction", query = "SELECT i FROM Interaction i WHERE i.typeinteraction = :typeinteraction")
@NamedQuery(name = "Interaction.findByTimeLancement", query = "SELECT i FROM Interaction i WHERE i.timeLancement = :timeLancement")
public class Interaction implements Serializable {

    private static final long serialVersionUID = 1L;
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Basic(optional = false)
    @Column(name = "idinteraction")
    private Integer idinteraction;
    @Basic(optional = false)
    @NotNull
    @Size(min = 1, max = 50)
    @Column(name = "typeinteraction")
    private String typeinteraction;
    @Basic(optional = false)
    @NotNull
    @Column(name = "time_lancement")
    @Temporal(TemporalType.TIMESTAMP)
    private Date timeLancement;
    @JoinColumn(name = "idmodule", referencedColumnName = "idmodule")
    @ManyToOne
    private Module idmodule;
    @JoinColumn(name = "idsound", referencedColumnName = "idsound")
    @ManyToOne
    private Sound idsound;

    /**
     * Owner role for filtering (multi-profile system).
     * Stores the role name (e.g., 'dauphin', 'aras') for data isolation.
     * 
     * @author Anas EL HOUDI
     */
    @Column(name = "owner_role")
    private String ownerRole;

    public Interaction() {
    }

    public Interaction(Integer idinteraction) {
        this.idinteraction = idinteraction;
    }

    public Interaction(Integer idinteraction, String typeinteraction, Date timeLancement) {
        this.idinteraction = idinteraction;
        this.typeinteraction = typeinteraction;
        this.timeLancement = timeLancement;
    }

    public Integer getIdinteraction() {
        return idinteraction;
    }

    public void setIdinteraction(Integer idinteraction) {
        this.idinteraction = idinteraction;
    }

    public String getTypeinteraction() {
        return typeinteraction;
    }

    public void setTypeinteraction(String typeinteraction) {
        this.typeinteraction = typeinteraction;
    }

    public Date getTimeLancement() {
        return timeLancement;
    }

    public void setTimeLancement(Date timeLancement) {
        this.timeLancement = timeLancement;
    }

    public Module getIdmodule() {
        return idmodule;
    }

    public void setIdmodule(Module idmodule) {
        this.idmodule = idmodule;
    }

    public Sound getIdsound() {
        return idsound;
    }

    public void setIdsound(Sound idsound) {
        this.idsound = idsound;
    }

    public String getOwnerRole() {
        return ownerRole;
    }

    public void setOwnerRole(String ownerRole) {
        this.ownerRole = ownerRole;
    }

    @Override
    public int hashCode() {
        int hash = 0;
        hash += (idinteraction != null ? idinteraction.hashCode() : 0);
        return hash;
    }

    @Override
    public boolean equals(Object object) {
        if (!(object instanceof Interaction)) {
            return false;
        }
        Interaction other = (Interaction) object;
        return !((this.idinteraction == null && other.idinteraction != null)
                || (this.idinteraction != null && !this.idinteraction.equals(other.idinteraction)));
    }

    @Override
    public String toString() {
        return "peps.peps_back.items.Interaction[ idinteraction=" + idinteraction + " ]";
    }

}
