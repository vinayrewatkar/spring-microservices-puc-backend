package com.puc.regionService.exception;

import com.puc.regionService.util.ResponseHandler;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(RegionNotFoundException.class)
    public ResponseEntity<Object> handleRegionNotFoundException(RegionNotFoundException ex) {
        return ResponseHandler.generateResponse(
                ex.getMessage(),
                HttpStatus.NOT_FOUND,
                null
        );
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<Object> handleValidationExceptions(MethodArgumentNotValidException ex) {
        String errorMessage = ex.getBindingResult().getFieldErrors().stream()
                .map(error -> error.getField() + ": " + error.getDefaultMessage())
                .reduce((a, b) -> a + ", " + b)
                .orElse("Validation error");

        return ResponseHandler.generateResponse(
                errorMessage,
                HttpStatus.BAD_REQUEST,
                null
        );
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<Object> handleGlobalException(Exception ex) {
        return ResponseHandler.generateResponse(
                "Internal server error: " + ex.getMessage(),
                HttpStatus.INTERNAL_SERVER_ERROR,
                null
        );
    }
}
