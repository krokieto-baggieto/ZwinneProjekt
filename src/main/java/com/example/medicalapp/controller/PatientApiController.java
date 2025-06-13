package com.example.medicalapp.controller;

import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import com.example.medicalapp.model.User; 
import org.springframework.http.ResponseEntity;
import com.example.medicalapp.repository.UserRepository;

@RestController
@RequestMapping("/api")
public class PatientApiController {
    private final UserRepository userRepository;

    public PatientApiController(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    @GetMapping("/patients/{id}")
    public ResponseEntity<?> getPatientDetails(@PathVariable Long id) {
        return userRepository.findById(id)
            .map(user -> {
                if (user.getRole() == User.Role.PATIENT) {
                    return ResponseEntity.ok(user);
                }
                return ResponseEntity.notFound().build();
            })
            .orElse(ResponseEntity.notFound().build());
    }
}