
package com.example.docAppointment.controller;

import jakarta.servlet.http.HttpSession;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class PageController {

    @GetMapping("/")
    public String home(HttpSession session, Model model) {
        model.addAttribute("name", session.getAttribute("name"));
        model.addAttribute("role", session.getAttribute("role"));
        return "index";
    }

    @GetMapping("/login")
    public String login() {
        return "login";
    }

    @GetMapping("/appointments")
    public String appointments(HttpSession session) {
        if (session.getAttribute("userId") == null) {
            return "redirect:/login";
        }

        return "appointments";
    }
}
