package com.innowise.order.exception;

/**
 * Exception thrown when one or more requested items are not found in the inventory
 */
public class ItemNotFoundException extends RuntimeException{
  public ItemNotFoundException(String message) {
    super(message);
  }
}
