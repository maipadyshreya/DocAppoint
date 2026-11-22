
package com.example.docAppointment.service;

import com.example.docAppointment.dto.NotificationReq;
import com.example.docAppointment.gateway.NotificationGateway;
import lombok.RequiredArgsConstructor;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;

@Component
@RequiredArgsConstructor
public class NotificationListener {

    private final NotificationGateway gateway;

    @Async("notificationExecutor")
    @TransactionalEventListener(
            phase = TransactionPhase.AFTER_COMMIT,
            fallbackExecution = true
    )
    public void handle(NotificationReq request) {
        gateway.send(request);
    }
}
