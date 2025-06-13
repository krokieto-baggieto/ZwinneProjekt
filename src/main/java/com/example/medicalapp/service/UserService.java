package com.example.medicalapp.service;

import com.example.medicalapp.model.User;
import com.example.medicalapp.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class UserService {
    @Autowired
    private UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    public UserService(UserRepository userRepository, PasswordEncoder passwordEncoder) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
    }

    public void registerUser(User user, User.Role role) {
        // Sprawdź czy użytkownik o takim loginie już istnieje
        if (userRepository.findByUsername(user.getUsername()).isPresent()) {
            throw new RuntimeException("Użytkownik o takim loginie już istnieje");
        }
        
        user.setRole(role);
        user.setPassword(passwordEncoder.encode(user.getPassword()));
        userRepository.save(user);
    }

    public User getUserByUsername(String username) {
        return userRepository.findByUsername(username)
            .orElseThrow(() -> new RuntimeException("Użytkownik nie znaleziony"));
    }

    public void updateUser(User user) {
        // Sprawdź czy użytkownik o takim loginie już istnieje (z wyłączeniem edytowanego użytkownika)
        userRepository.findByUsername(user.getUsername())
            .ifPresent(existingUser -> {
                if (!existingUser.getId().equals(user.getId())) {
                    throw new RuntimeException("Użytkownik o takim loginie już istnieje");
                }
            });
            
        userRepository.save(user);
    }

    public List<User> getAllPatients() {
        return userRepository.findByRole(User.Role.PATIENT);
    }

    public List<User> getAllUsers() {
        return userRepository.findAll();
    }

    public User getUserById(Long id) {
        return userRepository.findById(id)
            .orElseThrow(() -> new RuntimeException("User not found"));
    }

    public void deleteUser(Long id) {
        userRepository.deleteById(id);
    }

    public long countPatients() {
        return userRepository.countByRole(User.Role.PATIENT);
    }
}