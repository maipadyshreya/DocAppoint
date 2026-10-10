package com.example.docAppointment.service;

import com.example.docAppointment.dto.AppointmentSlot;
import com.example.docAppointment.dto.PatientAppointment;
import com.example.docAppointment.repository.AppointmentSlotRepo;

import lombok.RequiredArgsConstructor;

import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import java.time.LocalDate;
import java.util.List;

@Service
@RequiredArgsConstructor
public class AppointmentSlotService {

    private final AppointmentSlotRepo appointmentSlotRepo;


    public List<AppointmentSlot> getAvailableByDoctor(int doctorId) {
        return appointmentSlotRepo.findAvailableByDoctor(doctorId);
    }


    public List<AppointmentSlot> getAppointmentByUser(int userId) {
        return appointmentSlotRepo.getAppointmentByUser(userId);
    }


    public List<PatientAppointment> getPatientAppointments(int userId) {
        return appointmentSlotRepo.findPatientAppointments(userId);
    }


    public List<AppointmentSlot> searchAvailableSlots(
            Integer doctorId,
            Integer serviceId,
            LocalDate date,
            int page,
            int size
    ) {


        if (page < 0 || size < 1 || size > 100) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "Invalid pagination parameters"
            );
        }


        int offset;

        try {
            offset = Math.multiplyExact(page, size);
        } catch (ArithmeticException e) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "Page number is too large"
            );
        }


        return appointmentSlotRepo.searchAvailableSlots(
                doctorId,
                serviceId,
                date,
                size,
                offset
        );
    }
}
