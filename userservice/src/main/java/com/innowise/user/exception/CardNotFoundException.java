package com.innowise.user.exception;

/**
 * Custom exception
 */
public class CardNotFoundException extends RuntimeException {
  public CardNotFoundException(String message) {
    super(message);
  }
}
