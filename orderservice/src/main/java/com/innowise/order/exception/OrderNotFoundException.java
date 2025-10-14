package com.innowise.order.exception;

/**
 * Exception thrown when an order with the specified ID cannot be found
 */
public class OrderNotFoundException extends RuntimeException {

  public OrderNotFoundException(String message) {
    super(message);
  }
}