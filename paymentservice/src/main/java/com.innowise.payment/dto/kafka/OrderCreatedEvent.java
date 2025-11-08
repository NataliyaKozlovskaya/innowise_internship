package com.innowise.payment.dto.kafka;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import lombok.Getter;
import lombok.Setter;

/**
 *
 */
@Getter
@Setter
public class OrderCreatedEvent {

  private String orderId;
  private String userId;
  private BigDecimal amount;
  private LocalDateTime createdAt;
}
