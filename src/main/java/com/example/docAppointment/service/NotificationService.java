
package com.example.docAppointment.service;

import com.example.docAppointment.dto.NotificationReq;
import lombok.RequiredArgsConstructor;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class NotificationService {

    private final ApplicationEventPublisher publisher;

    public void sendAfterCommit(
            String notificationId,
            String email,
            String type,
            String message) {

        publisher.publishEvent(
                new NotificationReq(
                        notificationId, email, type, message
                )
        );
    }
}
