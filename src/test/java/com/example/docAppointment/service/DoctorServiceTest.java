
package com.example.docAppointment.service;

import com.example.docAppointment.repository.AppointmentSlotRepo;
import com.example.docAppointment.repository.DoctorRepo;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import org.springframework.http.HttpStatus;
import org.springframework.web.server.ResponseStatusException;

import java.time.LocalDate;
import java.time.LocalTime;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class DoctorServiceTest {

    @Mock
    private DoctorRepo doctorRepo;

    @Mock
    private AppointmentSlotRepo appointmentSlotRepo;

    @InjectMocks
    private DoctorService doctorService;

    private LocalDate futureDate;

    @BeforeEach
    void setUp() {
        futureDate = LocalDate.now().plusDays(7);
    }

    // TEST 1: Two hours should create four 30-minute slots
    @Test
    void createSlotSplitsTwoHoursIntoFourSlots() {

        when(doctorRepo.findDoctorIdByUserId(2))
                .thenReturn(1);

        when(doctorRepo.doctorOffersService(1, 1))
                .thenReturn(true);

        doctorService.createSlot(
                2,
                1,
                futureDate,
                LocalTime.of(9, 0),
                LocalTime.of(11, 0)
        );

        verify(appointmentSlotRepo).createSlot(
                1, 1, futureDate,
                LocalTime.of(9, 0),
                LocalTime.of(9, 30)
        );

        verify(appointmentSlotRepo).createSlot(
                1, 1, futureDate,
                LocalTime.of(9, 30),
                LocalTime.of(10, 0)
        );

        verify(appointmentSlotRepo).createSlot(
                1, 1, futureDate,
                LocalTime.of(10, 0),
                LocalTime.of(10, 30)
        );

        verify(appointmentSlotRepo).createSlot(
                1, 1, futureDate,
                LocalTime.of(10, 30),
                LocalTime.of(11, 0)
        );

        verify(appointmentSlotRepo, times(4))
                .createSlot(
                        eq(1),
                        eq(1),
                        eq(futureDate),
                        any(LocalTime.class),
                        any(LocalTime.class)
                );
    }

    // TEST 2: Reject slots shorter than 30 minutes
    @Test
    void rejectTimeRangeShorterThanThirtyMinutes() {

        when(doctorRepo.findDoctorIdByUserId(2))
                .thenReturn(1);

        when(doctorRepo.doctorOffersService(1, 1))
                .thenReturn(true);

        ResponseStatusException exception = assertThrows(
                ResponseStatusException.class,
                () -> doctorService.createSlot(
                        2,
                        1,
                        futureDate,
                        LocalTime.of(9, 0),
                        LocalTime.of(9, 15)
                )
        );

        assertEquals(
                HttpStatus.BAD_REQUEST,
                exception.getStatusCode()
        );

        verifyNoInteractions(appointmentSlotRepo);
    }

    // TEST 3: Reject a service the doctor does not offer
    @Test
    void rejectServiceNotOfferedByDoctor() {

        when(doctorRepo.findDoctorIdByUserId(2))
                .thenReturn(1);

        when(doctorRepo.doctorOffersService(1, 99))
                .thenReturn(false);

        ResponseStatusException exception = assertThrows(
                ResponseStatusException.class,
                () -> doctorService.createSlot(
                        2,
                        99,
                        futureDate,
                        LocalTime.of(9, 0),
                        LocalTime.of(10, 0)
                )
        );

        assertEquals(
                HttpStatus.BAD_REQUEST,
                exception.getStatusCode()
        );

        verifyNoInteractions(appointmentSlotRepo);
    }

    // TEST 4: Reject a user who is not a doctor
    @Test
    void rejectUserWhoIsNotDoctor() {

        when(doctorRepo.findDoctorIdByUserId(1))
                .thenReturn(null);

        ResponseStatusException exception = assertThrows(
                ResponseStatusException.class,
                () -> doctorService.createSlot(
                        1,
                        1,
                        futureDate,
                        LocalTime.of(9, 0),
                        LocalTime.of(10, 0)
                )
        );

        assertEquals(
                HttpStatus.FORBIDDEN,
                exception.getStatusCode()
        );

        verifyNoInteractions(appointmentSlotRepo);
    }

    // TEST 5: Reject end time before start time
    @Test
    void rejectEndTimeBeforeStartTime() {

        when(doctorRepo.findDoctorIdByUserId(2))
                .thenReturn(1);

        when(doctorRepo.doctorOffersService(1, 1))
                .thenReturn(true);

        ResponseStatusException exception = assertThrows(
                ResponseStatusException.class,
                () -> doctorService.createSlot(
                        2,
                        1,
                        futureDate,
                        LocalTime.of(11, 0),
                        LocalTime.of(9, 0)
                )
        );

        assertEquals(
                HttpStatus.BAD_REQUEST,
                exception.getStatusCode()
        );

        verifyNoInteractions(appointmentSlotRepo);
    }

    // TEST 6: Reject availability in the past
    @Test
    void rejectPastAppointmentDate() {

        when(doctorRepo.findDoctorIdByUserId(2))
                .thenReturn(1);

        when(doctorRepo.doctorOffersService(1, 1))
                .thenReturn(true);

        ResponseStatusException exception = assertThrows(
                ResponseStatusException.class,
                () -> doctorService.createSlot(
                        2,
                        1,
                        LocalDate.now().minusDays(1),
                        LocalTime.of(9, 0),
                        LocalTime.of(10, 0)
                )
        );

        assertEquals(
                HttpStatus.BAD_REQUEST,
                exception.getStatusCode()
        );

        verifyNoInteractions(appointmentSlotRepo);
    }
}
