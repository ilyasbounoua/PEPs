/**
 * @author Anas EL HOUDI
 * @description Repository for ModuleSound junction table.
 */
package peps.peps_back.repositories;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.transaction.annotation.Transactional;
import peps.peps_back.items.ModuleSound;

import java.util.List;
import java.util.Optional;

public interface ModuleSoundRepository extends JpaRepository<ModuleSound, Integer> {

    List<ModuleSound> findByModuleId(Integer moduleId);

    List<ModuleSound> findBySoundId(Integer soundId);

    Optional<ModuleSound> findByModuleIdAndSoundId(Integer moduleId, Integer soundId);

    @Modifying
    @Transactional
    @Query("DELETE FROM ModuleSound ms WHERE ms.moduleId = ?1 AND ms.soundId = ?2")
    void deleteByModuleIdAndSoundId(Integer moduleId, Integer soundId);
}
