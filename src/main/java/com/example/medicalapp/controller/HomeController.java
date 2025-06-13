package com.example.medicalapp.controller;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;

@Controller
public class HomeController {

    
    @GetMapping("/")
    public String home(
        Model model,
        @RequestParam(name = "logout", required = false) String logout
    ) {
        if (logout != null) {
            model.addAttribute("logoutMessage", "Zostałeś pomyślnie wylogowany!");
        }
        return "home";
    }
}