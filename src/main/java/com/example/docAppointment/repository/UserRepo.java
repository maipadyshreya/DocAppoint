package com.example.docAppointment.repository;

import com.example.docAppointment.dto.User;
import lombok.RequiredArgsConstructor;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
@RequiredArgsConstructor
public class UserRepo {

    private final JdbcTemplate jdbcTemplate;

    public User findByEmail(String email) {

        String sql = """
                SELECT
                    user_id,
                    name,
                    email,
                    password,
                    role
                FROM users
                WHERE email = ?
                """;

        List<User> users = jdbcTemplate.query(
                sql,
                (rs, rowNum) -> new User(
                        rs.getInt("user_id"),
                        rs.getString("name"),
                        rs.getString("email"),
                        rs.getString("password"),
                        rs.getString("role")
                ),
                email
        );

        if (users.isEmpty()) {
            return null;
        }

        return users.get(0);
    }


    public void save(User user) {

        String sql = """
            INSERT INTO users
                (name, email, password, role)
            VALUES (?, ?, ?, ?)
            """;

        jdbcTemplate.update(
                sql,
                user.getName(),
                user.getEmail(),
                user.getPassword(),
                user.getRole()
        );
    }

    public java.util.List<java.util.Map<String, Object>> findUpcomingAppointments() {
        String sql = """
        SELECT a.appointment_id,
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
