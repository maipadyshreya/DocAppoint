package com.example.docAppointment.repository;

import com.example.docAppointment.dto.AppointmentSlot;
import lombok.RequiredArgsConstructor;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;

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

}
