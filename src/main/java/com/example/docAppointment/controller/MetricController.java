package com.example.docAppointment.controller;

import com.example.docAppointment.service.BookingMetric;import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

@RestController
@RequiredArgsConstructor
public class MetricController {

    private final BookingMetric bookingMetric;

    @GetMapping("/metrics")
    public Map<String, Long> metrics() {
        return Map.of(
                "successfulBookings",
                bookingMetric.getSuccessfulBookings()
        );
    }
}
