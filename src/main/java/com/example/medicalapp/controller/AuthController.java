package com.example.medicalapp.controller;

import com.example.medicalapp.model.User;
import com.example.medicalapp.repository.DoctorRepository;
import com.example.medicalapp.repository.UserRepository;
import com.example.medicalapp.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import com.example.medicalapp.util.ValidationUtils;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
public class AuthController {

    private final UserService userService;
    private final DoctorRepository doctorRepository;
    private final UserRepository userRepository;

    @Autowired
    public AuthController(UserService userService, 
                        DoctorRepository doctorRepository,
                        UserRepository userRepository,
                        PasswordEncoder passwordEncoder) {
        this.userService = userService;
        this.doctorRepository = doctorRepository;
        this.userRepository = userRepository;
    }

    @GetMapping("/register")
    public String registerForm() {
        return "register";
    }

    @PostMapping("/register")
    public String registerUser(@ModelAttribute User user, 
                             @RequestParam String confirmPassword,
                             RedirectAttributes redirectAttributes,
                             Model model) {
        
        String validationError = ValidationUtils.validatePatientRegistration(
            user.getEmail(), 
            user.getPhoneNumber(), 
            user.getPesel()
        );
        
        if (validationError != null) {
            model.addAttribute("error", validationError);
            return "register";
        }

        if (!ValidationUtils.isValidPassword(user.getPassword(), confirmPassword)) {
            model.addAttribute("error", "Hasła nie są identyczne");
            return "register";
        }

        try {
            if (userRepository.findByUsername(user.getUsername()).isPresent()) {
                model.addAttribute("error", "Użytkownik o takim loginie już istnieje");
                return "register";
            }

            userService.registerUser(user, User.Role.PATIENT);
            redirectAttributes.addFlashAttribute("successMessage", "Rejestracja przebiegła pomyślnie");
            return "redirect:/login/patient";
        } catch (Exception e) {
            model.addAttribute("error", "Wystąpił błąd podczas rejestracji: " + e.getMessage());
            return "register";
        }
    }

    @GetMapping("/login")
    public String login(Model model) {
        return "redirect:/login/patient"; 
    }

    @GetMapping("/login/patient")
    public String loginPatient(Model model, 
                             @RequestParam(required = false) String error) {
        if (error != null) {
            model.addAttribute("error", "Nieprawidłowy login lub hasło");
        }
        return "login-patient";
    }

    @GetMapping("/login/doctor")
    public String loginDoctor(Model model, 
                            @RequestParam(required = false) String error) {
        model.addAttribute("doctors", doctorRepository.findAll());
        if (error != null) {
            model.addAttribute("error", "Nieprawidłowe hasło");
        }
        return "login-doctor";
    }
}