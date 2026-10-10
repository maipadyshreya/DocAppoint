package com.example.docAppointment.service;

import com.example.docAppointment.repository.AppointmentRepo;
import lombok.RequiredArgsConstructor;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

@Service
@RequiredArgsConstructor
public class AppointmentService {
 private final AppointmentRepo appointmentRepo;

    @Transactional
    public int bookAppointment(int userId, int availId, int serviceId) {

        if (!appointmentRepo.lockAvailableSlot(availId)) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "Appointment slot is not available");
        }
        if (!appointmentRepo.doctorOffersService(availId, serviceId)) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "The selected doctor does not offer this service");
        }
        if (!appointmentRepo.isSlotInFuture(availId)) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Cannot book an appointment in the past");
        }

        int result;

        try {
            result = appointmentRepo.bookAppointment(userId, availId, serviceId);
        } catch (DuplicateKeyException e) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "This appointment slot is already booked");
        }
        appointmentRepo.markSlotUnavailable(availId);

        return result;
    }

    @Transactional
    public void cancelAppointment(int appointmentId, int userId) {

        Integer availId = appointmentRepo.findCancellableSlotId(
                appointmentId, userId
        );

        if (availId == null) {
            throw new ResponseStatusException(
                    HttpStatus.FORBIDDEN,
                    "Appointment cannot be cancelled or does not belong to you"
            );
        }

        // Lock the slot before changing the appointment.
        // Booking uses the same lock.
        appointmentRepo.lockAvailableSlot(availId);

        int updated = appointmentRepo.cancelAppointment(
                appointmentId, userId
        );

        if (updated == 0) {
            throw new ResponseStatusException(
                    HttpStatus.FORBIDDEN,
                    "Appointment cannot be cancelled or does not belong to you"
            );
        }

        appointmentRepo.reopenFutureSlot(availId);
    }
}
