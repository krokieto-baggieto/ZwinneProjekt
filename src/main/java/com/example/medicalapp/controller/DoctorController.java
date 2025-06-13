package com.example.medicalapp.controller;


import com.example.medicalapp.model.Appointment;
import com.example.medicalapp.model.Appointment.AppointmentStatus;
import com.example.medicalapp.model.Doctor;
import com.example.medicalapp.model.DoctorAvailability;
import com.example.medicalapp.service.UserService; 
import com.example.medicalapp.service.AppointmentService;
import com.example.medicalapp.service.DoctorService;
import com.example.medicalapp.repository.DoctorRepository;
import org.springframework.beans.factory.annotation.Autowired; 
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import java.time.DayOfWeek;
import java.time.LocalTime;
import java.util.ArrayList;
import lombok.extern.slf4j.Slf4j;
import java.math.BigDecimal;

@Slf4j
@Controller
@RequestMapping("/doctor")
public class DoctorController {
    private final AppointmentService appointmentService;
    private final DoctorService doctorService;
    private final UserService userService; 
    private final DoctorRepository doctorRepository; 

    @Autowired
    public DoctorController(AppointmentService appointmentService,
            DoctorService doctorService,
            UserService userService,
            DoctorRepository doctorRepository) { 
        this.appointmentService = appointmentService;
        this.doctorService = doctorService;
        this.userService = userService; 
        this.doctorRepository = doctorRepository; 
    }

    @GetMapping("/appointments")
    public String doctorAppointments(Model model, @AuthenticationPrincipal UserDetails userDetails) {
        String username = userDetails.getUsername();
        List<Appointment> acceptedAppointments = 
            appointmentService.getDoctorAppointmentsByStatus(username, AppointmentStatus.ACCEPTED);
        
        List<Map<String, Object>> calendarEvents = acceptedAppointments.stream()
            .map(apt -> {
                Map<String, Object> event = new HashMap<>();
                event.put("title", "Wizyta: " + apt.getPatient().getLastName());
                event.put("start", apt.getDate().toString());
                return event;
            })
            .collect(Collectors.toList());

        model.addAttribute("calendarEvents", calendarEvents);
        model.addAttribute("pendingAppointments", 
            appointmentService.getDoctorAppointmentsByStatus(username, AppointmentStatus.PENDING));
        model.addAttribute("acceptedAppointments", acceptedAppointments);
        model.addAttribute("rejectedAppointments", 
            appointmentService.getDoctorAppointmentsByStatus(username, AppointmentStatus.REJECTED));
        model.addAttribute("patients", userService.getAllPatients());

        return "doctor-appointments";
    }

    @PostMapping("/appointments/{id}/accept")
    public String acceptAppointment(@PathVariable Long id) {
        try {
            appointmentService.acceptAppointment(id);
            return "redirect:/doctor/appointments";
        } catch (Exception e) {
            log.error("Błąd podczas akceptowania wizyty: " + e.getMessage());
            return "redirect:/doctor/appointments";
        }
    }

    @PostMapping("/appointments/reject")
    public String rejectAppointment(
            @RequestParam Long appointmentId,
            @RequestParam String rejectionReason,
            RedirectAttributes redirectAttributes) {
        try {
            appointmentService.rejectAppointment(appointmentId, rejectionReason);
            redirectAttributes.addFlashAttribute("successMessage", "Wizyta została odrzucona");
            return "redirect:/doctor/appointments";
        } catch (Exception e) {
            System.out.println("Błąd podczas odrzucania wizyty: " + e.getMessage());
            e.printStackTrace();
            redirectAttributes.addFlashAttribute("errorMessage", "Wystąpił błąd podczas odrzucania wizyty");
            return "redirect:/doctor/appointments";
        }
    }

    @PostMapping("/appointments/{id}/status")
    public String updateAppointmentStatus(
            @PathVariable Long id,
            @RequestParam AppointmentStatus status) {
        appointmentService.updateAppointmentStatus(id, status);
        return "redirect:/doctor/appointments";
    }

    @GetMapping("/availability")
    public String showAvailabilityForm(Model model, @AuthenticationPrincipal UserDetails userDetails) {
        try {
            Doctor doctor = doctorService.getDoctorByUsername(userDetails.getUsername());
            List<DoctorAvailability> availabilityList = doctorService.getDoctorAvailability(doctor);
            
            Map<DayOfWeek, DoctorAvailability> availabilityMap = availabilityList.stream()
                .collect(Collectors.toMap(
                    DoctorAvailability::getDayOfWeek,
                    availability -> availability,
                    (existing, replacement) -> existing
                ));
            
            if (!availabilityList.isEmpty()) {
                DoctorAvailability firstAvailability = availabilityList.get(0);
                model.addAttribute("generalStartTime", firstAvailability.getStartTime());
                model.addAttribute("generalEndTime", firstAvailability.getEndTime());
            }
            
            model.addAttribute("availabilities", availabilityMap);
            model.addAttribute("doctor", doctor); 
            return "doctor/availability";
        } catch (Exception e) {
            model.addAttribute("errorMessage", "Wystąpił błąd podczas ładowania harmonogramu");
            return "error";
        }
    }

    @PostMapping("/availability")
    public String updateAvailability(
        @AuthenticationPrincipal UserDetails userDetails,
        @RequestParam Map<String, String> formData,
        RedirectAttributes redirectAttributes) {
        
        try {
            String generalStartTime = formData.get("generalStartTime");
            String generalEndTime = formData.get("generalEndTime");

            // Sprawdź czy godziny są w prawidłowym zakresie (6:00-22:00)
            LocalTime startTime = LocalTime.parse(generalStartTime);
            LocalTime endTime = LocalTime.parse(generalEndTime);
            
            if (startTime.isBefore(LocalTime.of(6, 0)) || 
                endTime.isAfter(LocalTime.of(22, 0)) || 
                endTime.isBefore(startTime)) {
                redirectAttributes.addFlashAttribute("errorMessage", 
                    "Godziny przyjęć muszą być między 6:00 a 22:00, a godzina końcowa musi być późniejsza niż początkowa");
                return "redirect:/doctor/availability";
            }

            Doctor doctor = doctorService.getDoctorByUsername(userDetails.getUsername());
            List<DoctorAvailability> availabilities = new ArrayList<>();

            for (DayOfWeek day : DayOfWeek.values()) {
                if (day != DayOfWeek.SATURDAY && day != DayOfWeek.SUNDAY) {
                    String enabled = formData.get("enabled_" + day);
                    
                    if ("on".equals(enabled)) {
                        DoctorAvailability availability = new DoctorAvailability();
                        availability.setDoctor(doctor);
                        availability.setDayOfWeek(day);
                        availability.setStartTime(startTime);
                        availability.setEndTime(endTime);
                        availabilities.add(availability);
                    }
                }
            }

            doctorService.setAvailability(doctor, availabilities);
            redirectAttributes.addFlashAttribute("successMessage", 
                "Godziny przyjęć zostały zaktualizowane");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("errorMessage", 
                "Wystąpił błąd podczas aktualizacji godzin przyjęć: " + e.getMessage());
        }
        
        return "redirect:/doctor/availability";
    }

    @GetMapping("/api/doctors/{id}/availability")
    @ResponseBody
    public List<Map<String, Object>> getDoctorAvailability(@PathVariable Long id) {
        Doctor doctor = doctorRepository.findById(id)
            .orElseThrow(() -> new IllegalArgumentException("Nieprawidłowy lekarz"));
        
        List<DoctorAvailability> availabilities = doctorService.getDoctorAvailability(doctor);
        
        return availabilities.stream()
            .map(a -> {
                Map<String, Object> availability = new HashMap<>();
                availability.put("dayOfWeek", a.getDayOfWeek().toString());
                availability.put("startTime", a.getStartTime().toString());
                availability.put("endTime", a.getEndTime().toString());
                return availability;
            })
            .collect(Collectors.toList());
    }

    @PostMapping("/update-price")
    public String updatePrice(@AuthenticationPrincipal UserDetails userDetails,
                            @RequestParam BigDecimal price,
                            RedirectAttributes redirectAttributes) {
        try {
            Doctor doctor = doctorService.getDoctorByUsername(userDetails.getUsername());
            doctor.setPrice(price);
            doctorRepository.save(doctor);
            redirectAttributes.addFlashAttribute("successMessage", "Cena została zaktualizowana");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("errorMessage", "Wystąpił błąd podczas aktualizacji ceny");
        }
        return "redirect:/doctor/availability";
    }
}