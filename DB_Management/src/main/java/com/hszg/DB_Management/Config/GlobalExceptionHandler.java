package com.hszg.DB_Management.Config;


import java.util.HashMap;
import java.util.Map;

import org.springframework.dao.DataAccessException;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.reactive.function.client.WebClientResponseException;
import org.springframework.web.server.ResponseStatusException;

import com.fasterxml.jackson.databind.exc.MismatchedInputException;

import lombok.extern.slf4j.Slf4j;

@Slf4j
@ControllerAdvice
public class GlobalExceptionHandler {

    /**
     * takes care of errors that are thrown because the incoming JSON is malformed or has invalid data types
     */
    @ExceptionHandler(HttpMessageNotReadableException.class)
    public ResponseEntity<String> handleJsonMappingException(HttpMessageNotReadableException ex) {
        log.warn("JSON parsing error: {}", ex.getMessage());
        
        String responseMessage = "Malformed JSON request body or invalid data types.";

        // Dig down to give the user a precise field mismatch error if applicable
        if (ex.getCause() instanceof MismatchedInputException mismatchedEx) {
            String fieldName = mismatchedEx.getPath().stream()
                    .map(com.fasterxml.jackson.databind.JsonMappingException.Reference::getFieldName)
                    .reduce((first, second) -> second)
                    .orElse("unknown");

            String targetType = mismatchedEx.getTargetType().getSimpleName();
            responseMessage = String.format("JSON Parsing Error: Invalid value for field '%s'. Expected type: %s", fieldName, targetType);
        }

        return ResponseEntity
                .status(HttpStatus.BAD_REQUEST)
                .header(HttpHeaders.CONTENT_TYPE, MediaType.TEXT_PLAIN_VALUE)
                .body(responseMessage);
    }

    /**
     * takes care of errors when @Valid validation constraints fail (e.g., @NotBlank, @NotEmpty)
     * Returns a JSON object mapping fields to their specific errors
     */
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<Map<String, String>> handleValidationException(MethodArgumentNotValidException ex) {
        log.warn("Validation failed for request: {}", ex.getBindingResult().getObjectName());
        
        Map<String, String> errors = new HashMap<>();
        ex.getBindingResult().getFieldErrors().forEach(error -> 
            errors.put(error.getField(), error.getDefaultMessage())
        );

        return ResponseEntity
                .status(HttpStatus.BAD_REQUEST)
                .header(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE) // Returns Map as JSON object
                .body(errors);
    }

    /**
     * takes care of errors that are thrown because the database can not be accessed
     * 
     * @param exception
     * @return
     */
    @ExceptionHandler(DataAccessException.class)
    public ResponseEntity<String> handleDatabaseException(DataAccessException ex) {
        log.error("Database error occurred: {}", ex.getMessage(), ex);
        return ResponseEntity
                .status(HttpStatus.INTERNAL_SERVER_ERROR)
                .header(HttpHeaders.CONTENT_TYPE, MediaType.TEXT_PLAIN_VALUE)
                .body("A technical error occurred while accessing the database.");
    }

    /**
     * takes care of errors that are thrown because the external DB-API is not available or throws errors
     * 
     * @param exception
     * @return
     */
    @ExceptionHandler(WebClientResponseException.class)
    public ResponseEntity<String> handleExternalApiException(WebClientResponseException ex) {
        log.error("DB-API (Deutsche Bahn) returned error: {} - {}", ex.getStatusCode(), ex.getResponseBodyAsString());
        return ResponseEntity
                .status(HttpStatus.BAD_GATEWAY)
                .header(HttpHeaders.CONTENT_TYPE, MediaType.TEXT_PLAIN_VALUE)
                .body("Could not fetch data from the external Deutsche Bahn service.");
    }

    /**
     * takes care of errors that are thrown in the rest controllers
     * @param exception
     * @return
     */
    @ExceptionHandler(ResponseStatusException.class)
    public ResponseEntity<String> handleResponseStatusException(ResponseStatusException ex) {
        if (ex.getStatusCode() == HttpStatus.NOT_FOUND) {
            log.warn("Resource not found: {}", ex.getReason());
        } else if (ex.getStatusCode() == HttpStatus.UNAUTHORIZED) {
            log.warn("Unauthorized access attempt: {}", ex.getReason());
        } else {
            log.error("Restcontroller has thrown error: ", ex);
        }
        return ResponseEntity
                .status(ex.getStatusCode())
                .header(HttpHeaders.CONTENT_TYPE, MediaType.TEXT_PLAIN_VALUE)
                .body(ex.getReason());
    }

    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<String> handleIllegalArgument(IllegalArgumentException ex) {
        log.error("Rest controller has thrown: ", ex);

        return ResponseEntity
                .status(HttpStatus.BAD_REQUEST)
                .header(HttpHeaders.CONTENT_TYPE, MediaType.TEXT_PLAIN_VALUE)
                .body(ex.getMessage());
    }

    /**
     * takes care of all other errors that are thrown
     * 
     * @param exception
     * @return
     */
    @ExceptionHandler(Exception.class)
    public ResponseEntity<String> handleGeneralException(Exception ex) {
        log.error("Unexpected error: ", ex);
        return ResponseEntity
                .status(HttpStatus.INTERNAL_SERVER_ERROR)
                .header(HttpHeaders.CONTENT_TYPE, MediaType.TEXT_PLAIN_VALUE)
                .body("An internal error occurred.");
    }
}
