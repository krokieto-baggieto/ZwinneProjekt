package com.example.medicalapp.repository;

import com.example.medicalapp.model.Doctor;
import com.example.medicalapp.model.User;
import org.springframework.data.jpa.repository.JpaRepository;

public interface DoctorRepository extends JpaRepository<Doctor, Long> {
    Doctor findByUserUsername(String username);
    Doctor findByUser(User user); 
}