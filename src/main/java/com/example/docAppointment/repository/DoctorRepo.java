package com.example.docAppointment.repository;

import com.example.docAppointment.dto.DoctorAppointment;
import lombok.RequiredArgsConstructor;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
@RequiredArgsConstructor
public class DoctorRepo {
    private final JdbcTemplate jdbcTemplate;

    public Integer findDoctorIdByUserId(int userId) {
        String sql = """
            SELECT dr_id
            FROM doctors
            WHERE user_id = ?
            """;

        return jdbcTemplate.query(
                sql,
                rs -> rs.next() ? rs.getInt("dr_id") : null,
                userId
        );
    }

    public List<DoctorAppointment> findDoctorAppointments(int doctorId) {
        String sql = """
        SELECT
            ap.appointment_id,
            u.name AS patient_name,
            s.service_name,
            a.appointment_date,
            a.start_time,
            a.end_time,
            ap.status
        FROM appointments ap
        JOIN users u ON ap.user_id = u.user_id
        JOIN services s ON ap.service_id = s.service_id
        JOIN availability_slots a ON ap.avail_id = a.avail_id
        WHERE a.dr_id = ?
        ORDER BY a.appointment_date DESC, a.start_time DESC
        """;

        return jdbcTemplate.query(sql, (rs, rowNum) ->
                new DoctorAppointment(
                        rs.getInt("appointment_id"),
                        rs.getString("patient_name"),
                        rs.getString("service_name"),
                        rs.getDate("appointment_date").toLocalDate(),
                        rs.getTime("start_time").toLocalTime(),
                        rs.getTime("end_time").toLocalTime(),
                        rs.getString("status")
                ), doctorId);
    }
    public boolean doctorOffersService(int doctorId, int serviceId) {
        String sql = """
        SELECT COUNT(*)
        FROM doctor_service
        WHERE dr_id = ?
          AND service_id = ?
        """;

        Integer count = jdbcTemplate.queryForObject(
                sql,
                Integer.class,
                doctorId,
                serviceId
        );

        return count != null && count > 0;
    }
}
