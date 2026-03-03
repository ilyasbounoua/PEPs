/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/UnitTests/JUnit5TestClass.java to edit this template
 */
package peps.peps_back.items;

import java.util.ArrayList;
import java.util.Collection;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;
import static peps.peps_back.items.ModuleTest.config;

/**
 *
 * @author Clément
 */
public class SoundTest {
    
    Sound sound;
    public SoundTest() {
    }
    
    @BeforeAll
    public static void setUpClass() {
    }
    
    @AfterAll
    public static void tearDownClass() {
    }
    
    @BeforeEach
    public void setUp() {
        sound = new Sound(7, "gong", "Percussion", ".mp3");
        sound.setChemin("dossier/");
    }
    
    @AfterEach
    public void tearDown() {
    }

    /**
     * Test of getIdsound method, of class Sound.
     */
    @Test
    public void testGettersSetters() {
        System.out.println("getters setters");
        
        assertEquals(sound.getChemin(),"dossier/");
        assertEquals(sound.getExtension(),".mp3");
        assertEquals(sound.getIdsound(),7);
        assertEquals(sound.getNom(),"gong");
        assertEquals(sound.getTypeSon(),"Percussion");
        
        sound.setChemin("p");
        sound.setExtension("p");
        sound.setIdsound(8);
        sound.setNom("p");
        sound.setTypeSon("p");
        
        assertEquals(sound.getChemin(),"p");
        assertEquals(sound.getExtension(),"p");
        assertEquals(sound.getIdsound(),8);
        assertEquals(sound.getNom(),"p");
        assertEquals(sound.getTypeSon(),"p");
    }
    
    /**
     * Test of getInteractionCollection and setInteractionCollection methods, of class Sound.
     */
    @Test
    public void testInteractionCollection() {
        System.out.println("InteractionCollection");
        
        Collection<Interaction> result = sound.getInteractionCollection();
        assertEquals(null, result);
        
        Collection<Interaction> expResult = new ArrayList();
        Interaction inte = new Interaction();
        expResult.add(inte);
        
        sound.setInteractionCollection(expResult);
        assertEquals(expResult,sound.getInteractionCollection());
    }

    /**
     * Test of hashCode method, of class Sound.
     */
    @Test
    public void testHashCode() {
        System.out.println("hashCode");
        Integer id = 7;
        int expResult = id.hashCode();
        int result = sound.hashCode();
        assertEquals(expResult, result);
    }

    /**
     * Test of equals method, of class Sound.
     */
    @Test
    public void testEquals() {
        System.out.println("equals");
        Object t1 = null;
        Sound t2 = new Sound(7, "gong", "Percussion", ".mp3");
        t2.setChemin("dossier/");
        boolean expResult1 = false;
        boolean expResult2 = true;
        boolean result1 = sound.equals(t1);
        boolean result2 = sound.equals(t2);
        assertEquals(expResult1, result1);
        assertEquals(expResult2, result2);
    }

    /**
     * Test of toString method, of class Sound.
     */
    @Test
    public void testToString() {
        System.out.println("toString");
        String expResult = "peps.peps_back.items.Sound[ idsound=7 ]";
        String result = sound.toString();
        assertEquals(expResult, result);
    }
    
}
