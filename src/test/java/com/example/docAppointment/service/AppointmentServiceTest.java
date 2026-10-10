
package com.example.docAppointment.service;

import com.example.docAppointment.repository.AppointmentRepo;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.web.server.ResponseStatusException;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class AppointmentServiceTest {

    @Mock
    private AppointmentRepo appointmentRepo;

    @InjectMocks
    private AppointmentService appointmentService;

    // TEST 1: Successfully book an available appointment
    @Test
    void bookAvailableAppointmentSuccessfully() {

        int userId = 1;
        int availId = 7;
        int serviceId = 1;

        when(appointmentRepo.lockAvailableSlot(availId))
                .thenReturn(true);

        when(appointmentRepo.doctorOffersService(availId, serviceId))
                .thenReturn(true);

        when(appointmentRepo.isSlotInFuture(availId))
                .thenReturn(true);

        when(appointmentRepo.bookAppointment(userId, availId, serviceId))
                .thenReturn(1);

        int result = appointmentService.bookAppointment(
                userId, availId, serviceId
        );

        assertEquals(1, result);

        verify(appointmentRepo).markSlotUnavailable(availId);
    }

    // TEST 2: Reject booking when slot is unavailable
    @Test
    void rejectBookingUnavailableSlot() {

        when(appointmentRepo.lockAvailableSlot(7))
                .thenReturn(false);

        ResponseStatusException exception = assertThrows(
                ResponseStatusException.class,
                () -> appointmentService.bookAppointment(1, 7, 1)
        );

        assertEquals(
                HttpStatus.CONFLICT,
                exception.getStatusCode()
        );

        verify(appointmentRepo, never())
                .bookAppointment(anyInt(), anyInt(), anyInt());

        verify(appointmentRepo, never())
                .markSlotUnavailable(anyInt());
    }

    // TEST 3: Reject booking when doctor does not offer service
    @Test
    void rejectBookingInvalidService() {

        when(appointmentRepo.lockAvailableSlot(7))
                .thenReturn(true);

        when(appointmentRepo.doctorOffersService(7, 99))
                .thenReturn(false);

        ResponseStatusException exception = assertThrows(
                ResponseStatusException.class,
                () -> appointmentService.bookAppointment(1, 7, 99)
        );

        assertEquals(
                HttpStatus.BAD_REQUEST,
                exception.getStatusCode()
        );

        verify(appointmentRepo, never())
                .bookAppointment(anyInt(), anyInt(), anyInt());
    }

    // TEST 4: Reject cancellation by someone other than owner
    @Test
    void rejectCancellationByNonOwner() {

        int appointmentId = 10;
        int wrongUserId = 99;

        when(appointmentRepo.findCancellableSlotId(
                appointmentId, wrongUserId
        )).thenReturn(null);

        ResponseStatusException exception = assertThrows(
                ResponseStatusException.class,
                () -> appointmentService.cancelAppointment(
                        appointmentId, wrongUserId
                )
        );

        assertEquals(
                HttpStatus.FORBIDDEN,
                exception.getStatusCode()
        );

        verify(appointmentRepo, never())
                .cancelAppointment(anyInt(), anyInt());

        verify(appointmentRepo, never())
                .reopenFutureSlot(anyInt());
    }

    // TEST 5: Successfully cancel own appointment
    @Test
    void cancelOwnAppointmentSuccessfully() {

        int appointmentId = 10;
        int userId = 1;
        int availId = 7;

        when(appointmentRepo.findCancellableSlotId(
                appointmentId, userId
        )).thenReturn(availId);

        when(appointmentRepo.cancelAppointment(
                appointmentId, userId
        )).thenReturn(1);

        appointmentService.cancelAppointment(
                appointmentId, userId
        );

        verify(appointmentRepo).cancelAppointment(
                appointmentId, userId
        );

        verify(appointmentRepo).reopenFutureSlot(availId);
    }
}
