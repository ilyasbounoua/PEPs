/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/UnitTests/JUnit5TestClass.java to edit this template
 */
package peps.peps_back.controllers;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

public class ModuleConfigDTOTest {

    @Test
    public void testGetSetVolume() {
        System.out.println("get/set Volume");
        ModuleConfigDTO instance = new ModuleConfigDTO(50, "Auto", true, true);
        int expected = 60;
        instance.setVolume(expected);
        assertEquals(expected, instance.getVolume());
    }

    @Test
    public void testGetSetMode() {
        System.out.println("get/set Mode");
        ModuleConfigDTO instance = new ModuleConfigDTO(50, "Auto", true, true);
        String expected = "Manual";
        instance.setMode(expected);
        assertEquals(expected, instance.getMode());
    }

    @Test
    public void testIsSetActif() {
        System.out.println("is/set Actif");
        ModuleConfigDTO instance = new ModuleConfigDTO(50, "Auto", true, true);
        boolean expected = false;
        instance.setActif(expected);
        assertEquals(expected, instance.isActif());
    }

    @Test
    public void testIsSetSon() {
        System.out.println("is/set Son");
        ModuleConfigDTO instance = new ModuleConfigDTO(50, "Auto", true, true);
        boolean expected = false;
        instance.setSon(expected);
        assertEquals(expected, instance.isSon());
    }
}
