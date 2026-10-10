package com.example.docAppointment.controller;

import com.example.docAppointment.dto.AppointmentSlot;
import com.example.docAppointment.dto.CreateSlotDoc;
import com.example.docAppointment.dto.DoctorAppointment;
import com.example.docAppointment.service.DoctorService;
import jakarta.servlet.http.HttpSession;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;

@RestController
@RequestMapping("/doctor")
@RequiredArgsConstructor
public class DoctorController {

    private final DoctorService doctorService;

    @PostMapping("/slots")
    public String createSlot(
            @RequestBody CreateSlotDoc request,
            HttpSession session
    ) {
        Integer userId =
                (Integer) session.getAttribute("userId");

        if (userId == null) {
            throw new ResponseStatusException(
                    HttpStatus.UNAUTHORIZED,
                    "Please log in first"
            );
        }

        String role =
                (String) session.getAttribute("role");

        if (!"Doctor".equals(role)) {
            throw new ResponseStatusException(
                    HttpStatus.FORBIDDEN,
                    "Only doctors can create slots"
            );
        }

        doctorService.createSlot(
                userId,
                request.getServiceId(),
                request.getAppointmentDate(),
                request.getStartTime(),
                request.getEndTime()
        );

        return "Availability slot created successfully";
    }

    @DeleteMapping("/slots/{availId}")
    public String deleteSlot(
            @PathVariable int availId,
            HttpSession session
    ) {
        Integer userId = (Integer) session.getAttribute("userId");

        if (userId == null) {
            throw new ResponseStatusException(
                    HttpStatus.UNAUTHORIZED,
                    "Please log in first"
            );
        }

        String role = (String) session.getAttribute("role");

        if (!"Doctor".equals(role)) {
            throw new ResponseStatusException(
                    HttpStatus.FORBIDDEN,
                    "Only doctors can delete slots"
            );
        }

        doctorService.deleteSlot(userId, availId);

        return "Availability slot deleted successfully";
    }
    @GetMapping("/appointments")
    public List<DoctorAppointment> getAppointments(HttpSession session) {
        Integer userId = (Integer) session.getAttribute("userId");

        if (userId == null) {
            throw new ResponseStatusException(
                    HttpStatus.UNAUTHORIZED,
                    "Please log in first"
            );
        }

        String role = (String) session.getAttribute("role");

        if (!"Doctor".equals(role)) {
            throw new ResponseStatusException(
                    HttpStatus.FORBIDDEN,
                    "Only doctors can view these appointments"
            );
        }

        return doctorService.getDoctorAppointments(userId);
    }

    @GetMapping("/slots")
    public List<AppointmentSlot> getMySlots(HttpSession session) {
        Integer userId = (Integer) session.getAttribute("userId");

        if (userId == null) {
            throw new ResponseStatusException(
                    HttpStatus.UNAUTHORIZED,
                    "Please log in first"
            );
        }

        if (!"Doctor".equals(session.getAttribute("role"))) {
            throw new ResponseStatusException(
                    HttpStatus.FORBIDDEN,
                    "Only doctors can view their slots"
            );
        }

        return doctorService.getMySlots(userId);
    }

}
