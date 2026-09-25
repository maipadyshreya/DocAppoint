package com.example.docAppointment.service;

import com.example.docAppointment.dto.AppointmentSlot;
import com.example.docAppointment.repository.AppointmentSlotRepo;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class AppointmentSlotService {
    private final AppointmentSlotRepo appointmentSlotRepo;

    public List<AppointmentSlot> getAvailableByDoctor(int docId) {
        return appointmentSlotRepo.findAvailableByDoctor(docId);
    }
    public List<AppointmentSlot> getAppointmentByUser(int userId) {
        return appointmentSlotRepo.getAppointmentByUser(userId);
    }
}
