/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/UnitTests/JUnit5TestClass.java to edit this template
 */
package peps.peps_back.controllers;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

public class ModuleDTOTest {

    @Test
    public void testGetSetId() {
        System.out.println("get/set Id");
        ModuleDTO instance = new ModuleDTO(1, "Test", "Location", "Active", "127.0.0.1", new ModuleConfigDTO());
        Integer expected = 2;
        instance.setId(expected);
        assertEquals(expected, instance.getId());
    }

    @Test
    public void testGetSetName() {
        System.out.println("get/set Name");
        ModuleDTO instance = new ModuleDTO(1, "Test", "Location", "Active", "127.0.0.1", new ModuleConfigDTO());
        String expected = "New Name";
        instance.setName(expected);
        assertEquals(expected, instance.getName());
    }

    @Test
    public void testGetSetLocation() {
        System.out.println("get/set Location");
        ModuleDTO instance = new ModuleDTO(1, "Test", "Location", "Active", "127.0.0.1", new ModuleConfigDTO());
        String expected = "New Location";
        instance.setLocation(expected);
        assertEquals(expected, instance.getLocation());
    }

    @Test
    public void testGetSetStatus() {
        System.out.println("get/set Status");
        ModuleDTO instance = new ModuleDTO(1, "Test", "Location", "Active", "127.0.0.1", new ModuleConfigDTO());
        String expected = "Inactive";
        instance.setStatus(expected);
        assertEquals(expected, instance.getStatus());
    }

    @Test
    public void testGetSetIp() {
        System.out.println("get/set Ip");
        ModuleDTO instance = new ModuleDTO(1, "Test", "Location", "Active", "127.0.0.1", new ModuleConfigDTO());
        String expected = "192.168.1.1";
        instance.setIp(expected);
        assertEquals(expected, instance.getIp());
    }

    @Test
    public void testGetSetConfig() {
        System.out.println("get/set Config");
        ModuleDTO instance = new ModuleDTO(1, "Test", "Location", "Active", "127.0.0.1", new ModuleConfigDTO());
        ModuleConfigDTO expected = new ModuleConfigDTO(100, "Manual", false, false);
        instance.setConfig(expected);
        assertEquals(expected, instance.getConfig());
    }
}
