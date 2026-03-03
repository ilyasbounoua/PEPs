/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/UnitTests/JUnit5TestClass.java to edit this template
 */
package peps.peps_back.items;

import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.List;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;
import org.springframework.beans.factory.annotation.Autowired;
import peps.peps_back.repositories.InteractionRepository;

/**
 *
 * @author Clément
 */
public class InteractionTest {
    
    public InteractionTest() {
    }
    
    Interaction interaction;
    @BeforeAll
    public static void setUpClass() {
    }
    
    @AfterAll
    public static void tearDownClass() {
    }
    
    @BeforeEach
    public void setUp() {
        interaction = new Interaction(1, "sound", new GregorianCalendar(2014, Calendar.FEBRUARY, 11).getTime());
    }
    
    @AfterEach
    public void tearDown() {
    }
        
    /**
     * Test of getIdinteraction and setIdinteraction method, of class Interaction.
     */
    @Test
    public void testIdinteraction() {
        System.out.println("getIdinteraction");
        
        Integer result = interaction.getIdinteraction();
        assertEquals(result,1);
        
        interaction.setIdinteraction(8);
        Integer result2 = interaction.getIdinteraction();
        assertEquals(result2,8);
    }


    /**
     * Test of getTypeinteraction and method, of class Interaction.
     */
    @Test
    public void testTypeinteraction() {
        System.out.println("getTypeinteraction");
        
        String result = interaction.getTypeinteraction();
        assertEquals("sound", result);
        
        interaction.setTypeinteraction("superSound");
        String result2 = interaction.getTypeinteraction();
        assertEquals("superSound", result2);
    }


    /**
     * Test of getTimeLancement and setTimeLancement method, of class Interaction.
     */
    @Test
    public void testGetTimeLancement() {
        System.out.println("getTimeLancement");
        
        Date result = interaction.getTimeLancement();
        assertEquals(new GregorianCalendar(2014, Calendar.FEBRUARY, 11).getTime(), result);
        
        interaction.setTimeLancement(new GregorianCalendar(2003, Calendar.FEBRUARY, 11).getTime());
        Date result2 = interaction.getTimeLancement();
        assertEquals(new GregorianCalendar(2003, Calendar.FEBRUARY, 11).getTime(), result2);
    }



    /**
     * Test of getIdmodule method, of class Interaction.
     */
    @Test
    public void testGetIdmodule() {
        System.out.println("getIdmodule");

        Module result = interaction.getIdmodule();
        assertEquals(null, result);
        
        interaction.setIdmodule(new Module());
        Module result2 = interaction.getIdmodule();
        assertEquals(new Module(), result2);
    }

    /**
     * Test of getIdsound and setIdsound method, of class Interaction.
     */
    @Test
    public void testGetIdsound() {
        System.out.println("getIdsound");

        Sound result = interaction.getIdsound();
        assertEquals(null, result);
        
        interaction.setIdsound(new Sound());
        Sound result2 = interaction.getIdsound();
        assertEquals(new Sound(), result2);
    }



    /**
     * Test of hashCode method, of class Interaction.
     */
    @Test
    public void testHashCode() {
        System.out.println("hashCode");
        
        int result = interaction.hashCode();
        assertEquals(interaction.getIdinteraction().hashCode(), result);
    }

    /**
     * Test of equals method, of class Interaction.
     */
    @Test
    public void testEquals() {
        System.out.println("equals");

        
        Interaction interaction2 = new Interaction(1, "sound", new GregorianCalendar(2014, Calendar.FEBRUARY, 11).getTime());
        Interaction interaction3 = new Interaction(5, "superSound", new GregorianCalendar(2014, Calendar.FEBRUARY, 11).getTime());
        Sound interaction4 = new Sound();
        assertEquals(interaction, interaction2);
        assertNotEquals(interaction, interaction3);
        assertNotEquals(interaction, interaction4);
    }

    /**
     * Test of toString method, of class Interaction.
     */
    @Test
    public void testToString() {
        System.out.println("toString");
        String expResult = "peps.peps_back.items.Interaction[ idinteraction=1 ]";
        String result = interaction.toString();
        assertEquals(expResult, result);
    }
    
}
