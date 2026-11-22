
package com.example.docAppointment.ai;

import com.example.docAppointment.repository.AppointmentRepo;
import com.example.docAppointment.service.AppointmentService;
import lombok.RequiredArgsConstructor;
import org.springframework.ai.tool.annotation.Tool;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class AppointmentTools {

    private final AppointmentService appointmentService;
    private final AppointmentRepo appointmentRepo;


    public String bookAppointment(int userId, int availId, int serviceId) {
        int appointmentId = appointmentService.bookAppointment(
                userId, availId, serviceId
        );

        return "Appointment booked successfully. Appointment ID: " + appointmentId;
    }


    public String cancelAppointment(int appointmentId, int userId) {
        appointmentService.cancelAppointment(appointmentId, userId);

        return "Appointment cancelled successfully.";
    }

    @Tool(description = "Find the next 20 available future doctor appointment slots.")
    public String checkAvailability() {
        return appointmentRepo.findAvailableSlots().toString();
    }


    public String getMyAppointments(int userId) {
        return appointmentRepo.findMyAppointments(userId).toString();
    }



}
