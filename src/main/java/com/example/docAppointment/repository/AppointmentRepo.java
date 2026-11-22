package com.example.docAppointment.repository;

import lombok.RequiredArgsConstructor;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.support.GeneratedKeyHolder;
import org.springframework.jdbc.support.KeyHolder;
import org.springframework.stereotype.Repository;

import java.sql.PreparedStatement;
import java.sql.Statement;

@Repository
@RequiredArgsConstructor
public class AppointmentRepo {
    private final JdbcTemplate jdbcTemplate;


    public int bookAppointment(int userId, int availId, int serviceId) {
        String sql = """
        INSERT INTO appointments (user_id, avail_id, service_id)
        VALUES (?, ?, ?)
        """;

        KeyHolder keyHolder = new GeneratedKeyHolder();

        jdbcTemplate.update(connection -> {
            PreparedStatement ps = connection.prepareStatement(
                    sql,
                    Statement.RETURN_GENERATED_KEYS
            );

            ps.setInt(1, userId);
            ps.setInt(2, availId);
            ps.setInt(3, serviceId);

            return ps;
        }, keyHolder);

        if (keyHolder.getKey() == null) {
            throw new IllegalStateException(
                    "Failed to retrieve appointment ID"
            );
        }

        return keyHolder.getKey().intValue();
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

    public java.util.List<java.util.Map<String, Object>> findAvailableSlots() {
        String sql = """
        SELECT avail_id, dr_id, service_id,
               appointment_date, start_time
        FROM availability_slots
        WHERE is_avail = TRUE
          AND TIMESTAMP(appointment_date, start_time) > NOW()
        ORDER BY appointment_date, start_time
        LIMIT 20
        """;

        return jdbcTemplate.queryForList(sql);
    }

    public java.util.List<java.util.Map<String, Object>> findMyAppointments(int userId) {
        String sql = """
        SELECT a.appointment_id, a.status,
               s.dr_id, s.service_id,
               s.appointment_date, s.start_time
        FROM appointments a
        JOIN availability_slots s ON a.avail_id = s.avail_id
        WHERE a.user_id = ?
        ORDER BY s.appointment_date DESC, s.start_time DESC
        LIMIT 20
        """;

        return jdbcTemplate.queryForList(sql, userId);
    }
    public java.util.List<java.util.Map<String, Object>> findUpcomingAppointments() {
        String sql = """
        SELECT
            a.appointment_id,
            a.user_id,
            u.name AS patient_name,
            u.email AS patient_email,
            s.appointment_date,
            s.start_time,
            s.dr_id,
            s.service_id
        FROM appointments a
        JOIN availability_slots s
            ON a.avail_id = s.avail_id
        JOIN users u
            ON a.user_id = u.user_id
        WHERE a.status = 'SCHEDULED'
          AND TIMESTAMP(s.appointment_date, s.start_time)
              BETWEEN NOW() AND DATE_ADD(NOW(), INTERVAL 24 HOUR)
        ORDER BY s.appointment_date, s.start_time
        """;

        return jdbcTemplate.queryForList(sql);
    }

}
