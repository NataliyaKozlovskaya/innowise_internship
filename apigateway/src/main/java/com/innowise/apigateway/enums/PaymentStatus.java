package com.innowise.apigateway.enums;

/**
 * Enumeration representing the possible statuses of a payment
 */
public enum PaymentStatus {
  PENDING,
  COMPLETED,
  FAILED,
  REFUNDED,
  CANCELLED;
}