package peps.peps_back.controllers;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import peps.peps_back.items.Sound;
import peps.peps_back.repositories.SoundRepository;

import java.util.List;
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
                s.getTypeSon()
            ))
            .collect(Collectors.toList());
        
        return ResponseEntity.ok(dtos);
    }
}
