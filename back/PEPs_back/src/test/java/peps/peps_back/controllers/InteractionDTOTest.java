/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/UnitTests/JUnit5TestClass.java to edit this template
 */
package peps.peps_back.controllers;

import java.util.Date;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

public class InteractionDTOTest {

    @Test
    public void testGetSetId() {
        System.out.println("get/set Id");
        InteractionDTO instance = new InteractionDTO(null, null, null, null);
        Integer expected = 1;
        instance.setId(expected);
        assertEquals(expected, instance.getId());
    }

    @Test
    public void testGetSetDate() {
        System.out.println("get/set Date");
        InteractionDTO instance = new InteractionDTO(null, null, null, null);
        Date expected = new Date();
        instance.setDate(expected);
        assertEquals(expected, instance.getDate());
    }

    @Test
    public void testGetSetModule() {
        System.out.println("get/set Module");
        InteractionDTO instance = new InteractionDTO(null, null, null, null);
        String expected = "Test Module";
        instance.setModule(expected);
        assertEquals(expected, instance.getModule());
    }

    @Test
    public void testGetSetType() {
        System.out.println("get/set Type");
        InteractionDTO instance = new InteractionDTO(null, null, null, null);
        String expected = "Test Type";
        instance.setType(expected);
        assertEquals(expected, instance.getType());
    }
}
