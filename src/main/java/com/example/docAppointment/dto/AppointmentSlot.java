package com.example.docAppointment.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDate;
import java.time.LocalTime;

@Getter
@Setter
@AllArgsConstructor
public class AppointmentSlot {
    private int availId;
    private int doctorId;
    private String doctorName;
    private String specialty;
    private LocalDate appointmentDate;
    private LocalTime startTime;
    private LocalTime endTime;
}
