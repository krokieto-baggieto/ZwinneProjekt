package com.example.medicalapp.service;

import com.example.medicalapp.model.Doctor;
import com.example.medicalapp.model.DoctorAvailability;
import com.example.medicalapp.model.Appointment; 
import com.example.medicalapp.model.Appointment.AppointmentStatus; 
import com.example.medicalapp.repository.DoctorRepository;
import com.example.medicalapp.repository.DoctorAvailabilityRepository;
import com.example.medicalapp.repository.AppointmentRepository; 
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.DayOfWeek;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

@Service
public class DoctorService {
    private final DoctorRepository doctorRepository;
    private final DoctorAvailabilityRepository availabilityRepository;
    private final AppointmentRepository appointmentRepository;

    @Autowired
    public DoctorService(
            DoctorRepository doctorRepository,
            DoctorAvailabilityRepository availabilityRepository,
            AppointmentRepository appointmentRepository) {
        this.doctorRepository = doctorRepository;
        this.availabilityRepository = availabilityRepository;
        this.appointmentRepository = appointmentRepository;
    }

    public Doctor getDoctorByUsername(String username) {
        return doctorRepository.findByUserUsername(username);
    }

    public void updateDoctor(Doctor doctor) {
        // Aktualizuj dane w tabeli doctors
        Doctor existingDoctor = doctorRepository.findById(doctor.getId()).orElseThrow();
        existingDoctor.setSpecialization(doctor.getSpecialization());
        doctorRepository.save(existingDoctor);
    }

    @Transactional
    public void setAvailability(Doctor doctor, List<DoctorAvailability> availabilities) {
        // Pobierz istniejące dostępności
        List<DoctorAvailability> currentAvailabilities = availabilityRepository.findByDoctor(doctor);

        // Usuń tylko te dni, które nie są już zaznaczone
        Set<DayOfWeek> newDays = availabilities.stream()
            .map(DoctorAvailability::getDayOfWeek)
            .collect(Collectors.toSet());

        currentAvailabilities.stream()
            .filter(av -> !newDays.contains(av.getDayOfWeek()))
            .forEach(availabilityRepository::delete);

        // Aktualizuj lub dodaj nowe dostępności
        for (DoctorAvailability newAvailability : availabilities) {
            Optional<DoctorAvailability> existingAvailability = currentAvailabilities.stream()
                .filter(av -> av.getDayOfWeek() == newAvailability.getDayOfWeek())
                .findFirst();

            if (existingAvailability.isPresent()) {
                DoctorAvailability availability = existingAvailability.get();
                availability.setStartTime(newAvailability.getStartTime());
                availability.setEndTime(newAvailability.getEndTime());
                availabilityRepository.save(availability);
            } else {
                newAvailability.setDoctor(doctor);
                availabilityRepository.save(newAvailability);
            }
        }

        // Sprawdź i odrzuć wizyty, które nie pasują do nowego harmonogramu
        List<Appointment> acceptedAppointments = appointmentRepository
            .findByDoctorIdAndStatus(doctor.getId(), AppointmentStatus.ACCEPTED);

        for (Appointment appointment : acceptedAppointments) {
            if (!isTimeSlotAvailable(doctor, appointment.getDate())) {
                appointment.setStatus(AppointmentStatus.REJECTED);
                appointment.setRejectionReason(
                    "Wizyta została automatycznie odrzucona - terminarz lekarza został zmieniony");
                appointmentRepository.save(appointment);
            }
        }
    }

    @Transactional
    public void removeAllAvailabilities(Doctor doctor) {
        List<DoctorAvailability> availabilities = availabilityRepository.findByDoctor(doctor);
        availabilityRepository.deleteAll(availabilities);
    }

    public List<DoctorAvailability> getDoctorAvailability(Doctor doctor) {
        List<DoctorAvailability> availabilities = availabilityRepository.findByDoctor(doctor);
        // Sortowanie dni tygodnia
        availabilities.sort((a1, a2) -> 
            a1.getDayOfWeek().getValue() - a2.getDayOfWeek().getValue());
        return availabilities;
    }

    public boolean isTimeSlotAvailable(Doctor doctor, LocalDateTime dateTime) {
        DayOfWeek dayOfWeek = dateTime.getDayOfWeek();
        LocalTime time = dateTime.toLocalTime();

        // Sprawdź czy lekarz w ogóle przyjmuje w tym dniu
        List<DoctorAvailability> availabilities = 
            availabilityRepository.findByDoctorAndDayOfWeek(doctor, dayOfWeek);

        if (availabilities.isEmpty()) {
            return false;
        }

        // Sprawdź czy czas mieści się w godzinach przyjęć
        DoctorAvailability availability = availabilities.get(0);
        return !time.isBefore(availability.getStartTime()) && 
               !time.isAfter(availability.getEndTime());
    }

    public long countDoctors() {
        return doctorRepository.count();
    }
}