package com.innowise.order.dto.kafka;

import java.time.LocalDateTime;
import lombok.Getter;
import lombok.Setter;

@Setter
@Getter
public class PaymentProcessedEvent {
  private String orderId;
  private String paymentId;
  private String status;
//  private String message;
//  private LocalDateTime processedAt;
}
