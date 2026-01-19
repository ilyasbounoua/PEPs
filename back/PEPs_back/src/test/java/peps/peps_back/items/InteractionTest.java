/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/UnitTests/JUnit5TestClass.java to edit this template
 */
package peps.peps_back.items;

import java.util.Date;
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
    
    @BeforeAll
    public static void setUpClass() {
    }
    
    @AfterAll
    public static void tearDownClass() {
    }
    
    @BeforeEach
    public void setUp() {
    }
    
    @AfterEach
    public void tearDown() {
    }

    @Autowired
    private InteractionRepository interactionRepository;
        
    /**
     * Test of getIdinteraction method, of class Interaction.
     */
    @Test
    public void testGetIdinteraction() {
        System.out.println("getIdinteraction");
        
        List<Interaction> interactions = interactionRepository.findAll();
        if(interactions.isEmpty())
        {
            System.out.println("No Interactions found");
            assert(false);
        }
        Integer result = interactions.iterator().next().getIdinteraction();
        assert(result>0);
    }

    
    
}
