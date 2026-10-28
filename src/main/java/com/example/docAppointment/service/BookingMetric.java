package com.example.docAppointment.service;

import org.springframework.stereotype.Service;
import java.util.concurrent.atomic.AtomicLong;

@Service
public class BookingMetric {

    private final AtomicLong successfulBookings = new AtomicLong();

    public void recordSuccess() {
        successfulBookings.incrementAndGet();
    }

    public long getSuccessfulBookings() {
        return successfulBookings.get();
    }
}
