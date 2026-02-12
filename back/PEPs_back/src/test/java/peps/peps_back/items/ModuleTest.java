/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/UnitTests/JUnit5TestClass.java to edit this template
 */
package peps.peps_back.items;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collection;
import java.util.Date;
import java.util.GregorianCalendar;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

/**
 *
 * @author Clément
 */
public class ModuleTest {
    
    Module module;
    static Module.ModuleConfig config;
    
    public ModuleTest() {
        
    }
    
    @BeforeAll
    public static void setUpClass() {
        config = new Module.ModuleConfig();
        config.setActif(true);
        config.setCurrentMode("REPEAT");
        config.setIdmodule(2);
        config.setIpAdress("0.0.0.0");
        config.setLastSeen(new GregorianCalendar(2014, Calendar.FEBRUARY, 11).getTime());
        config.setNom("Best Module");
        config.setStatus("Active");
        config.setVolume(10);
    }
    
    @AfterAll
    public static void tearDownClass() {
    }
    
    @BeforeEach
    public void setUp() {
        module = new Module(config);
    }
    
    @AfterEach
    public void tearDown() {
    }


    
    
    /**
     * Test of getIdmodule method, of class Module.
     */
    @Test
    public void testGettersSetters() {        
        assertEquals(module.getActif(),true);
        assertEquals(module.getCurrentMode(),"REPEAT");
        assertEquals(module.getIdmodule(),2);
        assertEquals(module.getIpAdress(),"0.0.0.0");
        assertEquals(module.getLastSeen(),new GregorianCalendar(2014, Calendar.FEBRUARY, 11).getTime());
        assertEquals(module.getNom(),"Best Module");
        assertEquals(module.getStatus(),"Active");
        assertEquals(module.getVolume(),10);
        
        module.setActif(false);
        module.setCurrentMode("RzEPEAT");
        module.setIdmodule(21);
        module.setIpAdress("8.8.8.8");
        module.setLastSeen(new GregorianCalendar(2014, Calendar.FEBRUARY, 15).getTime());
        module.setNom("Besttt Module");
        module.setStatus("Off");
        module.setVolume(0);
        
        assertEquals(module.getActif(),false);
        assertEquals(module.getCurrentMode(),"RzEPEAT");
        assertEquals(module.getIdmodule(),21);
        assertEquals(module.getIpAdress(),"8.8.8.8");
        assertEquals(module.getLastSeen(),new GregorianCalendar(2014, Calendar.FEBRUARY, 15).getTime());
        assertEquals(module.getNom(),"Besttt Module");
        assertEquals(module.getStatus(),"Off");
        assertEquals(module.getVolume(),0);
    }

    

    /**
     * Test of getInteractionCollection and setInteractionCollection methods, of class Module.
     */
    @Test
    public void testInteractionCollection() {
        System.out.println("getInteractionCollection");
                
        Collection<Interaction> result = module.getInteractionCollection();
        assertEquals(null, result);
        
        Collection<Interaction> expResult = new ArrayList();
        Interaction inte = new Interaction();
        expResult.add(inte);
        
        module.setInteractionCollection(expResult);
        assertEquals(expResult,module.getInteractionCollection());
    }

    /**
     * Test of hashCode method, of class Module.
     */
    @Test
    public void testHashCode() {
        System.out.println("hashCode");
        Integer i = 2;
        int expResult = i.hashCode();
        int result = module.hashCode();
        assertEquals(expResult, result);
    }

    /**
     * Test of equals method, of class Module.
     */
    @Test
    public void testEquals() {
        System.out.println("equals");
        Object t1 = null;
        Module t2 = new Module(config);
        boolean expResult1 = false;
        boolean expResult2 = true;
        boolean result1 = module.equals(t1);
        boolean result2 = module.equals(t2);
        assertEquals(expResult1, result1);
        assertEquals(expResult2, result2);
    }

    /**
     * Test of toString method, of class Module.
     */
    @Test
    public void testToString() {
        System.out.println("toString");
        String expResult = "peps.peps_back.items.Module[ idmodule=2 ]";
        String result = module.toString();
        assertEquals(expResult, result);
    }
    
}
