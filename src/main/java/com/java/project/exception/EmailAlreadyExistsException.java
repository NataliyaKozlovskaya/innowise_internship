package com.java.project.exception;

/**
 * Custom exception
 */
public class EmailAlreadyExistsException extends RuntimeException {
  public EmailAlreadyExistsException(String message) {
    super(message);
  }
}
