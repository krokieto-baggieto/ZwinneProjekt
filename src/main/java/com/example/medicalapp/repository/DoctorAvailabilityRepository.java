package com.example.medicalapp.repository;

import com.example.medicalapp.model.Doctor;
import com.example.medicalapp.model.DoctorAvailability;
import org.springframework.data.jpa.repository.JpaRepository;
import java.time.DayOfWeek;
import java.util.List;

public interface DoctorAvailabilityRepository extends JpaRepository<DoctorAvailability, Long> {
    List<DoctorAvailability> findByDoctor(Doctor doctor);
    List<DoctorAvailability> findByDoctorAndDayOfWeek(Doctor doctor, DayOfWeek dayOfWeek);
    void deleteByDoctor(Doctor doctor); 
}
