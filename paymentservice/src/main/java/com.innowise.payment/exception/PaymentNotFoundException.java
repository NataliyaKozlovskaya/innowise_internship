package com.innowise.payment.exception;

/**
 * Exception thrown when a payment with the specified ID was not be found
 */
public class PaymentNotFoundException extends RuntimeException {

  public PaymentNotFoundException(String message) {
    super(message);
  }
}
