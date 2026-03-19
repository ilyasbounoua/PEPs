/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/UnitTests/JUnit5TestClass.java to edit this template
 */
package peps.peps_back.items;

import java.time.LocalDateTime;
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
public class UserTest {
    
    public UserTest() {
    }
    
    private User user;
    private LocalDateTime date1 = LocalDateTime.now();
    private Integer id1 = 1;
    private Integer id2 = 2;
    @BeforeAll
    public static void setUpClass() {
    }
    
    @AfterAll
    public static void tearDownClass() {
    }
    
    @BeforeEach
    public void setUp() {
        user = new User(id1,"user1","mdp",true,"ara", "viewer", date1, date1);
    }
    
    @AfterEach
    public void tearDown() {
    }

    /**
     * Test of getIdUser method, of class User.
     */
    @Test
    public void testGettersSetters() {
        System.out.println("getters setters");
        
        assertEquals(user.getIdUser(),id1);
        assertEquals(user.getLogin(),"user1");
        assertEquals(user.getPasswordHash(),"mdp");
        assertEquals(user.getEnabled(),true);
        assertEquals(user.getRole(),"ara");
        assertEquals(user.getPermission(),"viewer");
        
        user.setIdUser(id2);
        user.setLogin("p");
        user.setPasswordHash("p");
        user.setEnabled(false);
        user.setRole("p");
        user.setLogin("p");
        
        assertEquals(user.getIdUser(),id2);
        assertEquals(user.getLogin(),"p");
        assertEquals(user.getPasswordHash(),"p");
        assertEquals(user.getEnabled(),false);
        assertEquals(user.getRole(),"p");
        assertEquals(user.getLogin(),"p");
    }

        /**
     * Test of getIdUser method, of class User.
     */
    @Test
    public void testConstructors() {
        
        user = new User(id1,"user1","mdp",true,"ara", "viewer", date1, date1);
        
        user = new User();
    }
    
}
