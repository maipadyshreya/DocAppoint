package com.example.docAppointment.ai;

public record AppointmentRequest(
        String action,
        Integer availId,
        Integer serviceId,
        Integer appointmentId
) {}
