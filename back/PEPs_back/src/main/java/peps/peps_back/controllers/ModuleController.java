package peps.peps_back.controllers;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import peps.peps_back.items.Module;
import peps.peps_back.repositories.ModuleRepository;

import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/modules")
@CrossOrigin(origins = "http://localhost:4200")
public class ModuleController {

    @Autowired
    private ModuleRepository moduleRepository;

    @GetMapping
    public ResponseEntity<List<ModuleDTO>> getAllModules() {
        List<Module> modules = moduleRepository.findAll();
        
        List<ModuleDTO> dtos = modules.stream()
            .map(m -> new ModuleDTO(
                m.getIdmodule(),
                m.getNom(),
                "",
                m.getActif() ? "Actif" : "Inactif",
                m.getIpAdress(),
                new ModuleConfigDTO(
                    m.getVolume(),
                    m.getCurrentMode(),
                    m.getActif(),
                    false
                )
            ))
            .collect(Collectors.toList());
        
        return ResponseEntity.ok(dtos);
    }

    @GetMapping("/{id}")
    public ResponseEntity<ModuleDTO> getModule(@PathVariable Integer id) {
        Module m = moduleRepository.findById(id).orElse(null);
        if (m == null) {
            return ResponseEntity.notFound().build();
        }
        
        ModuleDTO dto = new ModuleDTO(
            m.getIdmodule(),
            m.getNom(),
            "",
            m.getActif() ? "Actif" : "Inactif",
            m.getIpAdress(),
            new ModuleConfigDTO(
                m.getVolume(),
                m.getCurrentMode(),
                m.getActif(),
                false
            )
        );
        
        return ResponseEntity.ok(dto);
    }

    @PutMapping("/{id}")
    public ResponseEntity<ModuleDTO> updateModule(@PathVariable Integer id, @RequestBody ModuleDTO dto) {
        Module module = moduleRepository.findById(id).orElse(null);
        if (module == null) {
            return ResponseEntity.notFound().build();
        }
        
        module.setNom(dto.getName());
        module.setIpAdress(dto.getIp());
        module.setVolume(dto.getConfig().getVolume());
        module.setCurrentMode(dto.getConfig().getMode());
        module.setActif(dto.getConfig().isActif());
        module.setStatus(dto.getConfig().isActif() ? "actif" : "inactif");
        
        moduleRepository.save(module);
        
        ModuleDTO updatedDto = new ModuleDTO(
            module.getIdmodule(),
            module.getNom(),
            dto.getLocation(),
            module.getActif() ? "Actif" : "Inactif",
            module.getIpAdress(),
            new ModuleConfigDTO(
                module.getVolume(),
                module.getCurrentMode(),
                module.getActif(),
                dto.getConfig().isSon()
            )
        );
        
        return ResponseEntity.ok(updatedDto);
    }
}
