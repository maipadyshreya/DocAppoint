package com.example.docAppointment;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
@EnableScheduling
public class DocAppointmentApplication {

	public static void main(String[] args) {
		SpringApplication.run(DocAppointmentApplication.class, args);
	}

}
