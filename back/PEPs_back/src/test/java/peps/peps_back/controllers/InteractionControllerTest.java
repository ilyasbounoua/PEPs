package peps.peps_back.controllers;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.GregorianCalendar;
import java.util.List;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Disabled;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import org.springframework.http.ResponseEntity;
import peps.peps_back.items.Interaction;
import peps.peps_back.repositories.InteractionRepository;
import org.junit.jupiter.api.extension.ExtendWith;
import static org.mockito.ArgumentMatchers.any;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.test.context.web.WebAppConfiguration;


public class InteractionControllerTest {
    
    private InteractionRepository interactionRepository;
    private InteractionController controller;

    @BeforeEach
    /**
     * Create mock and add it in the controller
     */
    public void setUp() {
        interactionRepository = mock(InteractionRepository.class);
        controller = new InteractionController(interactionRepository);
    }

    /**
     * Test of getAllInteractions method, of class InteractionController.
     */
    @Test
    public void testGetAllInteractions() {
        System.out.println("getAllInteractions");
        
        ArrayList<Interaction> listInter = new ArrayList<>();
        listInter.add(new Interaction(1, "sound", new GregorianCalendar(2014, Calendar.FEBRUARY, 11).getTime()));
        //when(interactionRepository.findAll()).thenReturn(listInter);
        when(interactionRepository.findByOwnerRoleAndTimeLancementAfter(any(), any())).thenReturn(listInter);
        when(interactionRepository.findByTimeLancementAfter(any())).thenReturn(listInter);
        
        String role = null;
        ResponseEntity result = controller.getAllInteractions(role);
        assertNotNull(result);
    }

}
