/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
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
import javax.persistence.NamedQueries;
import javax.persistence.NamedQuery;
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
@Table(name = "interaction")
@NamedQueries({
    @NamedQuery(name = "Interaction.findAll", query = "SELECT i FROM Interaction i"),
    @NamedQuery(name = "Interaction.findByIdinteraction", query = "SELECT i FROM Interaction i WHERE i.idinteraction = :idinteraction"),
    @NamedQuery(name = "Interaction.findByTypeinteraction", query = "SELECT i FROM Interaction i WHERE i.typeinteraction = :typeinteraction"),
    @NamedQuery(name = "Interaction.findByTimeLancement", query = "SELECT i FROM Interaction i WHERE i.timeLancement = :timeLancement")})
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

    @Override
    public int hashCode() {
        int hash = 0;
        hash += (idinteraction != null ? idinteraction.hashCode() : 0);
        return hash;
    }

    @Override
    public boolean equals(Object object) {
        // TODO: Warning - this method won't work in the case the id fields are not set
        if (!(object instanceof Interaction)) {
            return false;
        }
        Interaction other = (Interaction) object;
        if ((this.idinteraction == null && other.idinteraction != null) || (this.idinteraction != null && !this.idinteraction.equals(other.idinteraction))) {
            return false;
        }
        return true;
    }

    @Override
    public String toString() {
        return "peps.peps_back.items.Interaction[ idinteraction=" + idinteraction + " ]";
    }
    
}
