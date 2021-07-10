package de.ipvs.as.mbp.web.rest;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;

/**
 *
 * @author rafaelkperes, Imeri Amil
 */
@Controller
public class IndexController {
   
    @RequestMapping("/")
    public String viewIndex() {
        return "index";
    }
    
    @RequestMapping("/login")
    public String login() {
        return "index";
    }
    
    @RequestMapping("/register")
    public String register() {
        return "index";
    }
    
    @RequestMapping("/view/**")
    public String views() {
        return "index";
    }
}
