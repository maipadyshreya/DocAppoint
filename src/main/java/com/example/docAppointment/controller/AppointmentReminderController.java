
package com.example.docAppointment.controller;

import com.example.docAppointment.service.AppointmentReminderService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

@RestController
@RequestMapping("/api/ai/reminders")
@RequiredArgsConstructor
public class AppointmentReminderController {

    private final AppointmentReminderService reminderService;

    @PostMapping("/trigger")
    public String triggerReminders(
            jakarta.servlet.http.HttpSession session
    ) {
        Integer userId = (Integer) session.getAttribute("userId");
        String role = (String) session.getAttribute("role");

        if (!"DOCTOR".equalsIgnoreCase(role)) {
            throw new ResponseStatusException(
                    HttpStatus.FORBIDDEN,
                    "Doctor access required"
            );
        }

        return reminderService.sendReminders();
    }
}
