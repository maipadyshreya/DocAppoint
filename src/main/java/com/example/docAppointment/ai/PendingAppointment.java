package com.example.docAppointment.ai;

public record PendingAppointment(
        String action,
        Integer availId,
        Integer serviceId,
        Integer appointmentId
) {}
