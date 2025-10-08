package com.java.project.authenticationservice.controller.advice;

import com.java.project.authenticationservice.exception.InvalidTokenException;
import com.java.project.authenticationservice.exception.UserNotFoundException;
import com.java.project.userservice.exception.CardNotFoundException;
import com.java.project.userservice.exception.EmailAlreadyExistsException;
import com.java.project.userservice.exception.ErrorResponse;
import java.util.stream.Collectors;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.core.AuthenticationException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.MissingServletRequestParameterException;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.context.request.ServletWebRequest;

@ControllerAdvice
@Slf4j
public class GlobalExceptionHandler {

  /**
   * Handle UserNotFoundException - 404 Not Found
   */
  @ExceptionHandler(UserNotFoundException.class)
  public ResponseEntity<ErrorResponse> handleUserNotFound(UserNotFoundException ex,
      ServletWebRequest request) {
    ErrorResponse errorResponse = new ErrorResponse(
        HttpStatus.NOT_FOUND.value(),
        ex.getMessage(),
        getRequestPath(request)
    );

    log.warn("User not found: {}", ex.getMessage());
    return ResponseEntity.status(HttpStatus.NOT_FOUND).body(errorResponse);
  }

  /**
   * Handle EmailAlreadyExistsException - 409 Conflict
   */
  @ExceptionHandler(EmailAlreadyExistsException.class)
  public ResponseEntity<ErrorResponse> handleEmailAlreadyExists(EmailAlreadyExistsException ex,
      ServletWebRequest request) {
    ErrorResponse errorResponse = new ErrorResponse(
        HttpStatus.CONFLICT.value(),
        ex.getMessage(),
        getRequestPath(request)
    );

    log.warn("Email already exists: {}", ex.getMessage());
    return ResponseEntity.status(HttpStatus.CONFLICT).body(errorResponse);
  }

  /**
   * Handle CardNotFoundException - 404 Not Found
   */
  @ExceptionHandler(CardNotFoundException.class)
  public ResponseEntity<ErrorResponse> handleCardNotFound(CardNotFoundException ex,
      ServletWebRequest request) {
    ErrorResponse errorResponse = new ErrorResponse(
        HttpStatus.NOT_FOUND.value(),
        ex.getMessage(),
        getRequestPath(request)
    );

    log.warn("Card not found: {}", ex.getMessage());
    return ResponseEntity.status(HttpStatus.NOT_FOUND).body(errorResponse);
  }

  /**
   * Handle validation errors - 400 Bad Request
   */
  @ExceptionHandler(MethodArgumentNotValidException.class)
  public ResponseEntity<ErrorResponse> handleValidationErrors(MethodArgumentNotValidException ex,
      ServletWebRequest request) {
    String errorMessage = ex.getBindingResult()
        .getFieldErrors()
        .stream()
        .map(error -> error.getField() + ": " + error.getDefaultMessage())
        .collect(Collectors.joining(", "));

    ErrorResponse errorResponse = new ErrorResponse(
        HttpStatus.BAD_REQUEST.value(),
        errorMessage,
        getRequestPath(request)
    );

    log.warn("Validation error: {}", errorMessage);
    return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(errorResponse);
  }

  /**
   * Handle missing path variables - 400 Bad Request
   */
  @ExceptionHandler(MissingServletRequestParameterException.class)
  public ResponseEntity<ErrorResponse> handleMissingParams(
      MissingServletRequestParameterException ex,
      ServletWebRequest request) {
    ErrorResponse errorResponse = new ErrorResponse(
        HttpStatus.BAD_REQUEST.value(),
        "Required parameter is missing: " + ex.getParameterName(),
        getRequestPath(request)
    );

    log.warn("Missing parameter: {}", ex.getParameterName());
    return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(errorResponse);
  }

  /**
   * Handle general exceptions - 500 Internal Server Error
   */
  @ExceptionHandler(Exception.class)
  public ResponseEntity<ErrorResponse> handleGenericException(Exception ex,
      ServletWebRequest request) {
    ErrorResponse errorResponse = new ErrorResponse(
        HttpStatus.INTERNAL_SERVER_ERROR.value(),
        "An unexpected error occurred",
        getRequestPath(request)
    );

    log.error("Internal server error: {}", ex.getMessage(), ex);
    return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(errorResponse);
  }

  @ExceptionHandler(AuthenticationException.class)
  public ResponseEntity<ErrorResponse> handleAuthenticationException(AuthenticationException ex) {
    log.error("Authentication error: {}", ex.getMessage());

    ErrorResponse error = new ErrorResponse(
        HttpStatus.UNAUTHORIZED.value(),
        "AUTHENTICATION_FAILED",
        "Authentication failed"
    );
    return new ResponseEntity<>(error, HttpStatus.UNAUTHORIZED);
  }

  @ExceptionHandler(AccessDeniedException.class)
  public ResponseEntity<ErrorResponse> handleAccessDeniedException(AccessDeniedException ex) {
    log.error("Access denied: {}", ex.getMessage());

    ErrorResponse error = new ErrorResponse(
        HttpStatus.FORBIDDEN.value(),
        "ACCESS_DENIED",
        "Access denied"
        );
    return new ResponseEntity<>(error, HttpStatus.FORBIDDEN);
  }

  @ExceptionHandler(BadCredentialsException.class)
  public ResponseEntity<ErrorResponse> handleBadCredentialsException(BadCredentialsException ex) {
    log.error("Bad credentials: {}", ex.getMessage());

    ErrorResponse error = new ErrorResponse(
        HttpStatus.UNAUTHORIZED.value(),
        "BAD_CREDENTIALS",
        "Invalid login or password"
    );
    return new ResponseEntity<>(error, HttpStatus.UNAUTHORIZED);
  }

  @ExceptionHandler(InvalidTokenException.class)
  public ResponseEntity<ErrorResponse> handleInvalidTokenException(InvalidTokenException ex) {
    log.error("Invalid token: {}", ex.getMessage());

    ErrorResponse error = new ErrorResponse(
        HttpStatus.UNAUTHORIZED.value(),
        "INVALID_TOKEN",
        ex.getMessage()
    );
    return new ResponseEntity<>(error, HttpStatus.UNAUTHORIZED);
  }

  /**
   * Extract request path from ServletWebRequest
   */
  private String getRequestPath(ServletWebRequest request) {
    return request.getRequest().getRequestURI();
  }
}