package peps.peps_back.controllers;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import peps.peps_back.items.Sound;
import peps.peps_back.repositories.SoundRepository;

import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/sounds")
@CrossOrigin(origins = "http://localhost:4200")
public class SoundController {

    @Autowired
    private SoundRepository soundRepository;

    @GetMapping
    public ResponseEntity<List<SoundDTO>> getAllSounds() {
        List<Sound> sounds = soundRepository.findAll();
        
        List<SoundDTO> dtos = sounds.stream()
            .map(s -> new SoundDTO(
                s.getIdsound(),
                s.getNom(),
                s.getTypeSon(),
                s.getExtension()
            ))
            .collect(Collectors.toList());
        
        return ResponseEntity.ok(dtos);
    }

    @GetMapping("/{id}/file")
    public ResponseEntity<byte[]> getSoundFile(@PathVariable Integer id) {
        Sound sound = soundRepository.findById(id).orElse(null);
        if (sound == null) {
            return ResponseEntity.notFound().build();
        }

        if (sound.getDonneesAudio() == null || sound.getDonneesAudio().length == 0) {
            return ResponseEntity.notFound().build();
        }

        String contentType = getContentType(sound.getExtension());
        String fileName = sound.getNom().replaceAll("[^a-zA-Z0-9\\s]", "_").replaceAll("\\s+", "_") 
                        + "." + sound.getExtension();

        return ResponseEntity.ok()
                .contentType(MediaType.parseMediaType(contentType))
                .header(HttpHeaders.CONTENT_DISPOSITION, "inline; filename=\"" + fileName + "\"")
                .body(sound.getDonneesAudio());
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
            @RequestParam("file") MultipartFile file) {

        System.out.println("=== Upload Sound Request ===");
        System.out.println("Name: " + name);
        System.out.println("Type: " + type);
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

            // Store audio data in database
            byte[] audioData = file.getBytes();

            Sound sound = new Sound();
            sound.setNom(name);
            sound.setTypeSon(type);
            sound.setExtension(extension);
            sound.setDonneesAudio(audioData);
            sound = soundRepository.save(sound);
            
            System.out.println("Sound saved to database with ID: " + sound.getIdsound() + ", size: " + audioData.length + " bytes");

            SoundDTO dto = new SoundDTO(sound.getIdsound(), sound.getNom(), sound.getTypeSon(), sound.getExtension());
            return ResponseEntity.ok(dto);

        } catch (IOException e) {
            System.err.println("IOException during upload: " + e.getMessage());
            e.printStackTrace();
            Map<String, String> error = new HashMap<>();
            error.put("error", "Erreur lors de la lecture du fichier: " + e.getMessage());
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
    public ResponseEntity<?> updateSound(@PathVariable Integer id, @RequestBody SoundDTO soundDTO) {
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

        // Update only name and type, keep audio data unchanged
        sound.setNom(soundDTO.getName());
        sound.setTypeSon(soundDTO.getType());
        sound = soundRepository.save(sound);

        SoundDTO responseDTO = new SoundDTO(sound.getIdsound(), sound.getNom(), sound.getTypeSon(), sound.getExtension());
        return ResponseEntity.ok(responseDTO);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<?> deleteSound(@PathVariable Integer id) {
        Sound sound = soundRepository.findById(id).orElse(null);
        if (sound == null) {
            Map<String, String> error = new HashMap<>();
            error.put("error", "Son introuvable");
            return ResponseEntity.notFound().build();
        }

        soundRepository.delete(sound);

        Map<String, String> response = new HashMap<>();
        response.put("message", "Son supprimé avec succès");
        return ResponseEntity.ok(response);
    }
}
