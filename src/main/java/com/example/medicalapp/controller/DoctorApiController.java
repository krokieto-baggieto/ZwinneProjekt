package com.example.medicalapp.controller;

import com.example.medicalapp.service.AppointmentService;
import org.springframework.web.bind.annotation.*;
import java.util.List;

@RestController
@RequestMapping("/api/doctors")
public class DoctorApiController {
    private final AppointmentService appointmentService;

    public DoctorApiController(AppointmentService appointmentService) {
        this.appointmentService = appointmentService;
    }

    @GetMapping("/{doctorId}/schedule")
    public List<String> getDoctorSchedule(@PathVariable Long doctorId) {
        return appointmentService.getBookedDatetimesForDoctor(doctorId);
    }
}
