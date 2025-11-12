/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package peps.peps_back.controllers;

import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.servlet.ModelAndView;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
/**
 *
 * @author Clément
 */
public class DashBoardController {

    
    @GetMapping("/dashboard")
    @CrossOrigin(origins = "http://localhost:4200") // Allow Angular to access this API (4200 c'est notre port angular)
    public ResponseEntity<String> dashboard() {
        return ResponseEntity.ok("25");

    }

}
