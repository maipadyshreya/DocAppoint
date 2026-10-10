package com.example.docAppointment.controller;

import com.example.docAppointment.dto.AppointmentSlot;
import com.example.docAppointment.dto.PatientAppointment;
import com.example.docAppointment.service.AppointmentSlotService;
import jakarta.servlet.http.HttpSession;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

import java.time.LocalDate;
import java.util.List;

@RestController
@RequestMapping("/slot")
@RequiredArgsConstructor
public class AppointmentSlotController {
    private final AppointmentSlotService appointmentSlotService;
    @GetMapping("/doctor/{doctorId}")
    public List<AppointmentSlot> getAvailableByDoctor(
            @PathVariable int doctorId) {


        return appointmentSlotService.getAvailableByDoctor(doctorId);
    }
    @GetMapping("/user")
    public List<AppointmentSlot> getAppointmentByUser(HttpSession session) {
        Integer userId = (Integer) session.getAttribute("userId");

        if (userId == null) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED);
        }

        String role = (String) session.getAttribute("role");
        if (!"Patient".equals(role)) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN);
        }
        return appointmentSlotService.getAppointmentByUser(userId);
    }

    @GetMapping("/user/appointments")
    public List<PatientAppointment> getPatientAppointments(
            HttpSession session
    ) {
        Integer userId = (Integer) session.getAttribute("userId");

        if (userId == null) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED);
        }

        if (!"Patient".equals(session.getAttribute("role"))) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN);
        }

        return appointmentSlotService.getPatientAppointments(userId);
    }
    @GetMapping("/search")
    public List<AppointmentSlot> searchAvailableSlots(
            @RequestParam(required = false) Integer doctorId,
            @RequestParam(required = false) Integer serviceId,
            @RequestParam(required = false)
            @org.springframework.format.annotation.DateTimeFormat(
                    iso = org.springframework.format.annotation.DateTimeFormat.ISO.DATE
            )
            LocalDate date,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size
    ) {
        return appointmentSlotService.searchAvailableSlots(
                doctorId, serviceId, date, page, size
        );
    }
}
