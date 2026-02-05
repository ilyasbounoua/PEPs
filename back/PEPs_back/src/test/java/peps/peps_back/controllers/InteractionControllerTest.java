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
public class InteractionControllerTest {

    @Autowired
    private InteractionController instance;

    /**
     * Test of getAllInteractions method, of class InteractionController.
     */
    @Test
    public void testGetAllInteractions() {
        System.out.println("getAllInteractions");
        ResponseEntity<List<InteractionDTO>> result = instance.getAllInteractions(null);
        assertNotNull(result);
    }

}
