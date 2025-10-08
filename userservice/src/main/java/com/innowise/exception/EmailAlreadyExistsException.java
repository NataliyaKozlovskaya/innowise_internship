package com.java.project.userservice.exception;

/**
 * Custom exception
 */
public class EmailAlreadyExistsException extends RuntimeException {
  public EmailAlreadyExistsException(String message) {
    super(message);
  }
}
