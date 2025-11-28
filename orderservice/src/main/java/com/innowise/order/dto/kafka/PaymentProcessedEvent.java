package com.innowise.order.dto.kafka;

import lombok.Getter;
import lombok.Setter;

/**
 * Payment Processed Event representing the completion of a payment transaction
 */
@Setter
@Getter
public class PaymentProcessedEvent {

  private Long orderId;
  private String paymentId;
  private String status;
}
