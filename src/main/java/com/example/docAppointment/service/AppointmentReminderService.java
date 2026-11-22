
package com.example.docAppointment.service;

import com.example.docAppointment.repository.AppointmentRepo;
import lombok.RequiredArgsConstructor;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.util.HashSet;
import java.util.Map;
import java.util.Set;

@Service
@RequiredArgsConstructor
public class AppointmentReminderService {

    private final AppointmentRepo appointmentRepo;
    private final NotificationService notificationService;
    private final ChatClient.Builder chatClientBuilder;

    private final Set<Integer> sentReminders = new HashSet<>();

    @Scheduled(cron = "0 0 * * * *")
    public void scheduledReminders() {
        sendReminders();
    }

    public synchronized String sendReminders() {

        var appointments = appointmentRepo.findUpcomingAppointments();
        int sent = 0;

        for (Map<String, Object> appointment : appointments) {

            int appointmentId =
                    ((Number) appointment.get("appointment_id")).intValue();

            if (sentReminders.contains(appointmentId)) {
                continue;
            }

            String name =
                    String.valueOf(appointment.get("patient_name"));

            String email =
                    String.valueOf(appointment.get("patient_email"));

            String date =
                    String.valueOf(appointment.get("appointment_date"));

            String time =
                    String.valueOf(appointment.get("start_time"));

            String prompt = """
                    Write a short, friendly appointment reminder.
                    Include the patient's name, appointment date,
                    and appointment time.
                    Do not invent medical advice.

                    Patient: %s
                    Date: %s
                    Time: %s
                    """.formatted(name, date, time);

            try {
                String message = chatClientBuilder.build()
                        .prompt()
                        .user(prompt)
                        .call()
                        .content();

                if (message == null || message.isBlank()) {
                    continue;
                }

                notificationService.sendAfterCommit(
                        "reminder-" + appointmentId,
                        email,
                        "APPOINTMENT_REMINDER",
                        message
                );

                sentReminders.add(appointmentId);
                sent++;

            } catch (Exception e) {
                System.err.println(
                        "Reminder failed for appointment "
                                + appointmentId + ": " + e.getMessage()
                );
            }
        }

        return "AI reminders sent: " + sent;
    }
}
