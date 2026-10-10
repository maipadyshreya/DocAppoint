package com.example.docAppointment.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@AllArgsConstructor
public class User {

    private int userId;
    private String name;
    private String email;
    private String password;
    private String role;
}
