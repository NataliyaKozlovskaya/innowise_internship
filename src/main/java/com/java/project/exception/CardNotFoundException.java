package com.java.project.exception;

/**
 * Custom exception
 */
public class CardNotFoundException extends RuntimeException {
  public CardNotFoundException(String message) {
    super(message);
  }
}
