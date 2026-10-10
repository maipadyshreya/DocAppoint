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

}
