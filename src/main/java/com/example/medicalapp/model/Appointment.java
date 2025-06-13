package com.example.medicalapp.model;

import jakarta.persistence.*;
import jakarta.validation.constraints.Future;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;
import java.time.LocalDateTime;

@Data
@Entity
public class Appointment {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Future(message = "Data wizyty musi być w przyszłości")
    @Column(nullable = false)
    private LocalDateTime date;

    @NotNull(message = "Wybierz lekarza z listy")
    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "doctor_id", nullable = false)
    private Doctor doctor;

    @NotBlank(message = "Podaj powód wizyty")
    @Column(columnDefinition = "TEXT")
    private String reason;

    @NotNull(message = "Pacjent jest wymagany")
    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "patient_id", nullable = false)
    private User patient;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private AppointmentStatus status = AppointmentStatus.PENDING;

    @Column(columnDefinition = "TEXT")
    private String rejectionReason;

    @Column(nullable = false)
    private boolean isNfz; 

    @PrePersist
    @PreUpdate
    private void validateData() {
        if (status == null) {
            status = AppointmentStatus.PENDING;
        }
        if (doctor == null) {
            throw new IllegalStateException("Doctor cannot be null");
        }
        if (patient == null) {
            throw new IllegalStateException("Patient cannot be null");
        }
    }

    public enum AppointmentStatus {
        PENDING, 
        ACCEPTED, 
        REJECTED 
    }
}