package com.master.invoicemanagementserver.exception;

import com.master.invoicemanagementserver.dto.ErrorResponseDTO;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.slf4j.MDC;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.multipart.MaxUploadSizeExceededException;

import java.time.LocalDateTime;

@RestControllerAdvice
public class GlobalExceptionHandler {

    private static final Logger log = LoggerFactory.getLogger(GlobalExceptionHandler.class);

    @ExceptionHandler(InvoiceValidationException.class)
    public ResponseEntity<ErrorResponseDTO> handleValidation(InvoiceValidationException ex) {
        log.warn("Validation error: {}", ex.getMessage());
        return ResponseEntity.badRequest().body(buildError(400, "Bad Request", "The request could not be processed. Please contact support."));
    }

    @ExceptionHandler(InvoiceNotFoundException.class)
    public ResponseEntity<ErrorResponseDTO> handleNotFound(InvoiceNotFoundException ex) {
        log.warn("Not found: {}", ex.getMessage());
        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(buildError(404, "Not Found", "The requested resource was not found. Please contact support."));
    }

    @ExceptionHandler(MaxUploadSizeExceededException.class)
    public ResponseEntity<ErrorResponseDTO> handleMaxUpload(MaxUploadSizeExceededException ex) {
        log.warn("File too large: {}", ex.getMessage());
        return ResponseEntity.badRequest().body(buildError(400, "Bad Request", "The request could not be processed. Please contact support."));
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<ErrorResponseDTO> handleGeneral(Exception ex) {
        log.error("Unexpected error", ex);
        return ResponseEntity.internalServerError().body(buildError(500, "Internal Server Error", "An unexpected error occurred. Please contact support."));
    }

    private ErrorResponseDTO buildError(int status, String error, String message) {
        return new ErrorResponseDTO(LocalDateTime.now(), status, error, message, MDC.get("requestId"));
    }
}
