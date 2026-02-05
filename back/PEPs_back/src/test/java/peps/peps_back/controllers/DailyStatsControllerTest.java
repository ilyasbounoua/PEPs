package peps.peps_back.controllers;

import java.util.List;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.test.context.web.WebAppConfiguration;

@ExtendWith(SpringExtension.class)
@ContextConfiguration(locations = {"file:src/main/webapp/WEB-INF/applicationContext.xml", "file:src/main/webapp/WEB-INF/dispatcher-servlet.xml"})
@WebAppConfiguration
public class DailyStatsControllerTest {

    @Autowired
    private DailyStatsController instance;

    /**
     * Test of getDailyStats method, of class DailyStatsController.
     */
    @Test
    public void testGetDailyStats() {
        System.out.println("getDailyStats");
        ResponseEntity<List<DailyDataDTO>> result = instance.getDailyStats(null, null, null);
        assertNotNull(result);
    }

}
