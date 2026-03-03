package peps.peps_back.controllers;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.Calendar;
import java.util.GregorianCalendar;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import peps.peps_back.items.Interaction;
import peps.peps_back.repositories.InteractionRepository;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.test.context.web.WebAppConfiguration;

@ExtendWith(SpringExtension.class)
@ContextConfiguration(locations = {"file:src/main/webapp/WEB-INF/applicationContext.xml", "file:src/main/webapp/WEB-INF/dispatcher-servlet.xml"})
@WebAppConfiguration
public class DailyStatsControllerTest {
    
    private InteractionRepository interactionRepository;
    private DailyStatsController controller;

    @BeforeEach
    /**
     * Create mock and add it in the controller
     */
    public void setUp() {
        interactionRepository = mock(InteractionRepository.class);
        controller = new DailyStatsController(interactionRepository);
    }
      
    
    /**
     * Test of getDailyStats method, of class DailyStatsController.
     */
    @Test
    public void testGetDailyStats() {
        System.out.println("getDailyStats");
        
        String role = null;
        String startDate = "2026-01-01T12:08:56";
        String endDate = "2026-01-01T12:20:56";
        
        SimpleDateFormat isoFormat = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss");
        
        try {
            ArrayList<Interaction> listInter = new ArrayList<>();
            listInter.add(new Interaction(1, "sound", new GregorianCalendar(2014, Calendar.FEBRUARY, 11).getTime()));
            when(interactionRepository.findByTimeLancementBetween(isoFormat.parse(startDate),isoFormat.parse(endDate))).thenReturn(listInter);

            ResponseEntity result = controller.getDailyStats(role, startDate, endDate);
            assertNotNull(result);
        }
        catch (Exception e) {
            e.printStackTrace();
            assertTrue(false);
        }
    }
}
