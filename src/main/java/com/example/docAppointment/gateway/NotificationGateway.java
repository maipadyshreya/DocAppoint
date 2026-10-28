
package com.example.docAppointment.gateway;

import com.example.docAppointment.dto.NotificationReq;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.client.SimpleClientHttpRequestFactory;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestClientResponseException;

import java.time.Duration;

@Component
public class NotificationGateway {

    private static final Logger log =
            LoggerFactory.getLogger(NotificationGateway.class);

    private final RestClient client;

    public NotificationGateway(
            @Value("${notification.url:http://localhost:8080/mock-notifications}")
            String url) {

        var factory = new SimpleClientHttpRequestFactory();
        factory.setConnectTimeout(Duration.ofSeconds(1));
        factory.setReadTimeout(Duration.ofSeconds(1));

        client = RestClient.builder()
                .requestFactory(factory)
                .baseUrl(url)
                .build();
    }

    public void send(NotificationReq request) {

        for (int attempt = 1; attempt <= 3; attempt++) {
            try {
                client.post()
                        .body(request)
                        .retrieve()
                        .toBodilessEntity();

                log.info("Notification delivered: id={}",
                        request.getNotificationId());
                return;

            } catch (RestClientResponseException e) {
                log.warn("Notification HTTP failure: id={}, status={}",
                        request.getNotificationId(),
                        e.getStatusCode());

                if (!e.getStatusCode().is5xxServerError()
                        && e.getStatusCode().value() != 429) {
                    break;
                }

            } catch (Exception e) {
                log.warn("Notification attempt {} failed: id={}, error={}",
                        attempt,
                        request.getNotificationId(),
                        e.getMessage());
            }
        }

        log.error("Notification delivery failed: id={}. Appointment is preserved.",
                request.getNotificationId());
    }
}
