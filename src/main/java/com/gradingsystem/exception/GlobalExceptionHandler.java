package com.gradingsystem.exception;

import jakarta.servlet.http.HttpServletRequest;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ProblemDetail;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.net.URI;
import java.time.Instant;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * Translates domain and Spring Security exceptions to RFC 7807 ProblemDetail
 * responses (application/problem+json).
 *
 * Enable the Spring MVC default ProblemDetail rendering for built-in exceptions
 * via spring.mvc.problemdetails.enabled=true (already set in application.yml).
 */
@RestControllerAdvice
@Slf4j
public class GlobalExceptionHandler {

    // -----------------------------------------------------------------------
    // Domain exceptions
    // -----------------------------------------------------------------------

    @ExceptionHandler(ResourceNotFoundException.class)
    public ProblemDetail handleNotFound(ResourceNotFoundException ex, HttpServletRequest req) {
        return problem(HttpStatus.NOT_FOUND, "Resource Not Found", ex.getMessage(), req);
    }

    @ExceptionHandler(DuplicateResourceException.class)
    public ProblemDetail handleConflict(DuplicateResourceException ex, HttpServletRequest req) {
        return problem(HttpStatus.CONFLICT, "Duplicate Resource", ex.getMessage(), req);
    }

    @ExceptionHandler(UnauthorizedException.class)
    public ProblemDetail handleForbidden(UnauthorizedException ex, HttpServletRequest req) {
        return problem(HttpStatus.FORBIDDEN, "Access Denied", ex.getMessage(), req);
    }

    @ExceptionHandler(IllegalArgumentException.class)
    public ProblemDetail handleIllegalArgument(IllegalArgumentException ex, HttpServletRequest req) {
        return problem(HttpStatus.UNPROCESSABLE_ENTITY, "Invalid Argument", ex.getMessage(), req);
    }

    @ExceptionHandler(IllegalStateException.class)
    public ProblemDetail handleIllegalState(IllegalStateException ex, HttpServletRequest req) {
        return problem(HttpStatus.CONFLICT, "Conflict", ex.getMessage(), req);
    }

    // -----------------------------------------------------------------------
    // Spring Security exceptions
    // -----------------------------------------------------------------------

    @ExceptionHandler(BadCredentialsException.class)
    public ProblemDetail handleBadCredentials(BadCredentialsException ex, HttpServletRequest req) {
        // Generic message prevents username enumeration
        return problem(HttpStatus.UNAUTHORIZED, "Authentication Failed",
            "Invalid username or password", req);
    }

    @ExceptionHandler(AccessDeniedException.class)
    public ProblemDetail handleAccessDenied(AccessDeniedException ex, HttpServletRequest req) {
        return problem(HttpStatus.FORBIDDEN, "Access Denied",
            "You do not have permission to access this resource", req);
    }

    // -----------------------------------------------------------------------
    // Validation exception — returns field-level error map
    // -----------------------------------------------------------------------

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ProblemDetail handleValidation(MethodArgumentNotValidException ex, HttpServletRequest req) {
        Map<String, String> fieldErrors = ex.getBindingResult().getFieldErrors().stream()
            .collect(Collectors.toMap(
                FieldError::getField,
                f -> f.getDefaultMessage() != null ? f.getDefaultMessage() : "Invalid value",
                (first, second) -> first
            ));

        ProblemDetail pd = problem(HttpStatus.UNPROCESSABLE_ENTITY,
            "Validation Failed", "One or more fields are invalid", req);
        pd.setProperty("fieldErrors", fieldErrors);
        return pd;
    }

    // -----------------------------------------------------------------------
    // Catch-all
    // -----------------------------------------------------------------------

    @ExceptionHandler(Exception.class)
    public ProblemDetail handleGeneric(Exception ex, HttpServletRequest req) {
        log.error("Unhandled exception on {} {}", req.getMethod(), req.getRequestURI(), ex);
        return problem(HttpStatus.INTERNAL_SERVER_ERROR, "Internal Server Error",
            "An unexpected error occurred", req);
    }

    // -----------------------------------------------------------------------
    // Helper
    // -----------------------------------------------------------------------

    private ProblemDetail problem(HttpStatus status, String title, String detail,
                                  HttpServletRequest req) {
        ProblemDetail pd = ProblemDetail.forStatusAndDetail(status, detail);
        pd.setTitle(title);
        pd.setInstance(URI.create(req.getRequestURI()));
        pd.setProperty("timestamp", Instant.now().toString());
        return pd;
    }
}
