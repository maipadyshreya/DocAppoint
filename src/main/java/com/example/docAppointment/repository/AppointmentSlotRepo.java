package com.example.docAppointment.repository;

import com.example.docAppointment.dto.AppointmentSlot;
import com.example.docAppointment.dto.PatientAppointment;
import lombok.RequiredArgsConstructor;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;

@Repository
@RequiredArgsConstructor
public class AppointmentSlotRepo {
    private final JdbcTemplate jdbcTemplate;

    public List<AppointmentSlot> findAvailableByDoctor(int docId) {
        String sql = """
                SELECT
                    a.avail_id,
                    d.dr_id,
                    n.name AS doctor_name,
                    d.specialty,
                    a.appointment_date,
                    a.start_time,
                    a.end_time
                FROM availability_slots a
                JOIN doctors d ON a.dr_id = d.dr_id
                JOIN users n ON d.user_id = n.user_id
                WHERE a.is_avail = TRUE
                AND d.dr_id = ?
                """;
        return jdbcTemplate.query(sql, (rs, rowNum) -> new AppointmentSlot(
                        rs.getInt("avail_id"),
                        rs.getInt("dr_id"),
                        rs.getString("doctor_name"),
                        rs.getString("specialty"),
                        rs.getDate("appointment_date").toLocalDate(),
                        rs.getTime("start_time").toLocalTime(),
                        rs.getTime("end_time").toLocalTime()
                ),
                docId
        );
    }
    public List<AppointmentSlot> getAppointmentByUser(int userId) {

        String sql = """
            SELECT
                a.avail_id,
                d.dr_id,
                n.name AS dr_name,
                d.specialty,
                a.appointment_date,
                a.start_time,
                a.end_time
            FROM appointments ap
            JOIN availability_slots a ON ap.avail_id = a.avail_id
            JOIN doctors d ON a.dr_id = d.dr_id
            JOIN users n ON d.user_id = n.user_id
            

            WHERE ap.user_id = ?
            """;

        return jdbcTemplate.query(
                sql,
                (rs, rowNum) -> new AppointmentSlot(
                        rs.getInt("avail_id"),
                        rs.getInt("dr_id"),
                        rs.getString("dr_name"),
                        rs.getString("specialty"),
                        rs.getDate("appointment_date").toLocalDate(),
                        rs.getTime("start_time").toLocalTime(),
                        rs.getTime("end_time").toLocalTime()
                ),
                userId
        );
    }
    public int createSlot(
            int doctorId,
            int serviceId,
            LocalDate date,
            LocalTime startTime,
            LocalTime endTime
    ) {
        String sql = """
        INSERT INTO availability_slots
        (dr_id, service_id, appointment_date, start_time, end_time, is_avail)
        VALUES (?, ?, ?, ?, ?, TRUE)
        """;

        return jdbcTemplate.update(
                sql,
                doctorId,
                serviceId,
                date,
                startTime,
                endTime
        );
    }

    public int deleteDoctorSlot(int availId, int doctorId) {
        String sql = """
        DELETE FROM availability_slots
        WHERE avail_id = ?
          AND dr_id = ?
          AND is_avail = TRUE
          AND NOT EXISTS (
              SELECT 1
              FROM appointments
              WHERE appointments.avail_id = availability_slots.avail_id
          )
        """;

        return jdbcTemplate.update(sql, availId, doctorId);
    }
    public List<PatientAppointment> findPatientAppointments(int userId) {
        String sql = """
        SELECT
            ap.appointment_id,
            u.name AS doctor_name,
            d.specialty,
            s.service_name,
            a.appointment_date,
            a.start_time,
            a.end_time,
            ap.status
        FROM appointments ap
        JOIN availability_slots a ON ap.avail_id = a.avail_id
        JOIN doctors d ON a.dr_id = d.dr_id
        JOIN users u ON d.user_id = u.user_id
        JOIN services s ON ap.service_id = s.service_id
        WHERE ap.user_id = ?
        ORDER BY a.appointment_date DESC, a.start_time DESC
        """;

        return jdbcTemplate.query(sql, (rs, rowNum) ->
                new PatientAppointment(
                        rs.getInt("appointment_id"),
                        rs.getString("doctor_name"),
                        rs.getString("specialty"),
                        rs.getString("service_name"),
                        rs.getDate("appointment_date").toLocalDate(),
                        rs.getTime("start_time").toLocalTime(),
                        rs.getTime("end_time").toLocalTime(),
                        rs.getString("status")
                ), userId);
    }

    public List<AppointmentSlot> searchAvailableSlots(
            Integer doctorId,
            Integer serviceId,
            LocalDate date,
            int limit,
            int offset
    ) {
        String sql = """
        SELECT
            a.avail_id,
            d.dr_id,
            u.name AS doctor_name,
            d.specialty,
            a.appointment_date,
            a.start_time,
            a.end_time
        FROM availability_slots a
        JOIN doctors d ON a.dr_id = d.dr_id
        JOIN users u ON d.user_id = u.user_id
        WHERE a.is_avail = TRUE
          AND TIMESTAMP(a.appointment_date, a.start_time) > NOW()
          AND (? IS NULL OR d.dr_id = ?)
          AND (? IS NULL OR a.appointment_date = ?)
      AND (? IS NULL OR a.service_id = ?)
        ORDER BY a.appointment_date, a.start_time, a.avail_id
        LIMIT ? OFFSET ?
        """;

        return jdbcTemplate.query(
                sql,
                (rs, rowNum) -> new AppointmentSlot(
                        rs.getInt("avail_id"),
                        rs.getInt("dr_id"),
                        rs.getString("doctor_name"),
                        rs.getString("specialty"),
                        rs.getDate("appointment_date").toLocalDate(),
                        rs.getTime("start_time").toLocalTime(),
                        rs.getTime("end_time").toLocalTime()
                ),
                doctorId, doctorId, date, date, serviceId, serviceId, limit, offset );
    }

}
