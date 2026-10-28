
package com.example.docAppointment.service;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.support.GeneratedKeyHolder;
import org.springframework.jdbc.support.KeyHolder;

import java.sql.PreparedStatement;
import java.sql.Statement;
import java.sql.Timestamp;
import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

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
        int doctorId = 1;
        int serviceId = 1;

        LocalDateTime start = LocalDateTime.now()
                .plusDays(7)
                .withHour(10)
                .withMinute(0)
                .withSecond(0)
                .withNano(0);

        LocalDateTime end = start.plusMinutes(30);

        // Create a fresh available slot.
        KeyHolder keyHolder = new GeneratedKeyHolder();

        jdbcTemplate.update(connection -> {
            PreparedStatement ps = connection.prepareStatement(
                    """
                    INSERT INTO availability_slots
                        (dr_id, appointment_date, start_time,
                         end_time, is_avail, service_id)
                    VALUES (?, ?, ?, ?, ?, ?)
                    """,
                    Statement.RETURN_GENERATED_KEYS
            );

            ps.setInt(1, doctorId);
            ps.setDate(2, java.sql.Date.valueOf(start.toLocalDate()));
            ps.setTimestamp(3, Timestamp.valueOf(start));
            ps.setTimestamp(4, Timestamp.valueOf(end));
            ps.setBoolean(5, true);
            ps.setInt(6, serviceId);

            return ps;
        }, keyHolder);

        assertNotNull(keyHolder.getKey());

        int availId = keyHolder.getKey().intValue();

        // Book while the notification endpoint is unavailable.
        int appointmentId = appointmentService.bookAppointment(
                userId, availId, serviceId
        );

        // Verify the booking was saved in MySQL.
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

        // Verify the slot was marked unavailable.
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
