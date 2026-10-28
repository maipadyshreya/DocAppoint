
package com.example.docAppointment.service;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.jdbc.core.JdbcTemplate;

import static org.junit.jupiter.api.Assertions.assertEquals;

@SpringBootTest(properties = {
        "notification.url=http://127.0.0.1:1/mock-notifications"
})
class AppointmentServiceIntegrationTest {

    @Autowired
    private AppointmentService appointmentService;

    @Autowired
    private JdbcTemplate jdbcTemplate;

    @Test
    void bookingSucceedsWhenNotificationServiceIsDown() {

        int userId = 1;
        int availId = 12;
        int serviceId = 1;

        // Book using the real appointment service and MySQL.
        int appointmentId = appointmentService.bookAppointment(
                userId, availId, serviceId
        );

        // Confirm the appointment was saved.
        String status = jdbcTemplate.queryForObject(
                """
                SELECT status
                FROM appointments
                WHERE appointment_id = ?
                """,
                String.class,
                appointmentId
        );

        assertEquals("SCHEDULED", status);

        // Confirm the slot is no longer available.
        Boolean available = jdbcTemplate.queryForObject(
                """
                SELECT is_avail
                FROM availability_slots
                WHERE avail_id = ?
                """,
                Boolean.class,
                availId
        );

        assertEquals(Boolean.FALSE, available);
    }
}
