package com.medilabo.assessment.exception;

import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.reactive.function.client.WebClientResponseException;

import java.util.Map;

/**
 * Global exception handler for the Assessment microservice.
 */
@Slf4j
@RestControllerAdvice
public class GlobalExceptionHandler {

    /** Handle errors when calling downstream services (demographics / notes) */
    @ExceptionHandler(WebClientResponseException.class)
    public ResponseEntity<Map<String, String>> handleWebClientError(WebClientResponseException ex) {
        log.error("Downstream service error: {} — {}", ex.getStatusCode(), ex.getMessage());

        if (ex.getStatusCode().value() == 404) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(Map.of(
                    "error", "Not Found",
                    "message", "Patient or notes not found: " + ex.getMessage()
            ));
        }

        return ResponseEntity.status(HttpStatus.BAD_GATEWAY).body(Map.of(
                "error", "Service Unavailable",
                "message", "Error communicating with downstream service: " + ex.getMessage()
        ));
    }

    /** Handle any unexpected errors */
    @ExceptionHandler(Exception.class)
    public ResponseEntity<Map<String, String>> handleGenericError(Exception ex) {
        log.error("Unexpected error during assessment", ex);
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(Map.of(
                "error", "Internal Server Error",
                "message", ex.getMessage()
        ));
    }
}
