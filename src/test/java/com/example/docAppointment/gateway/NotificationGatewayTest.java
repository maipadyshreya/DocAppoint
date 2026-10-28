package com.example.docAppointment.gateway;

import com.example.docAppointment.dto.NotificationReq;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;

class NotificationGatewayTest {

    @Test
    void notificationFailureDoesNotThrow() {

        NotificationGateway gateway =
                new NotificationGateway(
                        "http://127.0.0.1:1/mock-notifications"
                );

        NotificationReq request = new NotificationReq(
                "booking-test-1",
                "patient@example.com",
                "BOOKING_CONFIRMATION",
                "Your appointment is confirmed."
        );

        assertDoesNotThrow(() -> gateway.send(request));
    }
}
