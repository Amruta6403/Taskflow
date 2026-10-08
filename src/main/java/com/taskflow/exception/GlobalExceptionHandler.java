package com.taskflow.exception;

import jakarta.validation.ConstraintViolationException;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import org.springframework.security.access.AccessDeniedException;

import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.time.LocalDateTime;
import java.util.LinkedHashMap;
import java.util.Map;

@RestControllerAdvice
public class GlobalExceptionHandler {

    // =====================================================
    // VALIDATION ERROR
    // =====================================================

    @ExceptionHandler(
            MethodArgumentNotValidException.class
    )
    public ResponseEntity<Map<String, Object>>
    handleValidationException(
            MethodArgumentNotValidException exception) {

        Map<String, String> errors =
                new LinkedHashMap<>();

        exception.getBindingResult()
                .getFieldErrors()
                .forEach(error ->
                        errors.put(
                                error.getField(),
                                error.getDefaultMessage()
                        )
                );

        return buildResponse(
                HttpStatus.BAD_REQUEST,
                "Validation failed",
                errors
        );
    }

    // =====================================================
    // CONSTRAINT VIOLATION
    // =====================================================

    @ExceptionHandler(
            ConstraintViolationException.class
    )
    public ResponseEntity<Map<String, Object>>
    handleConstraintViolation(
            ConstraintViolationException exception) {

        Map<String, String> errors =
                new LinkedHashMap<>();

        exception.getConstraintViolations()
                .forEach(violation ->
                        errors.put(
                                violation
                                        .getPropertyPath()
                                        .toString(),
                                violation
                                        .getMessage()
                        )
                );

        return buildResponse(
                HttpStatus.BAD_REQUEST,
                "Validation failed",
                errors
        );
    }

    // =====================================================
    // TASK NOT FOUND
    // =====================================================

    @ExceptionHandler(
            TaskNotFoundException.class
    )
    public ResponseEntity<Map<String, Object>>
    handleTaskNotFound(
            TaskNotFoundException exception) {

        return buildResponse(
                HttpStatus.NOT_FOUND,
                exception.getMessage(),
                null
        );
    }

    // =====================================================
    // USER NOT FOUND
    // =====================================================

    @ExceptionHandler(
            UserNotFoundException.class
    )
    public ResponseEntity<Map<String, Object>>
    handleUserNotFound(
            UserNotFoundException exception) {

        return buildResponse(
                HttpStatus.NOT_FOUND,
                exception.getMessage(),
                null
        );
    }

    // =====================================================
    // ACCESS DENIED
    // =====================================================

    @ExceptionHandler(
            AccessDeniedException.class
    )
    public ResponseEntity<Map<String, Object>>
    handleAccessDenied(
            AccessDeniedException exception) {

        return buildResponse(
                HttpStatus.FORBIDDEN,
                exception.getMessage(),
                null
        );
    }

    // =====================================================
    // ILLEGAL ARGUMENT
    // =====================================================

    @ExceptionHandler(
            IllegalArgumentException.class
    )
    public ResponseEntity<Map<String, Object>>
    handleIllegalArgument(
            IllegalArgumentException exception) {

        return buildResponse(
                HttpStatus.BAD_REQUEST,
                exception.getMessage(),
                null
        );
    }

    // =====================================================
    // GENERAL EXCEPTION
    // =====================================================

    @ExceptionHandler(
            Exception.class
    )
    public ResponseEntity<Map<String, Object>>
    handleGeneralException(
            Exception exception) {

        return buildResponse(
                HttpStatus.INTERNAL_SERVER_ERROR,
                "An unexpected error occurred",
                null
        );
    }

    // =====================================================
    // BUILD ERROR RESPONSE
    // =====================================================

    private ResponseEntity<Map<String, Object>>
    buildResponse(
            HttpStatus status,
            String message,
            Object errors) {

        Map<String, Object> response =
                new LinkedHashMap<>();

        response.put(
                "timestamp",
                LocalDateTime.now()
        );

        response.put(
                "status",
                status.value()
        );

        response.put(
                "error",
                status.getReasonPhrase()
        );

        response.put(
                "message",
                message
        );

        if (errors != null) {

            response.put(
                    "errors",
                    errors
            );
        }

        return ResponseEntity
                .status(status)
                .body(response);
    }
}