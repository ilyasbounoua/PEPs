/**
 * @author BOUNOUA Ilyas and VAZEILLE Clément
 * @description This file defines the IndexController class, which handles requests for the index page.
 */
package peps.peps_back.controllers;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind. annotation.RequestMapping;
import org.springframework.web.servlet.ModelAndView;

@Controller
public class IndexController {

@RequestMapping(value="index.do")

public ModelAndView handleIndexGet() {
return new ModelAndView("index");

}

}
