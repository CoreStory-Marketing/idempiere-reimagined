package com.corestory.idempiere.inventory.exception;

import com.corestory.idempiere.common.dto.ApiError;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.ConstraintViolationException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.dao.OptimisticLockingFailureException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.orm.ObjectOptimisticLockingFailureException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.time.Instant;
import java.util.List;

/**
 * Centralized REST error mapping. Returns the canonical {@link ApiError} envelope
 * across the whole inventory-service API surface.
 */
@RestControllerAdvice
public class RestExceptionHandler {

    private static final Logger log = LoggerFactory.getLogger(RestExceptionHandler.class);

    @ExceptionHandler(InsufficientStockException.class)
    public ResponseEntity<ApiError> handleInsufficient(
        InsufficientStockException ex, HttpServletRequest req) {
        log.warn("Insufficient stock: {}", ex.getMessage());
        ApiError body = new ApiError(
            "INSUFFICIENT_STOCK",
            ex.getMessage(),
            Instant.now(),
            req.getRequestURI(),
            List.of(
                new ApiError.Detail("requested", String.valueOf(ex.getRequested())),
                new ApiError.Detail("available", String.valueOf(ex.getAvailable())),
                new ApiError.Detail("productId", String.valueOf(ex.getProductId()))
            )
        );
        return ResponseEntity.status(HttpStatus.CONFLICT).body(body);
    }

    @ExceptionHandler(ResourceNotFoundException.class)
    public ResponseEntity<ApiError> handleNotFound(
        ResourceNotFoundException ex, HttpServletRequest req) {
        ApiError body = ApiError.of("NOT_FOUND", ex.getMessage(), req.getRequestURI());
        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(body);
    }

    @ExceptionHandler(MandatoryAttributeException.class)
    public ResponseEntity<ApiError> handleMandatory(
        MandatoryAttributeException ex, HttpServletRequest req) {
        ApiError body = ApiError.of("MANDATORY_ATTRIBUTE", ex.getMessage(), req.getRequestURI());
        return ResponseEntity.status(HttpStatus.UNPROCESSABLE_ENTITY).body(body);
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ApiError> handleValidation(
        MethodArgumentNotValidException ex, HttpServletRequest req) {
        List<ApiError.Detail> details = ex.getBindingResult().getFieldErrors().stream()
            .map(fe -> new ApiError.Detail(fe.getField(), fe.getDefaultMessage()))
            .toList();
        ApiError body = new ApiError(
            "VALIDATION_FAILED",
            "Request validation failed",
            Instant.now(),
            req.getRequestURI(),
            details
        );
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(body);
    }

    @ExceptionHandler(ConstraintViolationException.class)
    public ResponseEntity<ApiError> handleConstraint(
        ConstraintViolationException ex, HttpServletRequest req) {
        ApiError body = ApiError.of("VALIDATION_FAILED", ex.getMessage(), req.getRequestURI());
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(body);
    }

    @ExceptionHandler({OptimisticLockingFailureException.class,
                       ObjectOptimisticLockingFailureException.class})
    public ResponseEntity<ApiError> handleOptimisticLock(
        Exception ex, HttpServletRequest req) {
        log.warn("Optimistic lock conflict: {}", ex.getMessage());
        ApiError body = ApiError.of(
            "CONCURRENT_MODIFICATION",
            "Concurrent modification detected; retry the operation.",
            req.getRequestURI());
        return ResponseEntity.status(HttpStatus.CONFLICT).body(body);
    }

    @ExceptionHandler(DataIntegrityViolationException.class)
    public ResponseEntity<ApiError> handleDataIntegrity(
        DataIntegrityViolationException ex, HttpServletRequest req) {
        log.warn("Data integrity violation: {}", ex.getMessage());
        ApiError body = ApiError.of(
            "DATA_INTEGRITY", "Database constraint violation", req.getRequestURI());
        return ResponseEntity.status(HttpStatus.CONFLICT).body(body);
    }

    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<ApiError> handleIllegalArg(
        IllegalArgumentException ex, HttpServletRequest req) {
        ApiError body = ApiError.of("BAD_REQUEST", ex.getMessage(), req.getRequestURI());
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(body);
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<ApiError> handleGeneric(Exception ex, HttpServletRequest req) {
        log.error("Unhandled exception", ex);
        ApiError body = ApiError.of(
            "INTERNAL_ERROR", "Internal server error", req.getRequestURI());
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(body);
    }
}
