/**
 * @author Anas EL HOUDI
 * @description Junction entity linking modules to sounds (many-to-many).
 */
package peps.peps_back.items;

import java.io.Serializable;
import javax.persistence.Basic;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.Table;
import javax.persistence.UniqueConstraint;

@Entity
@Table(name = "module_sound", uniqueConstraints = {
        @UniqueConstraint(columnNames = { "module_id", "sound_id" })
})
public class ModuleSound implements Serializable {

    private static final long serialVersionUID = 1L;

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @Basic(optional = false)
    @Column(name = "module_id")
    private Integer moduleId;

    @Basic(optional = false)
    @Column(name = "sound_id")
    private Integer soundId;

    public ModuleSound() {
    }

    public ModuleSound(Integer moduleId, Integer soundId) {
        this.moduleId = moduleId;
        this.soundId = soundId;
    }

    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public Integer getModuleId() {
        return moduleId;
    }

    public void setModuleId(Integer moduleId) {
        this.moduleId = moduleId;
    }

    public Integer getSoundId() {
        return soundId;
    }

    public void setSoundId(Integer soundId) {
        this.soundId = soundId;
    }
}
