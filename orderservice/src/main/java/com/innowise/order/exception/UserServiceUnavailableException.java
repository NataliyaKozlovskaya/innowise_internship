package com.innowise.order.exception;

/**
 * Exception thrown when the User Service is unavailable or unresponsive.
 */
public class UserServiceUnavailableException extends RuntimeException{
  public UserServiceUnavailableException(String message, Exception e) {
    super(message);
  }
}
