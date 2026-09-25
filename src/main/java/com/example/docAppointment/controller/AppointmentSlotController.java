package com.example.docAppointment.controller;

import com.example.docAppointment.dto.AppointmentSlot;
import com.example.docAppointment.service.AppointmentSlotService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

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
    @GetMapping("/user/{userId}")
    public List<AppointmentSlot> getAppointmentByUser(
            @PathVariable int userId) {


        return appointmentSlotService.getAvailableByDoctor(userId);
    }
}
