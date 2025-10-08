package com.java.project.authenticationservice.exception;

/**
 * Custom exception
 */
public class InvalidTokenException extends RuntimeException {
  public InvalidTokenException(String message) {
    super(message);
  }
}