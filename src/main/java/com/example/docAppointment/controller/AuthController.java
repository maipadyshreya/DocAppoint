package com.example.docAppointment.controller;

import com.example.docAppointment.dto.LoginUser;
import com.example.docAppointment.dto.User;
import com.example.docAppointment.service.AuthService;
import jakarta.servlet.http.HttpSession;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/auth")
@RequiredArgsConstructor
public class AuthController {

    private final AuthService authService;

    @PostMapping("/login")
    public ResponseEntity<String> login(@RequestBody LoginUser login, HttpSession httpSession){
    User user = authService.login(login);

    if (user == null){
        return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("Invalid Password or email");
    }

        httpSession.setAttribute("userId", user.getUserId());
        httpSession.setAttribute("name", user.getName());
        httpSession.setAttribute("role", user.getRole());

        return ResponseEntity.ok("Login successful");
    }

    @PostMapping("/logout")
    public ResponseEntity<String> logout(HttpSession httpSession) {

        httpSession.invalidate();

        return ResponseEntity.ok("Logout successful");
    }

    @GetMapping("/session")
    public ResponseEntity<Map<String, Object>> checkSession(
            HttpSession session
    ) {
        Integer userId = (Integer) session.getAttribute("userId");
        String name = (String) session.getAttribute("name");
        String role = (String) session.getAttribute("role");

        if (userId == null) {
            return ResponseEntity
                    .status(HttpStatus.UNAUTHORIZED)
                    .build();
        }

        Map<String, Object> user = new HashMap<>();

        user.put("userId", userId);
        user.put("name", name);
        user.put("role", role);

        return ResponseEntity.ok(user);
    }
}
