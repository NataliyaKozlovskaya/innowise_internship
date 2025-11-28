package com.innowise.payment.dto.kafka;

import com.innowise.payment.enums.PaymentStatus;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * Payment Processed Event representing the completion of a payment transaction
 */
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class PaymentProcessedEvent {

  private Long orderId;
  private String paymentId;
  private PaymentStatus status;
}
