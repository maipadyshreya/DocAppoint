
package com.example.docAppointment.controller;

import com.example.docAppointment.dto.NotificationReq;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

@RestController
@RequestMapping("/mock-notifications")
public class NotificationController {

    private static final Logger log =
            LoggerFactory.getLogger(NotificationController.class);

    private final Set<String> processedIds =
            ConcurrentHashMap.newKeySet();

    @PostMapping
    public ResponseEntity<String> send(
            @RequestBody NotificationReq request) {

        if (request.getNotificationId() == null ||
                request.getNotificationId().isBlank() ||
                request.getRecipient() == null ||
                request.getRecipient().isBlank() ||
                request.getType() == null ||
                request.getType().isBlank() ||
                request.getMessage() == null ||
                request.getMessage().isBlank()) {

            return ResponseEntity.badRequest()
                    .body("Invalid notification request");
        }

        if (!processedIds.add(request.getNotificationId())) {
            return ResponseEntity.ok("Already processed");
        }

        log.info("Notification accepted: id={}, type={}, recipient={}",
                request.getNotificationId(),
                request.getType(),
                request.getRecipient());

        return ResponseEntity.ok("Notification sent");
    }
}
