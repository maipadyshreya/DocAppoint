package com.example.docAppointment.service;

import com.example.docAppointment.dto.LoginUser;
import com.example.docAppointment.dto.User;
import com.example.docAppointment.repository.UserRepo;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class AuthService {

    private final UserRepo userRepo;
    private final PasswordEncoder passwordEncoder;

    public User login(LoginUser login){
        //Checking if user exists by email
        User user = userRepo.findByEmail(login.getEmail());

        //If user with the entered email does not exist
        if(user ==null ){
            return null;
        }

        //Check entered password
        boolean checkPassword = passwordEncoder.matches(login.getPassword(), user.getPassword());

        //if password incorrect
        if(checkPassword ==false){
            return null;
        }

        return user;
    }

    public void register(String name, String email, String password) {

        if (name == null || name.isBlank() ||
                email == null || email.isBlank() ||
                password == null || password.length() < 8) {
            throw new IllegalArgumentException("Invalid registration details");
        }

        email = email.trim().toLowerCase();

        User existingUser = userRepo.findByEmail(email);

        if (existingUser != null) {
            throw new IllegalArgumentException("Email already registered");
        }

        String hashedPassword = passwordEncoder.encode(password);

        User newUser = new User(
                0,
                name.trim(),
                email,
                hashedPassword,
                "Patient"
        );

        userRepo.save(newUser);
    }


}
