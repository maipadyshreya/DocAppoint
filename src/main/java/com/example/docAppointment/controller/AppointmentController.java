package com.example.docAppointment.controller;

import com.example.docAppointment.dto.BookingRequest;
import com.example.docAppointment.service.AppointmentService;
import jakarta.servlet.http.HttpSession;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

@RestController
@RequestMapping("/appointment")
@RequiredArgsConstructor
public class AppointmentController {
    private final AppointmentService appointmentService;
    @PostMapping("/book")
    public String bookAppointment(
            @RequestBody BookingRequest request,
            HttpSession session) {

        Integer userId = (Integer) session.getAttribute("userId");

        if (userId == null) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED);
        }

        String role = (String) session.getAttribute("role");

        if (!"Patient".equals(role)) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN);
        }

        appointmentService.bookAppointment(
                userId,
                request.getAvailId(),
                request.getServiceId()
        );

        return "Appointment booked successfully";
    }

    @DeleteMapping("/{appointmentId}")
    public String cancelAppointment(
            @PathVariable int appointmentId,
            HttpSession session) {

        Integer userId = (Integer) session.getAttribute("userId");

        if (userId == null) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED);
        }

        String role = (String) session.getAttribute("role");

        if (!"Patient".equals(role)) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN);
        }

        appointmentService.cancelAppointment(appointmentId, userId);

        return "Appointment cancelled successfully";
    }
}
