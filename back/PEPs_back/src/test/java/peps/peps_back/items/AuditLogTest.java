/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/UnitTests/JUnit5TestClass.java to edit this template
 */
package peps.peps_back.items;

import java.util.Date;
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
public class AuditLogTest {
    
    public AuditLogTest() {
    }
    
    private AuditLog auditLog;
    
    @BeforeAll
    public static void setUpClass() {
    }
    
    @AfterAll
    public static void tearDownClass() {
    }
    
    Integer id1 =1;
    Integer id2 =2;
    @BeforeEach
    public void setUp() {
        auditLog = new AuditLog("Observer", "ara", id1, "ara1",
            "oiseau", "admin", "paco", "picou", "blablabla");
    }
    
    @AfterEach
    public void tearDown() {
    }

    /**
     * Test of getId method, of class AuditLog.
     */
    @Test
    public void testGetId() {
        //AuditLog(String action, String entityType, Integer entityId, String entityName,
         //   String entityRole, String userLogin, String oldValue, String newValue, String details)

        System.out.println("getters setters");

        assertEquals(auditLog.getAction(),"Observer");
        assertEquals(auditLog.getEntityType(),"ara");
        assertEquals(auditLog.getEntityId(),id1);
        assertEquals(auditLog.getEntityName(),"ara1");
        assertEquals(auditLog.getEntityRole(),"oiseau");
        assertEquals(auditLog.getUserLogin(),"admin");
        assertEquals(auditLog.getOldValue(),"paco");
        assertEquals(auditLog.getNewValue(),"picou");
        assertEquals(auditLog.getDetails(),"blablabla");
        
        auditLog.setAction("p");
        auditLog.setEntityType("p");
        auditLog.setEntityId(id2);
        auditLog.setEntityName("p");
        auditLog.setEntityRole("p");
        auditLog.setUserLogin("p");
        auditLog.setOldValue("p");
        auditLog.setNewValue("p");
        auditLog.setDetails("p");
        
        assertEquals(auditLog.getAction(),"p");
        assertEquals(auditLog.getEntityType(),"p");
        assertEquals(auditLog.getEntityId(),id2);
        assertEquals(auditLog.getEntityName(),"p");
        assertEquals(auditLog.getEntityRole(),"p");
        assertEquals(auditLog.getUserLogin(),"p");
        assertEquals(auditLog.getOldValue(),"p");
        assertEquals(auditLog.getNewValue(),"p");
        assertEquals(auditLog.getDetails(),"p");
    }
    
}
