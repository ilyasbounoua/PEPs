package peps.peps_back.controllers;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import peps.peps_back.items.Module;
import peps.peps_back.repositories.ModuleRepository;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
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
    public ResponseEntity<?> updateModule(@PathVariable Integer id, @RequestBody ModuleDTO dto) {
        Module module = moduleRepository.findById(id).orElse(null);
        if (module == null) {
            Map<String, String> error = new HashMap<>();
            error.put("error", "Module introuvable");
            return ResponseEntity.notFound().build();
        }

        if (dto.getName() == null || dto.getName().trim().isEmpty()) {
            Map<String, String> error = new HashMap<>();
            error.put("error", "Le nom du module est obligatoire");
            return ResponseEntity.badRequest().body(error);
        }

        if (dto.getIp() == null || dto.getIp().trim().isEmpty()) {
            Map<String, String> error = new HashMap<>();
            error.put("error", "L'adresse IP est obligatoire");
            return ResponseEntity.badRequest().body(error);
        }

        if (!dto.getIp().matches("^(?:[0-9]{1,3}\\.){3}[0-9]{1,3}$")) {
            Map<String, String> error = new HashMap<>();
            error.put("error", "Format d'adresse IP invalide");
            return ResponseEntity.badRequest().body(error);
        }

        if (dto.getConfig() == null) {
            Map<String, String> error = new HashMap<>();
            error.put("error", "La configuration est obligatoire");
            return ResponseEntity.badRequest().body(error);
        }

        if (dto.getConfig().getVolume() < 0 || dto.getConfig().getVolume() > 100) {
            Map<String, String> error = new HashMap<>();
            error.put("error", "Le volume doit être entre 0 et 100");
            return ResponseEntity.badRequest().body(error);
        }

        if (!dto.getConfig().getMode().equals("Manuel") && !dto.getConfig().getMode().equals("Automatique")) {
            Map<String, String> error = new HashMap<>();
            error.put("error", "Le mode doit être 'Manuel' ou 'Automatique'");
            return ResponseEntity.badRequest().body(error);
        }
        
        module.setNom(dto.getName());
        module.setIpAdress(dto.getIp());
        module.setVolume(dto.getConfig().getVolume());
        module.setCurrentMode(dto.getConfig().getMode());
        module.setActif(dto.getConfig().isActif());
        module.setStatus(dto.getConfig().isActif() ? "actif" : "inactif");
        module.setLastSeen(new java.util.Date());
        
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

    @PostMapping
    public ResponseEntity<?> createModule(@RequestBody ModuleDTO dto) {
        if (dto.getName() == null || dto.getName().trim().isEmpty()) {
            Map<String, String> error = new HashMap<>();
            error.put("error", "Le nom du module est obligatoire");
            return ResponseEntity.badRequest().body(error);
        }

        if (dto.getIp() == null || dto.getIp().trim().isEmpty()) {
            Map<String, String> error = new HashMap<>();
            error.put("error", "L'adresse IP est obligatoire");
            return ResponseEntity.badRequest().body(error);
        }

        if (!dto.getIp().matches("^(?:[0-9]{1,3}\\.){3}[0-9]{1,3}$")) {
            Map<String, String> error = new HashMap<>();
            error.put("error", "Format d'adresse IP invalide");
            return ResponseEntity.badRequest().body(error);
        }

        if (dto.getConfig() == null) {
            Map<String, String> error = new HashMap<>();
            error.put("error", "La configuration est obligatoire");
            return ResponseEntity.badRequest().body(error);
        }

        Module module = new Module();
        module.setNom(dto.getName());
        module.setIpAdress(dto.getIp());
        module.setVolume(dto.getConfig().getVolume());
        module.setCurrentMode(dto.getConfig().getMode());
        module.setActif(dto.getConfig().isActif());
        module.setStatus(dto.getConfig().isActif() ? "actif" : "inactif");
        module.setLastSeen(new java.util.Date());
        
        module = moduleRepository.save(module);
        
        ModuleDTO createdDto = new ModuleDTO(
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
        
        return ResponseEntity.ok(createdDto);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<?> deleteModule(@PathVariable Integer id) {
        Module module = moduleRepository.findById(id).orElse(null);
        if (module == null) {
            Map<String, String> error = new HashMap<>();
            error.put("error", "Module introuvable");
            return ResponseEntity.notFound().build();
        }

        moduleRepository.delete(module);

        Map<String, String> response = new HashMap<>();
        response.put("message", "Module supprimé avec succès");
        return ResponseEntity.ok(response);
    }
}
