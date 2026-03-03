/**
 * @author BOUNOUA Ilyas, VAZEILLE Clément, Anas EL HOUDI
 * @description This file defines the SoundController class, which handles CRUD operations for sounds, including file uploads and streaming.
 * 
 * Multi-profile system:
 * - Uses role to filter sounds by profile
 * - Each profile only sees its own sounds
 */
package peps.peps_back.controllers;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.Resource;
import org.springframework.core.io.UrlResource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import peps.peps_back.items.Sound;
import peps.peps_back.repositories.SoundRepository;
import peps.peps_back.repositories.UserRepository;
import peps.peps_back.items.User;
import peps.peps_back.services.AuditService;
import javax.persistence.OptimisticLockException;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/sounds")
@CrossOrigin(origins = "http://localhost:4200")
public class SoundController {

    private final SoundRepository soundRepository;

    // Files will be saved relative to Tomcat working directory (usually
    // tomcat/bin/sons)
    private static final String UPLOAD_DIR = "sons";

    @Autowired
    private AuditService auditService;

    @Autowired
    private UserRepository userRepository;

    public SoundController(SoundRepository soundRepository) {
        this.soundRepository = soundRepository;
    }

    /**
     * Lists sounds filtered by role.
     * 
     * @param role Role to filter by (e.g., 'dauphin', 'aras'). If null, returns
     *             all.
     */
    @GetMapping
    public ResponseEntity<List<SoundDTO>> getAllSounds(@RequestParam(required = false) String role) {
        List<Sound> sounds;

        // If role is provided, filter by owner_role
        if (role != null && !role.isEmpty()) {
            sounds = soundRepository.findByOwnerRole(role.toLowerCase());
        } else {
            sounds = soundRepository.findAll();
        }

        List<SoundDTO> dtos = sounds.stream()
                .map(s -> new SoundDTO(
                        s.getIdsound(),
                        s.getNom(),
                        s.getTypeSon(),
                        s.getExtension()))
                .collect(Collectors.toList());

        return ResponseEntity.ok(dtos);
    }

    @GetMapping("/{id}/file")
    public ResponseEntity<Resource> getSoundFile(@PathVariable Integer id) {
        Sound sound = soundRepository.findById(id).orElse(null);
        if (sound == null) {
            return ResponseEntity.notFound().build();
        }

        if (sound.getChemin() == null || sound.getChemin().isEmpty()) {
            return ResponseEntity.notFound().build();
        }

        try {
            Path filePath = Paths.get(sound.getChemin());
            Resource resource = new UrlResource(filePath.toUri());

            if (!resource.exists() || !resource.isReadable()) {
                return ResponseEntity.notFound().build();
            }

            String contentType = getContentType(sound.getExtension());
            String fileName = sound.getNom().replaceAll("[^a-zA-Z0-9\\s]", "_").replaceAll("\\s+", "_")
                    + "." + sound.getExtension();

            return ResponseEntity.ok()
                    .contentType(MediaType.parseMediaType(contentType))
                    .header(HttpHeaders.CONTENT_DISPOSITION, "inline; filename=\"" + fileName + "\"")
                    .body(resource);
        } catch (Exception e) {
            System.err.println("Error loading file: " + e.getMessage());
            e.printStackTrace();
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    private String getContentType(String extension) {
        switch (extension.toLowerCase()) {
            case "mp3":
                return "audio/mpeg";
            case "wav":
                return "audio/wav";
            case "ogg":
                return "audio/ogg";
            case "m4a":
                return "audio/mp4";
            default:
                return "application/octet-stream";
        }
    }

    @PostMapping(consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<?> uploadSound(
            @RequestParam("name") String name,
            @RequestParam("type") String type,
            @RequestParam("file") MultipartFile file,
            @RequestParam(value = "role", required = false) String role,
            @RequestHeader(value = "X-User-Login", required = false) String login) {

        // Permission Check
        if (login != null) {
            User user = userRepository.findByLogin(login).orElse(null);
            if (user != null && !"admin".equals(user.getRole()) && "viewer".equals(user.getPermission())) {
                Map<String, String> error = new HashMap<>();
                error.put("error", "Accès refusé : Vous avez la permission Viewer uniquement.");
                return ResponseEntity.status(403).body(error);
            }
        }

        System.out.println("=== Upload Sound Request ===");
        System.out.println("Name: " + name);
        System.out.println("Type: " + type);
        System.out.println("Role: " + role);
        System.out.println("File: " + (file != null ? file.getOriginalFilename() : "null"));
        System.out.println("File size: " + (file != null ? file.getSize() : 0));

        if (file == null || file.isEmpty()) {
            Map<String, String> error = new HashMap<>();
            error.put("error", "Le fichier est vide ou manquant");
            return ResponseEntity.badRequest().body(error);
        }

        if (name == null || name.trim().isEmpty()) {
            Map<String, String> error = new HashMap<>();
            error.put("error", "Le nom est obligatoire");
            return ResponseEntity.badRequest().body(error);
        }

        if (type == null || type.trim().isEmpty()) {
            Map<String, String> error = new HashMap<>();
            error.put("error", "Le type est obligatoire");
            return ResponseEntity.badRequest().body(error);
        }

        try {
            String originalFileName = file.getOriginalFilename();
            String extension = originalFileName.substring(originalFileName.lastIndexOf(".") + 1);

            if (!extension.matches("mp3|wav|ogg|m4a")) {
                Map<String, String> error = new HashMap<>();
                error.put("error", "Format de fichier non supporté. Utilisez mp3, wav, ogg ou m4a");
                return ResponseEntity.badRequest().body(error);
            }

            // Create directory structure: sons/type/
            Path typeDir = Paths.get(UPLOAD_DIR, type);
            Files.createDirectories(typeDir);

            // Generate unique filename
            String sanitizedName = name.replaceAll("[^a-zA-Z0-9\\s]", "_").replaceAll("\\s+", "_");
            String fileName = sanitizedName + "_" + System.currentTimeMillis() + "." + extension;
            Path filePath = typeDir.resolve(fileName);

            // Save file to disk
            Files.copy(file.getInputStream(), filePath, StandardCopyOption.REPLACE_EXISTING);

            // Save metadata to database with file path
            Sound sound = new Sound();
            sound.setNom(name);
            sound.setTypeSon(type);
            sound.setExtension(extension);
            sound.setChemin(filePath.toString());

            // Set owner role for multi-profile filtering
            if (role != null && !role.isEmpty()) {
                sound.setOwnerRole(role.toLowerCase());
            }

            sound = soundRepository.save(sound);

            System.out.println("Sound saved to file: " + filePath.toString() + ", ID: " + sound.getIdsound());

            // Log upload in audit
            String newValue = String.format(
                    "{\"name\":\"%s\",\"type\":\"%s\",\"extension\":\"%s\",\"path\":\"%s\"}",
                    sound.getNom(), sound.getTypeSon(), sound.getExtension(), sound.getChemin());
            String userLogin = (login != null) ? login : "unknown";
            auditService.log("CREATE", "sound", sound.getIdsound(), sound.getNom(),
                    sound.getOwnerRole(), userLogin, null, newValue, "Upload de fichier son");

            SoundDTO dto = new SoundDTO(sound.getIdsound(), sound.getNom(), sound.getTypeSon(), sound.getExtension());
            return ResponseEntity.ok(dto);

        } catch (IOException e) {
            System.err.println("IOException during upload: " + e.getMessage());
            e.printStackTrace();
            Map<String, String> error = new HashMap<>();
            error.put("error", "Erreur lors de la sauvegarde du fichier: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(error);
        } catch (Exception e) {
            System.err.println("Exception during upload: " + e.getMessage());
            e.printStackTrace();
            Map<String, String> error = new HashMap<>();
            error.put("error", "Erreur inattendue: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(error);
        }
    }

    @PutMapping("/{id}")
    public ResponseEntity<?> updateSound(@PathVariable Integer id, @RequestBody SoundDTO soundDTO,
            @RequestHeader(value = "X-User-Login", required = false) String login) {

        // Permission Check
        if (login != null) {
            User user = userRepository.findByLogin(login).orElse(null);
            if (user != null && !"admin".equals(user.getRole()) && "viewer".equals(user.getPermission())) {
                Map<String, String> error = new HashMap<>();
                error.put("error", "Accès refusé : Vous avez la permission Viewer uniquement.");
                return ResponseEntity.status(403).body(error);
            }
        }

        Sound sound = soundRepository.findById(id).orElse(null);
        if (sound == null) {
            Map<String, String> error = new HashMap<>();
            error.put("error", "Son introuvable");
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(error);
        }

        if (soundDTO.getName() == null || soundDTO.getName().trim().isEmpty()) {
            Map<String, String> error = new HashMap<>();
            error.put("error", "Le nom est obligatoire");
            return ResponseEntity.badRequest().body(error);
        }

        if (soundDTO.getType() == null || soundDTO.getType().trim().isEmpty()) {
            Map<String, String> error = new HashMap<>();
            error.put("error", "Le type est obligatoire");
            return ResponseEntity.badRequest().body(error);
        }

        // Capture old values
        String oldValue = String.format(
                "{\"name\":\"%s\",\"type\":\"%s\"}",
                sound.getNom(), sound.getTypeSon());

        // Update only name and type, keep file path unchanged
        sound.setNom(soundDTO.getName());
        sound.setTypeSon(soundDTO.getType());

        try {
            sound = soundRepository.save(sound);

            // Log update
            String newValue = String.format(
                    "{\"name\":\"%s\",\"type\":\"%s\"}",
                    sound.getNom(), sound.getTypeSon());
            String userLogin = (login != null) ? login : "unknown";
            auditService.log("UPDATE", "sound", sound.getIdsound(), sound.getNom(),
                    sound.getOwnerRole(), userLogin, oldValue, newValue, "Modification du son");

            SoundDTO responseDTO = new SoundDTO(sound.getIdsound(), sound.getNom(), sound.getTypeSon(),
                    sound.getExtension());
            return ResponseEntity.ok(responseDTO);

        } catch (javax.persistence.OptimisticLockException e) {
            Map<String, String> error = new HashMap<>();
            error.put("error", "Conflit de modification : le son a été modifié par un autre utilisateur.");
            return ResponseEntity.status(409).body(error);
        }
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<?> deleteSound(@PathVariable Integer id,
            @RequestHeader(value = "X-User-Login", required = false) String login) {

        // Permission Check
        if (login != null) {
            User user = userRepository.findByLogin(login).orElse(null);
            if (user != null && !"admin".equals(user.getRole()) && "viewer".equals(user.getPermission())) {
                Map<String, String> error = new HashMap<>();
                error.put("error", "Accès refusé : Vous avez la permission Viewer uniquement.");
                return ResponseEntity.status(403).body(error);
            }
        }

        Sound sound = soundRepository.findById(id).orElse(null);
        if (sound == null) {
            Map<String, String> error = new HashMap<>();
            error.put("error", "Son introuvable");
            return ResponseEntity.notFound().build();
        }

        // Log deletion (before file delete just in case)
        String oldValue = String.format(
                "{\"name\":\"%s\",\"type\":\"%s\",\"path\":\"%s\"}",
                sound.getNom(), sound.getTypeSon(), sound.getChemin());
        String userLogin = (login != null) ? login : "unknown";
        auditService.log("DELETE", "sound", id, sound.getNom(),
                sound.getOwnerRole(), userLogin, oldValue, null, "Suppression du son");

        // Delete file from disk
        if (sound.getChemin() != null && !sound.getChemin().isEmpty()) {
            try {
                Path filePath = Paths.get(sound.getChemin());
                Files.deleteIfExists(filePath);
                System.out.println("Deleted file: " + filePath.toString());
            } catch (IOException e) {
                System.err.println("Error deleting file: " + e.getMessage());
                e.printStackTrace();
            }
        }

        soundRepository.delete(sound);

        Map<String, String> response = new HashMap<>();
        response.put("message", "Son supprimé avec succès");
        return ResponseEntity.ok(response);
    }
}
