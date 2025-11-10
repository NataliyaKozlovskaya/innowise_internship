package com.innowise.order.dto.kafka;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

/**
 * Order Created Event for integration between microservices via Kafka
 */
@Setter
@Getter
@AllArgsConstructor
public class OrderCreatedEvent {

  private Long orderId;
  private String userId;
  private BigDecimal amount;
  private LocalDateTime createdAt;
}