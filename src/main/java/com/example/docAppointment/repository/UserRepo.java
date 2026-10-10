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
}
