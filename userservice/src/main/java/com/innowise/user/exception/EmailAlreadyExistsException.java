package com.innowise.user.exception;

/**
 * Custom exception
 */
public class EmailAlreadyExistsException extends RuntimeException {
  public EmailAlreadyExistsException(String message) {
    super(message);
  }
}
