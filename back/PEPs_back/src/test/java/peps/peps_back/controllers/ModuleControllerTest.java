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
public class ModuleControllerTest {

    @Autowired
    private ModuleController instance;

    /**
     * Test of getAllModules method, of class ModuleController.
     */
    @Test
    public void testGetAllModules() {
        System.out.println("getAllModules");
        ResponseEntity<List<ModuleDTO>> result = instance.getAllModules(null);
        assertNotNull(result);
    }

    /**
     * Test of getModule method, of class ModuleController.
     */
    @Test
    public void testGetModule() {
        System.out.println("getModule");
        Integer id = 1;
        ResponseEntity<ModuleDTO> result = instance.getModule(id);
        assertNotNull(result);
    }

    /**
     * Test of updateModule method, of class ModuleController.
     */
    @Test
    public void testUpdateModule() {
        System.out.println("updateModule");
        Integer id = 1;
        ModuleDTO dto = new ModuleDTO();
        ResponseEntity result = instance.updateModule(id, dto, null);
        assertNotNull(result);
    }

    /**
     * Test of createModule method, of class ModuleController.
     */
    @Test
    public void testCreateModule() {
        System.out.println("createModule");
        ModuleDTO dto = new ModuleDTO();
        ResponseEntity result = instance.createModule(dto, null, null);
        assertNotNull(result);
    }

    /**
     * Test of deleteModule method, of class ModuleController.
     */
    @Test
    public void testDeleteModule() {
        System.out.println("deleteModule");
        Integer id = 1;
        ResponseEntity result = instance.deleteModule(id, null);
        assertNotNull(result);
    }

}
