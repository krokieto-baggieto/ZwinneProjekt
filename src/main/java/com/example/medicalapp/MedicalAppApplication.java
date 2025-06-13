package com.example.medicalapp;

import com.example.medicalapp.model.Doctor;
import com.example.medicalapp.model.User;
import com.example.medicalapp.model.DoctorAvailability;
import com.example.medicalapp.repository.DoctorRepository;
import com.example.medicalapp.repository.UserRepository;
import com.example.medicalapp.repository.DoctorAvailabilityRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.math.BigDecimal;
import java.time.DayOfWeek;
import java.time.LocalTime;

@SpringBootApplication
public class MedicalAppApplication implements CommandLineRunner {
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final DoctorRepository doctorRepository;
    private final DoctorAvailabilityRepository availabilityRepository;

    @Autowired
    public MedicalAppApplication(
            UserRepository userRepository, 
            PasswordEncoder passwordEncoder,
            DoctorRepository doctorRepository,
            DoctorAvailabilityRepository availabilityRepository) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
        this.doctorRepository = doctorRepository;
        this.availabilityRepository = availabilityRepository;
    }

    public static void main(String[] args) {
        SpringApplication.run(MedicalAppApplication.class, args);
    }

    @Override
    public void run(String... args) throws Exception {
        if (!userRepository.findByUsername("admin").isPresent()) {
            User admin = new User();
            admin.setUsername("admin");
            admin.setPassword(passwordEncoder.encode("admin"));
            admin.setRole(User.Role.ADMIN);
            admin.setEmail("admin@system.com");
            admin.setFirstName("Admin");
            admin.setLastName("System");
            admin.setPesel("00000000000");
            admin.setPhoneNumber("000000000");
            userRepository.save(admin);
            System.out.println("Administrator został utworzony");
        }

        // Dodawanie lekarzy pozostaje bez zmian...
        addDoctor("lekarz1", "lekarz111", "Maksym", "Folek", "Kardiolog", new BigDecimal("200.00"), "90090515836");
        addDoctor("lekarz2", "lekarz222", "Oliwier", "Golonko", "Neurolog", new BigDecimal("250.00"), "85071794815");
        addDoctor("lekarz3", "lekarz333", "Antoni", "Walczak", "Pediatra", new BigDecimal("180.00"), "82100216357");
        addDoctor("lekarz4", "lekarz444", "Michał", "Kowalski", "Ortopeda", new BigDecimal("220.00"), "76020489651");
        addDoctor("lekarz5", "lekarz555", "Jan", "Nowak", "Dermatolog", new BigDecimal("190.00"), "88082447213");
        addDoctor("lekarz6", "lekarz666", "Piotr", "Wiśniewski", "Okulista", new BigDecimal("210.00"), "79120715379");
    }

    private void addDoctor(String username, String password, String firstName, String lastName, 
                          String specialization, BigDecimal price, String pesel) {
        if (!userRepository.findByUsername(username).isPresent()) {
            // Tworzenie lekarza
            User doctorUser = new User();
            doctorUser.setUsername(username);
            doctorUser.setPassword(passwordEncoder.encode(password));
            doctorUser.setRole(User.Role.DOCTOR);
            doctorUser.setEmail(username + "@przychodnia.pl");
            doctorUser.setFirstName(firstName);
            doctorUser.setLastName(lastName);
            doctorUser.setPhoneNumber("500500500");
            doctorUser.setPesel(pesel);
            userRepository.save(doctorUser);

            // Tworzenie wpisu w tabeli doctors
            Doctor doctor = new Doctor();
            doctor.setUser(doctorUser);
            doctor.setSpecialization(specialization);
            doctor.setPrice(price);
            doctorRepository.save(doctor);

            // Dodawanie dostępności
            addAvailability(doctor, DayOfWeek.MONDAY, "08:00", "16:00");
            addAvailability(doctor, DayOfWeek.WEDNESDAY, "08:00", "16:00");
            addAvailability(doctor, DayOfWeek.FRIDAY, "08:00", "16:00");
        }
    }

    private void addAvailability(Doctor doctor, DayOfWeek dayOfWeek, String startTime, String endTime) {
        DoctorAvailability availability = new DoctorAvailability();
        availability.setDoctor(doctor);
        availability.setDayOfWeek(dayOfWeek);
        availability.setStartTime(LocalTime.parse(startTime));
        availability.setEndTime(LocalTime.parse(endTime));
        availabilityRepository.save(availability);
    }
}