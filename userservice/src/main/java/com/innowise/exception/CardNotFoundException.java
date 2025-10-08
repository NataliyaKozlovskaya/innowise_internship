package com.java.project.userservice.exception;

/**
 * Custom exception
 */
public class CardNotFoundException extends RuntimeException {
  public CardNotFoundException(String message) {
    super(message);
  }
}
