package com.example.medicalapp.controller;

import com.example.medicalapp.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import com.example.medicalapp.model.User; 
import com.example.medicalapp.model.Doctor;
import org.springframework.web.bind.annotation.*;
import com.example.medicalapp.repository.DoctorRepository; 
import com.example.medicalapp.util.ValidationUtils;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
@RequestMapping("/profile")
public class ProfileController {
    private final UserService userService;
    private final PasswordEncoder passwordEncoder;
    private final DoctorRepository doctorRepository; 

    @Autowired
    public ProfileController(UserService userService, 
                           PasswordEncoder passwordEncoder,
                           DoctorRepository doctorRepository) {
        this.userService = userService;
        this.passwordEncoder = passwordEncoder;
        this.doctorRepository = doctorRepository; 
    }

    @GetMapping
    public String showProfile(Model model, @AuthenticationPrincipal UserDetails userDetails) {
        User user = userService.getUserByUsername(userDetails.getUsername());
        model.addAttribute("user", user);
        return "profile";
    }

    @PostMapping("/update")
    public String updateProfile(
            @ModelAttribute User updatedUser,
            @RequestParam(required = false) String currentPassword,
            @RequestParam(required = false) String newPassword,
            @RequestParam(required = false) String confirmPassword,
            @AuthenticationPrincipal UserDetails userDetails,
            RedirectAttributes redirectAttributes) {
        try {
            User user = userService.getUserByUsername(userDetails.getUsername());
            
            // Walidacja danych z pominięciem PESEL dla lekarza i admina
            String validationError = ValidationUtils.validateUserInput(
                updatedUser.getEmail(), 
                updatedUser.getPhoneNumber(), 
                null 
            );
            
            if (validationError != null) {
                redirectAttributes.addFlashAttribute("error", validationError);
                return "redirect:/profile";
            }

            // Aktualizacja hasła jeśli potrzebna
            if (currentPassword != null && !currentPassword.isEmpty()) {
                if (!passwordEncoder.matches(currentPassword, user.getPassword())) {
                    redirectAttributes.addFlashAttribute("error", "Obecne hasło jest nieprawidłowe");
                    return "redirect:/profile";
                }
                if (!ValidationUtils.isValidPassword(newPassword, confirmPassword)) {
                    redirectAttributes.addFlashAttribute("error", "Nowe hasła nie są identyczne");
                    return "redirect:/profile";
                }
                user.setPassword(passwordEncoder.encode(newPassword));
            }

            user.setEmail(updatedUser.getEmail());
            user.setPhoneNumber(updatedUser.getPhoneNumber());
            user.setFirstName(updatedUser.getFirstName());
            user.setLastName(updatedUser.getLastName());

            userService.updateUser(user);

            if (user.getRole() == User.Role.DOCTOR) {
                Doctor doctor = doctorRepository.findByUser(user);
                if (doctor != null) {
                           doctorRepository.save(doctor);
                }
            }

            redirectAttributes.addFlashAttribute("successMessage", "Profil został zaktualizowany");
            return "redirect:/profile";
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", "Wystąpił błąd podczas aktualizacji profilu");
            return "redirect:/profile";
        }
    }
}
