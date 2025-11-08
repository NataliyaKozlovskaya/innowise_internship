package com.innowise.payment.kafka;

import com.innowise.payment.enums.PaymentStatus;
import java.time.LocalDateTime;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 *
 */
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class PaymentProcessedEvent {

  private String orderId;
  private String paymentId;
  private PaymentStatus status;
//  private String message;
//  private LocalDateTime processedAt;

}
