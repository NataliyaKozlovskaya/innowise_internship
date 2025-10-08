package com.java.project.userservice.exception;

/**
 * Custom exception
 */
public class UserAlreadyExistsException extends RuntimeException {
  public UserAlreadyExistsException(String message) {
    super(message);
  }
}