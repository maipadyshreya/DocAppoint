
package com.example.docAppointment.exception;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.server.ResponseStatusException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.dao.DataIntegrityViolationException;

import java.time.LocalDateTime;
import java.util.Map;
import java.util.LinkedHashMap;

@RestControllerAdvice
public class GlobalExceptionHandler {

    private ResponseEntity<Map<String, Object>> error(
            HttpStatus status, String message) {

        Map<String, Object> body = new LinkedHashMap<>();
        body.put("timestamp", LocalDateTime.now().toString());
        body.put("status", status.value());
        body.put("error", status.getReasonPhrase());
        body.put("message", message);

        return ResponseEntity.status(status).body(body);
    }

    @ExceptionHandler(ResponseStatusException.class)
    public ResponseEntity<Map<String, Object>> handleStatus(
            ResponseStatusException ex) {

        HttpStatus status =
                HttpStatus.resolve(ex.getStatusCode().value());

        if (status == null) {
            status = HttpStatus.INTERNAL_SERVER_ERROR;
        }

        return error(status,
                ex.getReason() != null
                        ? ex.getReason()
                        : "Request failed");
    }

    @ExceptionHandler({
            MethodArgumentNotValidException.class,
            HttpMessageNotReadableException.class,
            IllegalArgumentException.class
    })
    public ResponseEntity<Map<String, Object>> handleBadRequest(
            Exception ex) {

        return error(HttpStatus.BAD_REQUEST,
                "Invalid request input");
    }

    @ExceptionHandler(DataIntegrityViolationException.class)
    public ResponseEntity<Map<String, Object>> handleDatabaseConflict(
            DataIntegrityViolationException ex) {

        return error(HttpStatus.CONFLICT,
                "Database constraint violation");
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<Map<String, Object>> handleUnexpected(
            Exception ex) {

        return error(HttpStatus.INTERNAL_SERVER_ERROR,
                "An unexpected server error occurred");
    }
}
