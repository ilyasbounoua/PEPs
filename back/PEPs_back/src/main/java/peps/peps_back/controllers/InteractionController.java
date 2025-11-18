package peps.peps_back.controllers;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import peps.peps_back.items.Interaction;
import peps.peps_back.repositories.InteractionRepository;

import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/interactions")
@CrossOrigin(origins = "http://localhost:4200")
public class InteractionController {

    @Autowired
    private InteractionRepository interactionRepository;

    @GetMapping
    public ResponseEntity<List<InteractionDTO>> getAllInteractions() {
        List<Interaction> interactions = interactionRepository.findAll();
        
        List<InteractionDTO> dtos = interactions.stream()
            .map(i -> new InteractionDTO(
                i.getIdinteraction(),
                i.getTimeLancement(),
                i.getIdmodule() != null ? i.getIdmodule().getNom() : "Unknown",
                i.getTypeinteraction()
            ))
            .sorted((a, b) -> b.getDate().compareTo(a.getDate()))
            .collect(Collectors.toList());
        
        return ResponseEntity.ok(dtos);
    }
}
