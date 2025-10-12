package com.innowise.user.exception;

import java.time.LocalDateTime;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class ErrorResponse {

  private LocalDateTime timestamp;
  private int status;
  private String message;
  private String path;


  public ErrorResponse() {
    this.timestamp = LocalDateTime.now();
  }

  public ErrorResponse(int status, String message, String path) {
    this();
    this.status = status;
    this.message = message;
    this.path = path;
  }
}
