/**
 * REST controller that manages sound metadata and file storage operations.
 *
 * <p>Multi-profile behavior is handled with the {@code role} filter so each profile only accesses
 * its own sounds.
 *
 * @author BOUNOUA Ilyas, VAZEILLE Clément, Anas EL HOUDI, Haytam BEN SRIBIT
 */
package peps.peps_back.controllers;

import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import io.minio.errors.ErrorResponseException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.core.io.Resource;
import org.springframework.core.io.UrlResource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import peps.peps_back.items.Sound;
import peps.peps_back.items.User;
import peps.peps_back.items.ModuleSound;
import peps.peps_back.items.Module;
import peps.peps_back.repositories.SoundRepository;
import peps.peps_back.repositories.UserRepository;
import peps.peps_back.repositories.ModuleSoundRepository;
import peps.peps_back.repositories.ModuleRepository;
import peps.peps_back.services.AuditService;
import peps.peps_back.services.MinioStorageService;

@RestController
@RequestMapping("/sounds")
@CrossOrigin(origins = "http://localhost:4200")
public class SoundController {

    private final SoundRepository soundRepository;
    private final MinioStorageService minioStorageService;

    @Autowired
    private AuditService auditService;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private ModuleSoundRepository moduleSoundRepository;

    @Autowired
    private ModuleRepository moduleRepository;

    public SoundController(SoundRepository soundRepository, MinioStorageService minioStorageService) {
        this.soundRepository = soundRepository;
        this.minioStorageService = minioStorageService;
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

    /**
     * Streams the sound file associated with the given sound identifier.
     *
     * @param id sound identifier
     * @return audio resource when found, or a not found/server error response
     */
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
            byte[] fileContent = minioStorageService.downloadSound(sound.getChemin());
            Resource resource = new ByteArrayResource(fileContent);

            String contentType = getContentType(sound.getExtension());
            String fileName = sound.getNom().replaceAll("[^a-zA-Z0-9\\s]", "_").replaceAll("\\s+", "_")
                    + "." + sound.getExtension().replace(".", "");

            return ResponseEntity.ok()
                    .contentType(MediaType.parseMediaType(contentType))
                    .header(HttpHeaders.CONTENT_DISPOSITION, "inline; filename=\"" + fileName + "\"")
                    .body(resource);
        } catch (ErrorResponseException e) {
            if (e.errorResponse() != null && "NoSuchKey".equalsIgnoreCase(e.errorResponse().code())) {
                return ResponseEntity.notFound().build();
            }
            System.err.println("MinIO error loading file: " + e.getMessage());
            e.printStackTrace();
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        } catch (Exception e) {
            System.err.println("Error loading file: " + e.getMessage());
            e.printStackTrace();
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    /**
     * Resolves an audio MIME type from a file extension.
     *
     * @param extension file extension with or without leading dot
     * @return MIME type corresponding to the extension, or
     *         {@code application/octet-stream}
     */
    private String getContentType(String extension) {
        String normalizedExtension = extension.replace(".", "").toLowerCase();
        switch (normalizedExtension) {
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

    @Autowired
    private peps.peps_back.services.AudioJobPublisher audioJobPublisher;

    /**
     * NOTE: Additional Redis streams and workers can be added to this controller if needed for
     * other asynchronous tasks. However, currently, only the sound upload process is implemented
     * with this worker-based pattern because it's significantly heavier (I/O and binary processing)
     * than other metadata operations.
     */

    /**
     * Uploads a sound file.
     * <p>
     * This version uses the asynchronous Redis pipeline (Pipeline 1).
     * The file metadata is saved to the database immediately, and a job is queued
     * in Redis for workers to handle the binary storage (MinIO) and path update.
     *
     * @param name  logical sound name
     * @param type  sound category/type
     * @param file  binary multipart file
     * @param role  optional owner role used for profile isolation
     * @param login optional user login for permission checks and auditing
     * @return 202 Accepted with Sound metadata
     */
    @PostMapping(consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<?> uploadSound(
            @RequestParam("name") String name,
            @RequestParam("type") String type,
            @RequestParam("file") MultipartFile file,
            @RequestParam(value = "role", required = false) String role,
            @RequestHeader(value = "X-User-Login", required = false) String login) {

        if (login != null) {
            User user = userRepository.findByLogin(login).orElse(null);
            if (user != null && !"admin".equals(user.getRole()) && "viewer".equals(user.getPermission())) {
                Map<String, String> error = new HashMap<>();
                error.put("error", "Access denied: Viewer permission only.");
                return ResponseEntity.status(403).body(error);
            }
        }

        if (file == null || file.isEmpty()) {
            Map<String, String> error = new HashMap<>();
            error.put("error", "The file is empty or missing");
            return ResponseEntity.badRequest().body(error);
        }

        if (name == null || name.trim().isEmpty()) {
            Map<String, String> error = new HashMap<>();
            error.put("error", "Name is required");
            return ResponseEntity.badRequest().body(error);
        }

        if (type == null || type.trim().isEmpty()) {
            Map<String, String> error = new HashMap<>();
            error.put("error", "Type is required");
            return ResponseEntity.badRequest().body(error);
        }

        try {
            String originalFileName = file.getOriginalFilename();
            if (originalFileName == null || !originalFileName.contains(".")) {
                Map<String, String> error = new HashMap<>();
                error.put("error", "Invalid filename");
                return ResponseEntity.badRequest().body(error);
            }

            String extension = originalFileName.substring(originalFileName.lastIndexOf(".") + 1).toLowerCase();
            if (!extension.matches("mp3|wav|ogg|m4a")) {
                Map<String, String> error = new HashMap<>();
                error.put("error", "Unsupported file format. Use mp3, wav, ogg or m4a");
                return ResponseEntity.badRequest().body(error);
            }

            // 1. Create and persist Sound entity (without path yet)
            Sound sound = new Sound();
            sound.setNom(name);
            sound.setTypeSon(type);
            sound.setExtension(extension);
            sound.setChemin(null); // Will be updated by StorageWorker

            if (role != null && !role.isEmpty()) {
                sound.setOwnerRole(role.toLowerCase());
            }

            sound = soundRepository.save(sound);
            Integer soundId = sound.getIdsound();

            // 2. Prepare and publish Redis Job (Pipeline 1)
            String jobId = java.util.UUID.randomUUID().toString();
            String audioBase64 = java.util.Base64.getEncoder().encodeToString(file.getBytes());
            String contentType = getContentType(extension);

            Map<String, String> payload = new HashMap<>();
            payload.put("jobId", jobId);
            payload.put("soundId", soundId.toString());
            payload.put("audioBase64", audioBase64);
            payload.put("contentType", contentType);

            audioJobPublisher.publishUploadJob(payload);

            System.out.println("Sound upload job queued. ID: " + soundId + ", JobID: " + jobId);

            // 3. Audit Log
            String newValue = String.format(
                    "{\"name\":\"%s\",\"type\":\"%s\",\"extension\":\"%s\",\"jobId\":\"%s\"}",
                    sound.getNom(), sound.getTypeSon(), sound.getExtension(), jobId);
            String userLogin = (login != null) ? login : "unknown";
            auditService.log("CREATE_ASYNC", "sound", soundId, sound.getNom(),
                    sound.getOwnerRole(), userLogin, null, newValue, "Upload de fichier son");

            SoundDTO dto = new SoundDTO(soundId, sound.getNom(), sound.getTypeSon(), sound.getExtension());
            return ResponseEntity.status(HttpStatus.ACCEPTED).body(dto);

        } catch (IOException e) {
            System.err.println("IOException during async upload trigger: " + e.getMessage());
            Map<String, String> error = new HashMap<>();
            error.put("error", "Error reading file content: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(error);
        } catch (Exception e) {
            System.err.println("Exception during async upload trigger: " + e.getMessage());
            Map<String, String> error = new HashMap<>();
            error.put("error", "Unexpected error: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(error);
        }
    }

    /**
     * Updates sound metadata for an existing sound.
     *
     * @param id       sound identifier
     * @param soundDTO payload carrying updated metadata
     * @param login    optional user login for permission checks and auditing
     * @return updated sound DTO or an error payload
     */
    @PutMapping("/{id}")
    public ResponseEntity<?> updateSound(@PathVariable Integer id, @RequestBody SoundDTO soundDTO,
            @RequestHeader(value = "X-User-Login", required = false) String login) {

        if (login != null) {
            User user = userRepository.findByLogin(login).orElse(null);
            if (user != null && !"admin".equals(user.getRole()) && "viewer".equals(user.getPermission())) {
                Map<String, String> error = new HashMap<>();
                error.put("error", "Access denied: Viewer permission only.");
                return ResponseEntity.status(403).body(error);
            }
        }

        Sound sound = soundRepository.findById(id).orElse(null);
        if (sound == null) {
            Map<String, String> error = new HashMap<>();
            error.put("error", "Sound not found");
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(error);
        }

        if (soundDTO.getName() == null || soundDTO.getName().trim().isEmpty()) {
            Map<String, String> error = new HashMap<>();
            error.put("error", "Name is required");
            return ResponseEntity.badRequest().body(error);
        }

        if (soundDTO.getType() == null || soundDTO.getType().trim().isEmpty()) {
            Map<String, String> error = new HashMap<>();
            error.put("error", "Type is required");
            return ResponseEntity.badRequest().body(error);
        }

        String oldValue = String.format(
                "{\"name\":\"%s\",\"type\":\"%s\"}",
                sound.getNom(), sound.getTypeSon());

        sound.setNom(soundDTO.getName());
        sound.setTypeSon(soundDTO.getType());

        try {
            sound = soundRepository.save(sound);

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
            error.put("error", "Modification conflict: sound was updated by another user.");
            return ResponseEntity.status(409).body(error);
        }
    }

    /**
     * Deletes a sound metadata record and its backing object storage file when
     * available.
     *
     * @param id    sound identifier
     * @param login optional user login for permission checks and auditing
     * @return confirmation payload or an error response
     */
    @DeleteMapping("/{id}")
    public ResponseEntity<?> deleteSound(@PathVariable Integer id,
            @RequestHeader(value = "X-User-Login", required = false) String login) {

        if (login != null) {
            User user = userRepository.findByLogin(login).orElse(null);
            if (user != null && !"admin".equals(user.getRole()) && "viewer".equals(user.getPermission())) {
                Map<String, String> error = new HashMap<>();
                error.put("error", "Access denied: Viewer permission only.");
                return ResponseEntity.status(403).body(error);
            }
        }

        Sound sound = soundRepository.findById(id).orElse(null);
        if (sound == null) {
            Map<String, String> error = new HashMap<>();
            error.put("error", "Sound not found");
            return ResponseEntity.notFound().build();
        }

        String oldValue = String.format(
                "{\"name\":\"%s\",\"type\":\"%s\",\"path\":\"%s\"}",
                sound.getNom(), sound.getTypeSon(), sound.getChemin());
        String userLogin = (login != null) ? login : "unknown";
        auditService.log("DELETE", "sound", id, sound.getNom(),
                sound.getOwnerRole(), userLogin, oldValue, null, "Suppression du son");

        if (sound.getChemin() != null && !sound.getChemin().isEmpty()) {
            try {
                minioStorageService.deleteSound(sound.getChemin());
                System.out.println("Deleted object: " + sound.getChemin());
            } catch (Exception e) {
                System.err.println("Error deleting object: " + e.getMessage());
                e.printStackTrace();
            }
        }

        soundRepository.delete(sound);

        Map<String, String> response = new HashMap<>();
        response.put("message", "Sound deleted successfully");
        return ResponseEntity.ok(response);
    }

    // ========== Sound-Module Assignment Query ==========

    /**
     * Returns the list of modules a sound is assigned to.
     */
    @GetMapping("/{id}/modules")
    public ResponseEntity<?> getSoundModules(@PathVariable Integer id) {
        Sound sound = soundRepository.findById(id).orElse(null);
        if (sound == null) {
            return ResponseEntity.notFound().build();
        }

        List<ModuleSound> assignments = moduleSoundRepository.findBySoundId(id);
        List<Map<String, Object>> modules = assignments.stream()
                .map(ms -> moduleRepository.findById(ms.getModuleId()).orElse(null))
                .filter(m -> m != null)
                .map(m -> {
                    Map<String, Object> info = new HashMap<>();
                    info.put("id", m.getIdmodule());
                    info.put("name", m.getNom());
                    return info;
                })
                .collect(Collectors.toList());

        return ResponseEntity.ok(modules);
    }
}
