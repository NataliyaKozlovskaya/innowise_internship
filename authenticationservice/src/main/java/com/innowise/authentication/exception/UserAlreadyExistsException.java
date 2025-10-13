package com.innowise.authentication.exception;

/**
 * Custom exception
 */
public class UserAlreadyExistsException extends RuntimeException {
  public UserAlreadyExistsException(String message) {
    super(message);
  }
}