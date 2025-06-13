package com.example.medicalapp.repository;

import com.example.medicalapp.model.User;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.Optional;
import java.util.List; 


public interface UserRepository extends JpaRepository<User, Long> {
    Optional<User> findByUsername(String username);
    List<User> findByRole(User.Role role); 
    long countByRole(User.Role role); 
}