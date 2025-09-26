package com.java.project.exception;

/**
 * Custom exception
 */
public class UserNotFoundException extends RuntimeException {
  public UserNotFoundException(String message) {
    super(message);
  }
}
