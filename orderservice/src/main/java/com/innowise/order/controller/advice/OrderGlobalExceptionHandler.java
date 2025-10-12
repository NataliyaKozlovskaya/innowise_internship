package com.innowise.order.controller.advice;

import com.innowise.order.exception.ErrorResponse;
import com.innowise.order.exception.UserServiceUnavailableException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;

/**
 * Global exception handler for Order Service
 */
@ControllerAdvice
public class OrderGlobalExceptionHandler {

  @ExceptionHandler(RuntimeException.class)
  public ResponseEntity<ErrorResponse> handleRuntimeException(RuntimeException ex) {
    ErrorResponse error = new ErrorResponse(HttpStatus.BAD_REQUEST.value(), ex.getMessage());
    return ResponseEntity.badRequest().body(error);
  }

  /**
   * Handles generic RuntimeException instances that are not caught by more specific exception
   * handlers
   */
  @ExceptionHandler(Exception.class)
  public ResponseEntity<ErrorResponse> handleException(Exception ex) {
    ErrorResponse error = new ErrorResponse(HttpStatus.INTERNAL_SERVER_ERROR.value(),
        "Internal server error");
    return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(error);
  }

  /**
   * Handles UserServiceUnavailableException when the user service is unreachable or experiencing
   * issues
   */
  @ExceptionHandler(UserServiceUnavailableException.class)
  public ResponseEntity<ErrorResponse> handleUserServiceUnavailable(
      UserServiceUnavailableException ex) {
    ErrorResponse error = new ErrorResponse(
        HttpStatus.SERVICE_UNAVAILABLE.value(),
        ex.getMessage());
    return ResponseEntity.status(HttpStatus.SERVICE_UNAVAILABLE).body(error);
  }
}

