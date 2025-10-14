package com.innowise.order.exception;

/**
 * Exception thrown when a user with the specified ID cannot be found
 */
public class UserNotFoundException extends RuntimeException {

  public UserNotFoundException(String message) {
    super(message);
  }
}
