package com.example.docAppointment.dto;

import lombok.Getter;
import lombok.Setter;

import java.time.LocalDate;
import java.time.LocalTime;

@Getter
@Setter
public class CreateSlotDoc {
    private int serviceId;
    private LocalDate appointmentDate;
    private LocalTime startTime;
    private LocalTime endTime;
}
