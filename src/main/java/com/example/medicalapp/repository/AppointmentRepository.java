package com.example.medicalapp.repository;

import com.example.medicalapp.model.Appointment;
import com.example.medicalapp.model.Appointment.AppointmentStatus;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;
import java.util.Optional;
import java.time.LocalDateTime;

@Repository
public interface AppointmentRepository extends JpaRepository<Appointment, Long> {
    List<Appointment> findByPatientUsername(String username);
    List<Appointment> findByDoctorUserUsernameAndStatus(String username, AppointmentStatus status);
    List<Appointment> findByDoctorUserUsername(String username); 
    
    List<Appointment> findByDoctorIdAndStatus(Long doctorId, AppointmentStatus status);
    Optional<Appointment> findByDoctorIdAndDateAndStatus(Long doctorId, LocalDateTime date, AppointmentStatus status);
    
    List<Appointment> findByPatientUsernameAndStatus(String username, AppointmentStatus status);
    List<Appointment> findByDoctorId(Long doctorId);

}