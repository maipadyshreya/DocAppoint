package com.example.docAppointment.service;

import com.example.docAppointment.dto.AppointmentSlot;
import com.example.docAppointment.dto.DoctorAppointment;
import com.example.docAppointment.repository.AppointmentSlotRepo;
import com.example.docAppointment.repository.DoctorRepo;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;

@Service
@RequiredArgsConstructor
public class DoctorService {
    private final DoctorRepo doctorRepo;
    private final AppointmentSlotRepo appointmentSlotRepo;


    @Transactional
    public void createSlot(
            int userId,
            int serviceId,
            LocalDate date,
            LocalTime startTime,
            LocalTime endTime
    ) {

        Integer doctorId = doctorRepo.findDoctorIdByUserId(userId);

        if (doctorId == null) {
            throw new ResponseStatusException(
                    HttpStatus.FORBIDDEN,
                    "Only doctors can create availability"
            );
        }

        // Check date and times
        if (date == null || startTime == null || endTime == null) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "Date and times are required"
            );
        }

        // Validate service
        if (serviceId <= 0 ||
                !doctorRepo.doctorOffersService(doctorId, serviceId)) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "Doctor does not offer this service"
            );
        }


        if (!endTime.isAfter(startTime)) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "End time must be after start time"
            );
        }

        // Appointment in future
        if (!date.atTime(startTime).isAfter(
                java.time.LocalDateTime.now())) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "Appointment must be in the future"
            );
        }

        // Calculate total duration
        long minutes = java.time.Duration.between(
                startTime, endTime
        ).toMinutes();

        // 30 min slots
        if (minutes < 30 || minutes % 30 != 0) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "Time range must be divisible into 30-minute slots"
            );
        }

        // Split slots
        LocalTime current = startTime;

        while (current.isBefore(endTime)) {

            LocalTime next = current.plusMinutes(30);

            appointmentSlotRepo.createSlot(
                    doctorId,
                    serviceId,
                    date,
                    current,
                    next
            );

            current = next;
        }
    }


    @Transactional
    public void deleteSlot(int userId, int availId) {

        Integer doctorId = doctorRepo.findDoctorIdByUserId(userId);

        if (doctorId == null) {
            throw new ResponseStatusException(
                    HttpStatus.FORBIDDEN,
                    "Only doctors can delete availability slots"
            );
        }

        int deleted = appointmentSlotRepo.deleteDoctorSlot(
                availId, doctorId
        );

        if (deleted == 0) {
            throw new ResponseStatusException(
                    HttpStatus.CONFLICT,
                    "Slot cannot be deleted. It may be booked, have appointment history, or belong to another doctor."
            );
        }
    }
    public List<DoctorAppointment> getDoctorAppointments(int userId) {
        Integer doctorId = doctorRepo.findDoctorIdByUserId(userId);

        if (doctorId == null) {
            throw new ResponseStatusException(
                    HttpStatus.FORBIDDEN,
                    "Only doctors can view doctor appointments"
            );
        }

        return doctorRepo.findDoctorAppointments(doctorId);
    }

    public List<AppointmentSlot> getMySlots(int userId) {

        Integer doctorId = doctorRepo.findDoctorIdByUserId(userId);

        if (doctorId == null) {
            throw new ResponseStatusException(
                    HttpStatus.FORBIDDEN,
                    "Only doctors can view availability slots"
            );
        }

        return appointmentSlotRepo.findAvailableByDoctor(doctorId);
    }



}
