package com.example.medicalapp.service;
import com.example.medicalapp.model.Appointment;
import com.example.medicalapp.model.Appointment.AppointmentStatus;
import com.example.medicalapp.model.User;
import com.example.medicalapp.repository.AppointmentRepository;
import com.example.medicalapp.repository.UserRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import lombok.extern.slf4j.Slf4j; 
import java.util.List;
import java.util.ArrayList;
import java.util.stream.Collectors; 
import java.time.LocalDateTime; 
import java.util.Map;
import java.time.format.DateTimeFormatter;
import com.example.medicalapp.model.Doctor; 
import org.springframework.beans.factory.annotation.Autowired; 

@Slf4j  
@Service
@Transactional
public class AppointmentService {

    private final AppointmentRepository appointmentRepository;
    private final UserRepository userRepository;
    private final DoctorService doctorService; 

    @Autowired
    public AppointmentService(
        AppointmentRepository appointmentRepository,
        UserRepository userRepository,
        DoctorService doctorService) { 
            this.appointmentRepository = appointmentRepository;
            this.userRepository = userRepository;
            this.doctorService = doctorService; 
        }

    @Transactional(readOnly = true)
    public List<Appointment> getUserAppointments(String username) {

        try {
            return appointmentRepository.findByPatientUsername(username);
        } catch (Exception e) {
            log.error("Error getting appointments for user {}: {}", username, e.getMessage());
            throw new RuntimeException("Nie udało się pobrać wizyt", e);
        }
    }

    public List<Appointment> getDoctorAppointments(String username) {

        List<Appointment> appointments = appointmentRepository.findByDoctorUserUsername(username);
    
        return appointments;
    }

    public void saveAppointment(Appointment appointment) {
        appointmentRepository.save(appointment);
    }

    public User getCurrentPatient(String username) {
        return userRepository.findByUsername(username)
                .orElseThrow(() -> new RuntimeException("Użytkownik nie znaleziony"));
    }

    public void updateAppointmentStatus(Long appointmentId, AppointmentStatus status) {
        Appointment appointment = appointmentRepository.findById(appointmentId)
                .orElseThrow(() -> new RuntimeException("Wizyta nie znaleziona"));
        appointment.setStatus(status);
        appointmentRepository.save(appointment);
    }

    public List<Appointment> getPendingAppointmentsForDoctor(String username) {
        return appointmentRepository.findByDoctorUserUsernameAndStatus(username, AppointmentStatus.PENDING);
    }

    @Transactional(readOnly = true)
    public List<Appointment> getDoctorAppointmentsByStatus(String username, AppointmentStatus status) {
        try {
            List<Appointment> appointments = appointmentRepository.findByDoctorUserUsernameAndStatus(username, status);
            log.info("Found {} appointments for doctor {} with status {}", 
                     appointments.size(), username, status);
            return appointments;
        } catch (Exception e) {
            log.error("Error getting appointments for doctor {} with status {}: {}", 
                      username, status, e.getMessage());
            return new ArrayList<>();
        }
    }

    @Transactional
    public void rejectAppointment(Long appointmentId, String rejectionReason) {

        try {
            Appointment appointment = appointmentRepository.findById(appointmentId)
                    .orElseThrow(() -> new RuntimeException("Wizyta nie znaleziona"));
            appointment.setStatus(AppointmentStatus.REJECTED);
            appointment.setRejectionReason(rejectionReason);
            appointmentRepository.save(appointment);
        } catch (Exception e) {
            log.error("Error rejecting appointment {}: {}", appointmentId, e.getMessage());
            throw new RuntimeException("Nie udało się odrzucić wizyty", e);
        }
    }

    public List<String> getBookedDatetimesForDoctor(Long doctorId) {
        return appointmentRepository.findByDoctorIdAndStatus(doctorId, AppointmentStatus.ACCEPTED)
            .stream()
            .map(appointment -> appointment.getDate().toString())
            .collect(Collectors.toList());
    }

    public boolean isDateTimeAvailable(Long doctorId, LocalDateTime dateTime) {
        return appointmentRepository
            .findByDoctorIdAndDateAndStatus(doctorId, dateTime, AppointmentStatus.ACCEPTED)
            .isEmpty();
    }

    public long countAllAppointments() {
        return appointmentRepository.count();
    }

    public Map<AppointmentStatus, Long> getAppointmentStatsByStatus() {
        return appointmentRepository.findAll().stream()
            .collect(Collectors.groupingBy(
                Appointment::getStatus,
                Collectors.counting()
            ));
    }

    public Map<String, Long> getAppointmentStatsByDoctor() {
        return appointmentRepository.findAll().stream()
            .collect(Collectors.groupingBy(
                appointment -> appointment.getDoctor().getUser().getFirstName() + 
                             " " + 
                             appointment.getDoctor().getUser().getLastName(),
                Collectors.counting()
            ));
    }

    public Map<String, Long> getAppointmentStatsByMonth() {
        return appointmentRepository.findAll().stream()
            .collect(Collectors.groupingBy(
                appointment -> appointment.getDate().format(DateTimeFormatter.ofPattern("yyyy-MM")),
                Collectors.counting()
            ));
    }

    @Transactional
    public boolean canScheduleAppointment(String username, LocalDateTime dateTime) {
        // Pobierz wszystkie zaakceptowane wizyty pacjenta
        List<Appointment> existingAppointments = appointmentRepository
            .findByPatientUsernameAndStatus(username, AppointmentStatus.ACCEPTED);

        // Założenie że każda wizyta trwa 30 minut
        LocalDateTime newStart = dateTime;
        LocalDateTime newEnd = dateTime.plusMinutes(30);

        // Sprawdź czy nowa wizyta nie nakłada się z istniejącymi
        for (Appointment existing : existingAppointments) {
            LocalDateTime existingStart = existing.getDate();
            LocalDateTime existingEnd = existingStart.plusMinutes(30);

            // Sprawdź czy terminy się nakładają
            if (!(newEnd.isBefore(existingStart) || newStart.isAfter(existingEnd))) {
                return false;
            }
        }
        return true; 
    }

    @Transactional
    public void checkAndUpdateConflictingAppointments(Doctor doctor) {
        // Pobierz wszystkie oczekujące wizyty dla danego lekarza
        List<Appointment> pendingAppointments = 
            appointmentRepository.findByDoctorIdAndStatus(doctor.getId(), AppointmentStatus.PENDING);
        
        // Sprawdź każdą oczekującą wizytę z harmonogramem lekarza
        for (Appointment appointment : pendingAppointments) {
            if (!doctorService.isTimeSlotAvailable(doctor, appointment.getDate())) {
                appointment.setStatus(AppointmentStatus.REJECTED);
                appointment.setRejectionReason(
                    "Wizyta została automatycznie odrzucona - terminarz lekarza został zmieniony");
                appointmentRepository.save(appointment);
            }
        }
    }

    // Sprawdzanie czy wizyta może być zaakceptowana
    public void checkAndUpdateConflictingAppointments(Doctor doctor, LocalDateTime newAvailabilityStart, LocalDateTime newAvailabilityEnd) {
        List<Appointment> pendingAppointments = 
            appointmentRepository.findByDoctorIdAndStatus(doctor.getId(), AppointmentStatus.PENDING);
        
        for (Appointment appointment : pendingAppointments) {
            LocalDateTime appointmentTime = appointment.getDate();
            if (appointmentTime.isBefore(newAvailabilityStart) || appointmentTime.isAfter(newAvailabilityEnd) ||
                !doctorService.isTimeSlotAvailable(doctor, appointmentTime)) {
                appointment.setStatus(AppointmentStatus.REJECTED);
                appointment.setRejectionReason("Wizyta została automatycznie odrzucona - terminarz lekarza został zmieniony");
                appointmentRepository.save(appointment);
            }
        }
    }

    @Transactional
    public void acceptAppointment(Long appointmentId) {
        Appointment appointment = appointmentRepository.findById(appointmentId)
            .orElseThrow(() -> new RuntimeException("Wizyta nie znaleziona"));

        // Najpierw sprawdź czy termin mieści się w godzinach przyjęć lekarza
        if (!doctorService.isTimeSlotAvailable(appointment.getDoctor(), appointment.getDate())) {
            appointment.setStatus(AppointmentStatus.REJECTED);
            appointment.setRejectionReason(
                "Wizyta została automatycznie odrzucona - termin poza godzinami przyjęć lekarza");
            appointmentRepository.save(appointment);
            return;
        }

        // Sprawdź czy lekarz nie ma już innej wizyty w tym terminie
        LocalDateTime appointmentTime = appointment.getDate();
        List<Appointment> existingAppointments = appointmentRepository
            .findByDoctorIdAndStatus(appointment.getDoctor().getId(), AppointmentStatus.ACCEPTED);

        // Sprawdź nakładanie się wizyt (założenie że wizyta trwa 30 minut)
        boolean hasConflict = existingAppointments.stream().anyMatch(existing -> {
            LocalDateTime existingStart = existing.getDate();
            LocalDateTime existingEnd = existingStart.plusMinutes(30);
            LocalDateTime newStart = appointmentTime;
            LocalDateTime newEnd = newStart.plusMinutes(30);
            
            return !(newEnd.isBefore(existingStart) || newStart.isAfter(existingEnd));
        });

        if (hasConflict) {
            appointment.setStatus(AppointmentStatus.REJECTED);
            appointment.setRejectionReason(
                "Wizyta została automatycznie odrzucona - lekarz ma już wizytę w tym terminie");
            appointmentRepository.save(appointment);
            return;
        }

        // Sprawdź czy pacjent nie ma już wizyty w tym terminie
        List<Appointment> patientAppointments = appointmentRepository
            .findByPatientUsernameAndStatus(appointment.getPatient().getUsername(), AppointmentStatus.ACCEPTED);

        boolean hasPatientConflict = patientAppointments.stream().anyMatch(existing -> {
            LocalDateTime existingStart = existing.getDate();
            LocalDateTime existingEnd = existingStart.plusMinutes(30);
            LocalDateTime newStart = appointmentTime;
            LocalDateTime newEnd = newStart.plusMinutes(30);
            
            return !(newEnd.isBefore(existingStart) || newStart.isAfter(existingEnd));
        });

        if (hasPatientConflict) {
            appointment.setStatus(AppointmentStatus.REJECTED);
            appointment.setRejectionReason(
                "Wizyta została automatycznie odrzucona - pacjent ma już wizytę w tym terminie");
            appointmentRepository.save(appointment);
            return;
        }

        appointment.setStatus(AppointmentStatus.ACCEPTED);
        appointmentRepository.save(appointment);
    }

    @Transactional
    public void addAppointment(Appointment appointment, boolean isNfz) {
        if (isNfz) {
            validateNfzAppointment(appointment);
            LocalDateTime sixMonthsFromNow = LocalDateTime.now().plusMonths(6);
            if (appointment.getDate().isBefore(sixMonthsFromNow)) {
                appointment.setDate(sixMonthsFromNow);
            }
        }
        appointment.setNfz(isNfz);
        appointmentRepository.save(appointment);
    }

    private void validateNfzAppointment(Appointment appointment) {
        LocalDateTime now = LocalDateTime.now();
        LocalDateTime sixMonthsFromNow = now.plusMonths(6);
        
        if (appointment.getDate().isAfter(sixMonthsFromNow)) {
            throw new IllegalArgumentException(
                "Wizyty NFZ można planować maksymalnie 6 miesięcy do przodu"
            );
        }
    }

    // Sprawdzanie czy można zaplanować wizytę
    @Transactional
    public boolean canScheduleAppointment(String username, LocalDateTime dateTime, boolean isNfz) {
        // Sprawdzenie czy data nie jest z przeszłości
        if (dateTime.isBefore(LocalDateTime.now())) {
            return false;
        }

        // sprawdzanie czy termin na nfz jest minimum 6 miesięcy do przodu
        if (isNfz) {
            LocalDateTime sixMonthsFromNow = LocalDateTime.now().plusMonths(6);
            if (dateTime.isBefore(sixMonthsFromNow)) {
                return false; 
            }
        }

        // Sprawdzenie czy pacjent nie ma już wizyty w tym terminie
        List<Appointment> existingAppointments = appointmentRepository
            .findByPatientUsernameAndStatus(username, AppointmentStatus.ACCEPTED);

        LocalDateTime newStart = dateTime;
        LocalDateTime newEnd = dateTime.plusMinutes(30);

        return existingAppointments.stream().noneMatch(existing -> {
            LocalDateTime existingStart = existing.getDate();
            LocalDateTime existingEnd = existingStart.plusMinutes(30);
            return !(newEnd.isBefore(existingStart) || newStart.isAfter(existingEnd));
        });
    }

    @Transactional
    public void removeAllDoctorAppointments(Long doctorId) {
        List<Appointment> appointments = appointmentRepository.findByDoctorId(doctorId);
        appointmentRepository.deleteAll(appointments);
    }

}