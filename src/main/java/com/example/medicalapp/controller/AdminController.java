package com.example.medicalapp.controller;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;
import org.springframework.ui.Model;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;
import com.example.medicalapp.model.User;
import com.example.medicalapp.model.Doctor;
import com.example.medicalapp.service.UserService;
import com.example.medicalapp.repository.DoctorRepository; 
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.beans.factory.annotation.Autowired;
import com.example.medicalapp.repository.UserRepository; 
import com.example.medicalapp.util.ValidationUtils;
import java.math.BigDecimal;
import java.util.List;
import java.util.stream.Collectors;
import com.example.medicalapp.service.DoctorService; 
import com.example.medicalapp.service.AppointmentService; 


@Controller
@RequestMapping("/admin")
@PreAuthorize("hasRole('ADMIN')")
public class AdminController {
    
    private final UserService userService;
    private final PasswordEncoder passwordEncoder;
    private final DoctorRepository doctorRepository; 
    private final UserRepository userRepository;
    private final DoctorService doctorService; 
    private final AppointmentService appointmentService; 
    
    @Autowired
    public AdminController(UserService userService, 
                         PasswordEncoder passwordEncoder,
                         DoctorRepository doctorRepository,
                         UserRepository userRepository,
                         DoctorService doctorService, 
                         AppointmentService appointmentService) { 
        this.userService = userService;
        this.passwordEncoder = passwordEncoder;
        this.doctorRepository = doctorRepository; 
        this.userRepository = userRepository;
        this.doctorService = doctorService; 
        this.appointmentService = appointmentService; 
    }
    
    @GetMapping
    public String adminPanel(Model model) {
        try {
            List<User> users = userRepository.findAll().stream()
                .filter(user -> user.getRole() != User.Role.ADMIN)
                .collect(Collectors.toList());
            model.addAttribute("users", users);
            return "admin/panel";
        } catch (Exception e) {
            e.printStackTrace(); 
            model.addAttribute("errorMessage", "Wystąpił błąd podczas ładowania panelu administratora");
            return "error";
        }
    }
    
    @GetMapping("/user/add")
    public String showAddUserForm(Model model) {
        model.addAttribute("user", new User());
        model.addAttribute("roles", User.Role.values());
        return "admin/add-user";
    }
    
    @PostMapping("/user/add")
    public String addUser(@ModelAttribute User user, 
                         @RequestParam(required = false) BigDecimal price, 
                         @RequestParam(required = false) String specialization,
                         RedirectAttributes redirectAttributes) {
        try {
            // Sprawdź czy użytkownik już istnieje
            if (userRepository.findByUsername(user.getUsername()).isPresent()) {
                redirectAttributes.addFlashAttribute("errorMessage", "Użytkownik o takim loginie już istnieje");
                return "redirect:/admin/user/add";
            }

            // Walidacja danych
            String validationError = ValidationUtils.validateUserInput(
                user.getEmail(), 
                user.getPhoneNumber(), 
                user.getPesel(),
                user.getRole()
            );
            
            if (validationError != null) {
                redirectAttributes.addFlashAttribute("errorMessage", validationError);
                return "redirect:/admin/user/add";
            }

            user.setPassword(passwordEncoder.encode(user.getPassword()));
            userRepository.save(user);

            if (user.getRole() == User.Role.DOCTOR) {
                Doctor doctor = new Doctor();
                doctor.setUser(user);
                doctor.setSpecialization(specialization); 
                doctor.setPrice(price != null ? price : new BigDecimal("200.00"));
                doctorRepository.save(doctor);
            }

            redirectAttributes.addFlashAttribute("successMessage", "Użytkownik został dodany");
            return "redirect:/admin";
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("errorMessage", "Błąd podczas dodawania użytkownika: " + e.getMessage());
            return "redirect:/admin/user/add";
        }
    }
    
    @PostMapping("/users/delete/{id}")
    public String deleteUser(@PathVariable Long id, RedirectAttributes redirectAttributes) {
        try {
            User user = userService.getUserById(id);
            if (user.getRole() != User.Role.ADMIN) {
                // Jeśli użytkownik jest lekarzem
                if (user.getRole() == User.Role.DOCTOR) {
                    Doctor doctor = doctorRepository.findByUser(user);
                    if (doctor != null) {
                        // usuń wszystkie dostępności
                        doctorService.removeAllAvailabilities(doctor);
                        // usuń wizyty
                        appointmentService.removeAllDoctorAppointments(doctor.getId());
                        // usuń doktora
                        doctorRepository.delete(doctor);
                    }
                }
                userService.deleteUser(id);
                redirectAttributes.addFlashAttribute("successMessage", "Użytkownik został usunięty");
            }
            return "redirect:/admin";
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("errorMessage", "Wystąpił błąd podczas usuwania użytkownika");
            return "redirect:/admin";
        }
    }
    
    @GetMapping("/users/edit/{id}")
    public String editUser(@PathVariable Long id, Model model) {
        User user = userService.getUserById(id);
        // Czyszczenie hasła przed przekazaniem do widoku
        user.setPassword(null);
        model.addAttribute("user", user);
        model.addAttribute("roles", User.Role.values());
        
        if (user.getRole() == User.Role.DOCTOR) {
            Doctor doctor = doctorRepository.findByUser(user);
            model.addAttribute("doctor", doctor);
        }
        
        // sprawdzanie czy hasło nie jest przekazywane do widoku
        user.setPassword(null);
        
        return "admin/edit-user";
    }
    
    @PostMapping("/users/edit/{id}")
    public String updateUser(@PathVariable Long id, 
                           @ModelAttribute User updatedUser,
                           @RequestParam(required = false) String newPassword,
                           @RequestParam(required = false) BigDecimal price,
                           @RequestParam(required = false) String specialization,
                           RedirectAttributes redirectAttributes) {
        try {
            User existingUser = userService.getUserById(id);
            
            // sprawdzanie czy nowy login już nie istnieje
            if (!existingUser.getUsername().equals(updatedUser.getUsername()) && 
                userRepository.findByUsername(updatedUser.getUsername()).isPresent()) {
                redirectAttributes.addFlashAttribute("errorMessage", "Użytkownik o takim loginie już istnieje");
                return "redirect:/admin/users/edit/" + id;
            }
            
            existingUser.setUsername(updatedUser.getUsername()); 
            existingUser.setEmail(updatedUser.getEmail());
            existingUser.setPhoneNumber(updatedUser.getPhoneNumber());
            existingUser.setPesel(updatedUser.getPesel());
            existingUser.setFirstName(updatedUser.getFirstName());
            existingUser.setLastName(updatedUser.getLastName());
            
            if (newPassword != null && !newPassword.trim().isEmpty()) {
                existingUser.setPassword(passwordEncoder.encode(newPassword));
            }

            userService.updateUser(existingUser);

            // Aktualizacja danych lekarza
            if (existingUser.getRole() == User.Role.DOCTOR) {
                Doctor doctor = doctorRepository.findByUser(existingUser);
                if (doctor == null) {
                    doctor = new Doctor();
                    doctor.setUser(existingUser);
                }

                doctor.setSpecialization(specialization);
                if (specialization != null && !specialization.trim().isEmpty()) {
                    doctor.setSpecialization(specialization);
                }
                if (price != null) {
                    doctor.setPrice(price);
                }
                doctorRepository.save(doctor);
            }

            redirectAttributes.addFlashAttribute("successMessage", "Dane użytkownika zostały zaktualizowane");
            return "redirect:/admin";
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("errorMessage", "Wystąpił błąd podczas aktualizacji użytkownika");
            return "redirect:/admin/users/edit/" + id;
        }
    }
}