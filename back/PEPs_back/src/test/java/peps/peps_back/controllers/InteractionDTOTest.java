/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/UnitTests/JUnit5TestClass.java to edit this template
 */
package peps.peps_back.controllers;

import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

public class InteractionDTOTest {

    @Test
    public void testGettersSettersId() {
        System.out.println("getId");
        Integer id = 1;
        InteractionDTO interactionDTO = new InteractionDTO(id,new GregorianCalendar(2014, Calendar.FEBRUARY, 11).getTime()
        ,"modue","type");
        
        interactionDTO.setId(2);
        interactionDTO.setDate(new GregorianCalendar(2014, Calendar.MARCH, 11).getTime());
        interactionDTO.setModule("module");
        interactionDTO.setType("thetype");
        
        assertEquals(interactionDTO.getId(),2);
        assertEquals(interactionDTO.getDate(),new GregorianCalendar(2014, Calendar.MARCH, 11).getTime());
        assertEquals(interactionDTO.getModule(),"module");
        assertEquals(interactionDTO.getType(),"thetype");
        
        
    }

}
