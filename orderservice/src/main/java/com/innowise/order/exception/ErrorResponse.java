package com.innowise.order.exception;

import java.time.LocalDateTime;
import lombok.Getter;
import lombok.Setter;

/**
 * Standardized error response structure for API exceptions
 */
@Getter
@Setter
public class ErrorResponse {

  private int status;
  private String message;
  private LocalDateTime timestamp;

  public ErrorResponse(int status, String message) {
    this.status = status;
    this.message = message;
    this.timestamp = LocalDateTime.now();
  }
}
