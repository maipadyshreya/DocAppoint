package com.example.docAppointment.repository;

import lombok.RequiredArgsConstructor;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;

@Repository
@RequiredArgsConstructor
public class AppointmentRepo {
    private final JdbcTemplate jdbcTemplate;

    public int bookAppointment(int userId, int availId, int serviceId) {
        String sql = """
        INSERT INTO appointments (user_id, avail_id, service_id)
        VALUES (?, ?, ?)
        """;

        return jdbcTemplate.update(sql, userId, availId, serviceId);
    }


    public boolean lockAvailableSlot(int availId) {
        String sql = """
        SELECT is_avail
        FROM availability_slots
        WHERE avail_id = ?
        FOR UPDATE
        """;

        return Boolean.TRUE.equals(
                jdbcTemplate.query(
                        sql,
                        rs -> rs.next() && rs.getBoolean("is_avail"),
                        availId
                )
        );
    }

    public int markSlotUnavailable(int availId) {
        String sql = """
        UPDATE availability_slots
        SET is_avail = FALSE
        WHERE avail_id = ?
        """;

        return jdbcTemplate.update(sql, availId);
    }

    public boolean doctorOffersService(int availId, int serviceId) {
        String sql = """
        SELECT COUNT(*)
        FROM availability_slots a
        JOIN doctor_service ds
          ON a.dr_id = ds.dr_id
         AND a.service_id = ds.service_id
        WHERE a.avail_id = ?
          AND a.service_id = ?
        """;

        Integer count = jdbcTemplate.queryForObject(
                sql,
                Integer.class,
                availId,
                serviceId
        );

        return count != null && count > 0;
    }

    public boolean isSlotInFuture(int availId) {
        String sql = """
        SELECT COUNT(*)
        FROM availability_slots
        WHERE avail_id = ?
          AND TIMESTAMP(appointment_date, start_time) > NOW()
        """;

        Integer count = jdbcTemplate.queryForObject(
                sql, Integer.class, availId
        );

        return count != null && count > 0;
    }

    public int cancelAppointment(int appointmentId, int userId) {
        String sql = """
        UPDATE appointments
        SET status = 'CANCELLED'
        WHERE appointment_id = ?
          AND user_id = ?
          AND status = 'SCHEDULED'
        """;

        return jdbcTemplate.update(sql, appointmentId, userId);
    }



    public Integer findCancellableSlotId(int appointmentId, int userId) {
        String sql = """
        SELECT avail_id
        FROM appointments
        WHERE appointment_id = ?
          AND user_id = ?
          AND status = 'SCHEDULED'
        """;

        return jdbcTemplate.query(
                sql,
                rs -> rs.next() ? rs.getInt("avail_id") : null,
                appointmentId,
                userId
        );
    }

    public int reopenFutureSlot(int availId) {
        String sql = """
        UPDATE availability_slots
        SET is_avail = TRUE
        WHERE avail_id = ?
          AND TIMESTAMP(appointment_date, start_time) > NOW()
        """;

        return jdbcTemplate.update(sql, availId);
    }
}
