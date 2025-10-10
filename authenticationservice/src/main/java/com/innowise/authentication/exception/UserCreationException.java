package com.innowise.authentication.exception;

/**
 * Custom exception
 */
public class UserCreationException extends RuntimeException  {
  public UserCreationException(String message) {
    super(message);
  }
}
