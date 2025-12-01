/**
 * @author BOUNOUA Ilyas and VAZEILLE Clément
 * @description This file defines the Sound entity, which represents a sound file in the system.
 */
package peps.peps_back.items;

import java.io.Serializable;
import java.util.Collection;
import javax.persistence.Basic;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.NamedQuery;
import javax.persistence.OneToMany;
import javax.persistence.Table;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;

@Entity
@Table(name = "sound")
@NamedQuery(name = "Sound.findAll", query = "SELECT s FROM Sound s")
@NamedQuery(name = "Sound.findByIdsound", query = "SELECT s FROM Sound s WHERE s.idsound = :idsound")
@NamedQuery(name = "Sound.findByNom", query = "SELECT s FROM Sound s WHERE s.nom = :nom")
@NamedQuery(name = "Sound.findByTypeSon", query = "SELECT s FROM Sound s WHERE s.typeSon = :typeSon")
public class Sound implements Serializable {

    private static final long serialVersionUID = 1L;
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Basic(optional = false)
    @Column(name = "idsound")
    private Integer idsound;
    @Basic(optional = false)
    @NotNull
    @Size(min = 1, max = 255)
    @Column(name = "nom")
    private String nom;
    @Basic(optional = false)
    @NotNull
    @Size(min = 1, max = 50)
    @Column(name = "type_son")
    private String typeSon;
    @Basic(optional = false)
    @NotNull
    @Size(min = 1, max = 10)
    @Column(name = "extension")
    private String extension;
    @Size(max = 500)
    @Column(name = "chemin")
    private String chemin;
    @OneToMany(mappedBy = "idsound")
    private Collection<Interaction> interactionCollection;

    public Sound() {
    }

    public Sound(Integer idsound) {
        this.idsound = idsound;
    }

    public Sound(Integer idsound, String nom, String typeSon, String extension) {
        this.idsound = idsound;
        this.nom = nom;
        this.typeSon = typeSon;
        this.extension = extension;
    }

    public Integer getIdsound() {
        return idsound;
    }

    public void setIdsound(Integer idsound) {
        this.idsound = idsound;
    }

    public String getNom() {
        return nom;
    }

    public void setNom(String nom) {
        this.nom = nom;
    }

    public String getTypeSon() {
        return typeSon;
    }

    public void setTypeSon(String typeSon) {
        this.typeSon = typeSon;
    }

    public String getExtension() {
        return extension;
    }

    public void setExtension(String extension) {
        this.extension = extension;
    }

    public String getChemin() {
        return chemin;
    }

    public void setChemin(String chemin) {
        this.chemin = chemin;
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
        hash += (idsound != null ? idsound.hashCode() : 0);
        return hash;
    }

    @Override
    public boolean equals(Object object) {

        if (!(object instanceof Sound)) {
            return false;
        }
        Sound other = (Sound) object;
        return this.idsound != null ? this.idsound.equals(other.idsound) : other.idsound == null;
    }

    @Override
    public String toString() {
        return "peps.peps_back.items.Sound[ idsound=" + idsound + " ]";
    }
    
}
