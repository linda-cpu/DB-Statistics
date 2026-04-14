package com.hszg.DB_Management.Config;


import org.springframework.dao.DataAccessException;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.reactive.function.client.WebClientResponseException;
import org.springframework.web.server.ResponseStatusException;

import lombok.extern.slf4j.Slf4j;

@Slf4j
@ControllerAdvice
public class GlobalExceptionHandler {

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
     * takes care of errors that ara thrown in the rest controllers
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
