package com.example.medicalapp.controller;

import com.example.medicalapp.model.Appointment;
import com.example.medicalapp.model.Appointment.AppointmentStatus;
import com.example.medicalapp.model.Doctor;
import com.example.medicalapp.model.User;
import com.example.medicalapp.repository.DoctorRepository;
import com.example.medicalapp.service.AppointmentService;
import com.example.medicalapp.model.DoctorAvailability;
import com.example.medicalapp.service.DoctorService;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;
import java.util.ArrayList;
import java.util.List;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import java.util.HashMap;
import java.util.Map;
import java.time.LocalDateTime;



@Controller

@RequestMapping("/appointments")

public class AppointmentController {
    private final AppointmentService appointmentService;
    private final DoctorRepository doctorRepository;
    private final DoctorService doctorService;
    private static final Logger log = LoggerFactory.getLogger(AppointmentController.class);


    @Autowired
    public AppointmentController(AppointmentService appointmentService, 
                                DoctorRepository doctorRepository,
                                DoctorService doctorService) {

        this.appointmentService = appointmentService;
        this.doctorRepository = doctorRepository;
        this.doctorService = doctorService;

    }



    @GetMapping
    public String listAppointments(Model model, @AuthenticationPrincipal UserDetails userDetails) {
        try {
            List<Appointment> appointments = appointmentService.getUserAppointments(userDetails.getUsername());
            List<Doctor> doctors = doctorRepository.findAll();
            model.addAttribute("appointments", appointments);
            model.addAttribute("doctors", doctors);
            model.addAttribute("username", userDetails.getUsername());
            return "appointments";
        } catch (Exception e) {
            log.error("Błąd w listAppointments: ", e);
            model.addAttribute("errorMessage", "Wystąpił błąd podczas ładowania wizyt");
            model.addAttribute("appointments", new ArrayList<>());
            model.addAttribute("username", userDetails.getUsername());
            return "appointments";
        }
    }



    @GetMapping("/add")

    public String showAddAppointmentForm(Model model) {

        List<Doctor> doctors = doctorRepository.findAll();
        model.addAttribute("doctors", doctors);

    
        // Pobierz dostępność dla każdego lekarza
        Map<Long, List<DoctorAvailability>> availabilities = new HashMap<>();

        for (Doctor doctor : doctors) {
            List<DoctorAvailability> doctorAvailabilities = doctorService.getDoctorAvailability(doctor);
            availabilities.put(doctor.getId(), doctorAvailabilities);
        }

        model.addAttribute("availabilities", availabilities);

        return "add-appointment";

    }



    @PostMapping
    public String addAppointment(
        @ModelAttribute Appointment appointment,
        @AuthenticationPrincipal UserDetails userDetails,
        @RequestParam Long doctorId,
        @RequestParam(required = false, defaultValue = "false") boolean isNfz,
        RedirectAttributes redirectAttributes

    ) {

        try {

            // Sprawdzenie czy data nie jest z przeszłości
            if (appointment.getDate().isBefore(LocalDateTime.now())) {
                redirectAttributes.addFlashAttribute("errorMessage", 
                    "Nie można umówić wizyty na termin z przeszłości");
                return "redirect:/appointments/add";
            }

            // Sprawdzenie warunku NFZ
            if (isNfz && appointment.getDate().isBefore(LocalDateTime.now().plusMonths(6))) {
                redirectAttributes.addFlashAttribute("errorMessage", 
                    "Wizyty w ramach NFZ można umawiać wyłącznie na terminy odległe o co najmniej 6 miesięcy.");
                return "redirect:/appointments/add";
            }

            
            if (isNfz && appointment.getDate().isBefore(LocalDateTime.now().plusMonths(6))) {
                redirectAttributes.addFlashAttribute("warningMessage", 
                    "Twoja wizyta NFZ została zaplanowana na najwcześniejszy możliwy termin (za 6 miesięcy)");
            }

            Doctor doctor = doctorRepository.findById(doctorId)

                .orElseThrow(() -> new IllegalArgumentException("Nieprawidłowy lekarz"));



            if (!appointmentService.canScheduleAppointment(userDetails.getUsername(), appointment.getDate())) {
                redirectAttributes.addFlashAttribute("errorMessage", 
                    "Masz już zaplanowaną wizytę w tym terminie");
                return "redirect:/appointments/add";
            }

            // Sprawdzanie dostępności lekarza
            if (!doctorService.isTimeSlotAvailable(doctor, appointment.getDate())) {

                redirectAttributes.addFlashAttribute("errorMessage", 

                    "Lekarz nie przyjmuje w wybranym terminie");

                return "redirect:/appointments/add";

            }



            // Sprawdź czy termin nie jest już zajęty
            if (!appointmentService.isDateTimeAvailable(doctorId, appointment.getDate())) {

                redirectAttributes.addFlashAttribute("errorMessage", 

                    "Ten termin jest już zajęty");

                return "redirect:/appointments/add";

            }



           
            User patient = appointmentService.getCurrentPatient(userDetails.getUsername());
            appointment.setPatient(patient);
            appointment.setDoctor(doctor);
            appointment.setNfz(isNfz);
            appointmentService.saveAppointment(appointment);

            
            redirectAttributes.addFlashAttribute("successMessage", "Wizyta została pomyślnie dodana");
            return "redirect:/appointments";
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("errorMessage", "Wystąpił błąd podczas dodawania wizyty: " + e.getMessage());
            return "redirect:/appointments/add";
        }
    }



    @GetMapping("/doctor")
    public String listDoctorAppointments(Model model, @AuthenticationPrincipal UserDetails userDetails) {
        List<Appointment> pendingAppointments = appointmentService.getPendingAppointmentsForDoctor(userDetails.getUsername());
        model.addAttribute("appointments", pendingAppointments);
        return "doctor-appointments";
    }



    @PostMapping("/{id}/status")
    public String updateAppointmentStatus(@PathVariable Long id, 
                                        @RequestParam AppointmentStatus status,
                                        @AuthenticationPrincipal UserDetails userDetails) {
        appointmentService.updateAppointmentStatus(id, status);
        return "redirect:/appointments/doctor";
    }
}