package com.example.medicalapp.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

import com.example.medicalapp.service.AppointmentService;
import com.example.medicalapp.service.DoctorService;
import com.example.medicalapp.service.UserService;

@Controller
@RequestMapping("/admin/reports")
@PreAuthorize("hasRole('ADMIN')")
public class AdminReportController {
    private final AppointmentService appointmentService;
    private final DoctorService doctorService;
    private final UserService userService;

    @Autowired
    public AdminReportController(AppointmentService appointmentService, 
                               DoctorService doctorService,
                               UserService userService) {
        this.appointmentService = appointmentService;
        this.doctorService = doctorService;
        this.userService = userService;
    }

    @GetMapping
    public String showReports(Model model) {
        try {
            // Statystyki ogólne
            model.addAttribute("totalPatients", userService.countPatients());
            model.addAttribute("totalDoctors", doctorService.countDoctors());
            model.addAttribute("totalAppointments", appointmentService.countAllAppointments());
            
            // Statystyki wizyt
            model.addAttribute("appointmentsByStatus", appointmentService.getAppointmentStatsByStatus());
            model.addAttribute("appointmentsByDoctor", appointmentService.getAppointmentStatsByDoctor());
            model.addAttribute("appointmentsByMonth", appointmentService.getAppointmentStatsByMonth());

            return "admin/reports";
        } catch (Exception e) {
            e.printStackTrace();
            throw new RuntimeException("Błąd podczas generowania raportów: " + e.getMessage());
        }
    }
}
